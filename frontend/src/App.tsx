import { useEffect, useState } from "react";
import {
    api,
    ApiError,
    date,
    money,
    type Sale,
    type Show,
    type Tenant,
    type Ticket,
    type User,
} from "./api";
import { Auth } from "./Auth";
import { Checkout, restore } from "./Checkout";
export default function App() {
    const [tenant, setTenant] = useState<Tenant>("CINE_TICS"),
        [user, setUser] = useState<User | null>(null),
        [ready, setReady] = useState(false),
        [view, setView] = useState<"shows" | "auth" | "checkout" | "tickets">(
            "shows",
        ),
        [shows, setShows] = useState<Show[]>([]),
        [selected, setSelected] = useState<Show | null>(null),
        [loading, setLoading] = useState(true),
        [error, setError] = useState(""),
        [tickets, setTickets] = useState<Ticket[]>([]),
        [sale, setSale] = useState<Sale | null>(null),
        [account, setAccount] = useState(false),
        [busy, setBusy] = useState(false);
    const customer = !!user?.roles.includes("ROLE_CLIENTE");
    useEffect(() => {
        window.scrollTo({ top: 0, behavior: "auto" });
        document.getElementById("main")?.focus({ preventScroll: true });
    }, [view]);
    useEffect(() => {
        api.me()
            .then((u) => {
                setUser(u);
                setTenant(u.tenant);
            })
            .catch((e) => {
                if (!(e instanceof ApiError && e.status === 401))
                    setError(e.message);
            })
            .finally(() => setReady(true));
    }, []);
    useEffect(() => {
        if (!ready) return;
        let active = true;
        setLoading(true);
        api.shows(tenant)
            .then((data) => {
                if (active) setShows(data);
            })
            .catch((e) => {
                if (active) setError(e.message);
            })
            .finally(() => {
                if (active) setLoading(false);
            });
        return () => {
            active = false;
        };
    }, [tenant, ready]);
    const failure = (e: unknown) => {
        setError(
            e instanceof Error
                ? e.message
                : "No pudimos cargar la información.",
        );
        if (e instanceof ApiError && e.status === 401) {
            setUser(null);
            setAccount(false);
            setView("auth");
        }
    };
    function choose(show: Show) {
        setError("");
        if (user && !customer) {
            setError(
                "La compra de boletos está disponible para cuentas de cliente.",
            );
            return;
        }
        const previous = user && restore(user);
        setSelected(previous ?? show);
        setSale(null);
        setView(user ? "checkout" : "auth");
    }
    async function history() {
        setTickets([]);
        setError("");
        setView("tickets");
        setLoading(true);
        try {
            const result = await api.tickets();
            setTickets(result);
            if (user) {
                const key = `cine-hold:${user.tenant}:${user.memberId}`;
                try {
                    const held = JSON.parse(
                        sessionStorage.getItem(key) ?? "null",
                    );
                    if (
                        held?.selected.every((n: number) =>
                            result.some(
                                (t) =>
                                    t.funcion === held.show.id &&
                                    t.asiento === n,
                            ),
                        )
                    )
                        sessionStorage.removeItem(key);
                } catch {
                    /* Ignore malformed local recovery data. */
                }
            }
        } catch (e) {
            failure(e);
        } finally {
            setLoading(false);
        }
    }
    async function done(result: Sale) {
        setSale(result);
        setView("tickets");
        setSelected(null);
        await history();
        api.me().then(setUser).catch(failure);
    }
    async function logout() {
        setBusy(true);
        try {
            await api.logout();
            setUser(null);
            setSelected(null);
            setTickets([]);
            setSale(null);
            setAccount(false);
            setView("shows");
        } catch (e) {
            failure(e);
        } finally {
            setBusy(false);
        }
    }
    function loggedIn(u: User) {
        setTickets([]);
        setSale(null);
        setAccount(false);
        setUser(u);
        setTenant(u.tenant);
        setError("");
        const current = restore(u) ?? selected;
        if (current && u.roles.includes("ROLE_CLIENTE")) {
            setSelected(current);
            setView("checkout");
        } else setView("shows");
    }
    return (
        <>
            <a href="#main" className="skip">
                Saltar al contenido
            </a>
            <div className="demo-bar">
                <span>UNA NUEVA FORMA DE VIVIR EL CINE</span>
                <span>DEMO · PAGOS SIMULADOS</span>
            </div>
            <header>
                <button
                    className="brand"
                    onClick={() => {
                        setView("shows");
                        setError("");
                    }}
                    aria-label="CINE-TICS, inicio"
                >
                    <span className="brand-icon">
                        C<span>✦</span>
                    </span>
                    CINE<span className="brand-light">—TICS</span>
                </button>
                <nav aria-label="Principal">
                    <button
                        className={view === "shows" ? "active" : ""}
                        onClick={() => setView("shows")}
                    >
                        Cartelera
                    </button>
                    {customer && (
                        <button
                            className={view === "tickets" ? "active" : ""}
                            onClick={history}
                        >
                            Mis boletos
                        </button>
                    )}
                </nav>
                <div className="header-actions">
                    <label className="tenant-label">
                        <span className="sr-only">Cadena</span>
                        <select
                            aria-label="Cadena"
                            value={tenant}
                            disabled={!!user}
                            onChange={(e) => {
                                setTenant(e.target.value as Tenant);
                                setSelected(null);
                                setView("shows");
                                setError("");
                            }}
                        >
                            <option value="CINE_TICS">CINE-TICS</option>
                            <option value="CADENA_DEMO">Cadena demo</option>
                        </select>
                    </label>
                    {user ? (
                        <button
                            className="account-btn"
                            onClick={() => setAccount(!account)}
                            aria-expanded={account}
                        >
                            <span className="avatar">{user.nombre[0]}</span>
                            {user.nombre}
                            <span>⌄</span>
                        </button>
                    ) : (
                        <button
                            className="outline"
                            disabled={!ready}
                            onClick={() => setView("auth")}
                        >
                            Mi cuenta ↗
                        </button>
                    )}
                </div>
            </header>
            {account && user && (
                <aside className="account-panel panel">
                    <strong>Hola, {user.nombre}</strong>
                    <p className="muted">Tu ID de socio</p>
                    <code>{user.memberId}</code>
                    <p>
                        Saldo de demostración <b>{money(user.saldoDemo)}</b>
                    </p>
                    <p className="small muted">
                        {user.tenant} ·{" "}
                        {user.roles
                            .filter((r) => r.startsWith("ROLE_"))
                            .map((r) => r.slice(5))
                            .join(", ")}
                    </p>
                    <button
                        className="outline full"
                        disabled={busy}
                        onClick={logout}
                    >
                        {busy ? "Cerrando…" : "Cerrar sesión"}
                    </button>
                </aside>
            )}
            <main id="main" tabIndex={-1}>
                {error && (
                    <div className="error global-error" role="alert">
                        {error}
                        <button
                            onClick={() => setError("")}
                            aria-label="Cerrar aviso"
                        >
                            ×
                        </button>
                    </div>
                )}
                {view === "shows" && (
                    <>
                        <section className="hero">
                            <div className="hero-copy">
                                <p className="eyebrow">
                                    <i /> LUCES FUERA. HISTORIAS DENTRO.
                                </p>
                                <h1>
                                    Tu próxima
                                    <br />
                                    buena <em>historia.</em>
                                </h1>
                                <p>
                                    Elige tu función, encuentra tu lugar y
                                    disfruta.
                                    <br />
                                    Nosotros ponemos la pantalla.
                                </p>
                                <a className="primary" href="#cartelera">
                                    Explorar funciones <span>↘</span>
                                </a>
                                <div className="hero-foot">
                                    <span>01 / CARTELERA</span>
                                    <span>HECHO PARA COMPARTIR</span>
                                </div>
                            </div>
                            <div className="hero-art" aria-hidden="true">
                                <div className="orbit orbit-one" />
                                <div className="orbit orbit-two" />
                                <div className="art-ticket">
                                    <div className="ticket-top">
                                        CINE—TICS <span>EST. 2025</span>
                                    </div>
                                    <div className="asterisk">✳</div>
                                    <div className="ticket-title">
                                        LA MAGIA
                                        <br />
                                        ESTÁ EN
                                        <br />
                                        <em>VENIR.</em>
                                    </div>
                                    <div className="ticket-bottom">
                                        UNA BUTACA.
                                        <br />
                                        MIL HISTORIAS.<span>↗</span>
                                    </div>
                                </div>
                                <div className="art-caption">
                                    PRIMERA FILA PARA LAS BUENAS HISTORIAS
                                </div>
                            </div>
                        </section>
                        {customer && user && restore(user) && (
                            <div className="resume notice">
                                <span>
                                    Tienes una operación pendiente. Puedes
                                    continuar con la misma reserva.
                                </span>
                                <button
                                    className="text-button"
                                    onClick={() => choose(restore(user)!)}
                                >
                                    Continuar →
                                </button>
                            </div>
                        )}
                        <section id="cartelera" className="show-section">
                            <div className="section-heading">
                                <div>
                                    <p className="eyebrow">
                                        EL PLAN EMPIEZA AQUÍ
                                    </p>
                                    <h2>
                                        En cartelera
                                        <span className="lime-dot">.</span>
                                    </h2>
                                </div>
                                <span className="pill">
                                    {tenant === "CINE_TICS"
                                        ? "Todas las sucursales"
                                        : "Cadena de demostración"}
                                </span>
                            </div>
                            {loading ? (
                                <p role="status" className="empty">
                                    Buscando tu próxima función…
                                </p>
                            ) : shows.length === 0 ? (
                                <div className="empty panel">
                                    <span className="empty-icon">◷</span>
                                    <h3>
                                        La próxima historia está por llegar.
                                    </h3>
                                    <p>
                                        No hay funciones futuras en esta cadena.
                                        Vuelve a consultar más tarde.
                                    </p>
                                </div>
                            ) : (
                                <div className="show-grid">
                                    {shows.map((show, i) => (
                                        <article
                                            className="show-card"
                                            key={show.id}
                                        >
                                            <div
                                                className={`poster poster-${i % 3}`}
                                            >
                                                <span className="poster-label">
                                                    FUNCIÓN DE DEMOSTRACIÓN
                                                </span>
                                                <div className="poster-ring" />
                                                <span className="poster-number">
                                                    {String(i + 1).padStart(
                                                        2,
                                                        "0",
                                                    )}
                                                </span>
                                                <div className="poster-copy">
                                                    <small>
                                                        CINE-TICS PRESENTA
                                                    </small>
                                                    <strong>
                                                        {show.titulo}
                                                    </strong>
                                                    <span>
                                                        EN PANTALLA ·{" "}
                                                        {show.sucursal.toUpperCase()}
                                                    </span>
                                                </div>
                                            </div>
                                            <div className="show-info">
                                                <div className="show-meta">
                                                    <span>{show.sucursal}</span>
                                                    <span>{show.sala}</span>
                                                </div>
                                                <h3>{show.titulo}</h3>
                                                <p>{date(show.inicio)}</p>
                                                <div className="show-actions">
                                                    <span>
                                                        Adulto <b>$80</b>{" "}
                                                        <small>
                                                            / Niño $50
                                                        </small>
                                                    </span>
                                                    <button
                                                        className="round-button"
                                                        onClick={() =>
                                                            choose(show)
                                                        }
                                                        aria-label={`Elegir asientos para ${show.titulo}`}
                                                    >
                                                        ↗
                                                    </button>
                                                </div>
                                            </div>
                                        </article>
                                    ))}
                                </div>
                            )}
                            <p className="small muted">
                                Horarios de Ciudad de México · Precios en MXN ·
                                Funciones y saldo de demostración.
                            </p>
                        </section>
                        <section className="bottom-band">
                            <span className="spark">✦</span>
                            <h2>
                                Las mejores historias
                                <br />
                                se viven en compañía.
                            </h2>
                            <span>
                                CINE—TICS
                                <br />
                                <small>NOS VEMOS EN EL CINE.</small>
                            </span>
                        </section>
                    </>
                )}
                {view === "auth" && (
                    <Auth
                        tenant={tenant}
                        onLogin={loggedIn}
                        onBack={() => setView("shows")}
                    />
                )}
                {view === "checkout" && selected && user && (
                    <Checkout
                        key={`${user.memberId}:${selected.id}`}
                        show={selected}
                        user={user}
                        onBack={() => setView("shows")}
                        onDone={done}
                        onSessionExpired={() => {
                            setUser(null);
                            setView("auth");
                        }}
                    />
                )}
                {view === "tickets" && (
                    <section className="tickets-page">
                        <div className="section-heading">
                            <div>
                                <p className="eyebrow">TU PRÓXIMA HISTORIA</p>
                                <h1>
                                    Mis boletos
                                    <span className="lime-dot">.</span>
                                </h1>
                                <p className="muted">
                                    Aquí están tus lugares confirmados.
                                </p>
                            </div>
                            <button
                                className="outline"
                                onClick={() => setView("shows")}
                            >
                                Ver cartelera ↗
                            </button>
                        </div>
                        {sale && (
                            <div className="success" role="status">
                                <strong>
                                    ✓ ¡Tus lugares están confirmados!
                                </strong>
                                <p>
                                    {sale.boletos.length} boletos ·{" "}
                                    {money(sale.total)} · Pago simulado
                                    {sale.repetida
                                        ? " recuperado, sin un nuevo cargo"
                                        : ""}
                                    .
                                </p>
                            </div>
                        )}
                        {loading ? (
                            <p role="status">Consultando tus boletos…</p>
                        ) : tickets.length === 0 ? (
                            <div className="panel empty">
                                <span className="empty-icon">▣</span>
                                <h2>Una historia te espera.</h2>
                                <p>
                                    Todavía no tienes boletos. Encuentra tu
                                    próxima función en la cartelera.
                                </p>
                            </div>
                        ) : (
                            <div className="ticket-list">
                                {tickets.map((t) => {
                                    const show = shows.find(
                                        (s) => s.id === t.funcion,
                                    );
                                    return (
                                        <article
                                            className="admission panel"
                                            key={t.id}
                                        >
                                            <div className="admission-main">
                                                <p className="eyebrow">
                                                    CINE—TICS · BOLETO DEMO
                                                </p>
                                                <h2>
                                                    {show?.titulo ??
                                                        `Función ${t.funcion}`}
                                                </h2>
                                                <p className="muted">
                                                    {show
                                                        ? `${show.sucursal} · ${date(show.inicio)}`
                                                        : `Función ${t.funcion}`}
                                                </p>
                                                <code>{t.id}</code>
                                                <p className="small muted">
                                                    Pago simulado · No es un
                                                    comprobante fiscal.
                                                </p>
                                            </div>
                                            <div className="admission-seat">
                                                <span>ASIENTO</span>
                                                <strong>{t.asiento}</strong>
                                                <span>
                                                    {t.tipo === "NINO"
                                                        ? "Niño"
                                                        : "Adulto"}{" "}
                                                    · {money(t.precio)}
                                                </span>
                                            </div>
                                        </article>
                                    );
                                })}
                            </div>
                        )}
                    </section>
                )}
            </main>
            <footer>
                <span className="footer-brand">CINE—TICS</span>
                <span>Un proyecto con muchas historias por contar.</span>
                <span>DEMOSTRACIÓN ACADÉMICA · 2026</span>
            </footer>
        </>
    );
}

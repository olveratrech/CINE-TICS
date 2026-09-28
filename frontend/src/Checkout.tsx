import { useEffect, useRef, useState } from "react";
import {
    api,
    ApiError,
    date,
    money,
    type Fare,
    type Hold,
    type Sale,
    type Seat,
    type Show,
    type User,
} from "./api";
type Pending = {
    show: Show;
    key: string;
    selected: number[];
    hold?: Hold;
    fares: Fare[];
    attempted?: boolean;
};
const storage = (user: User) => `cine-hold:${user.tenant}:${user.memberId}`;
export function restore(user: User): Show | null {
    try {
        return (
            JSON.parse(sessionStorage.getItem(storage(user)) ?? "null")?.show ??
            null
        );
    } catch {
        return null;
    }
}
export function Checkout({
    show,
    user,
    onBack,
    onDone,
    onSessionExpired,
}: {
    show: Show;
    user: User;
    onBack: () => void;
    onDone: (sale: Sale) => void;
    onSessionExpired: () => void;
}) {
    const [seats, setSeats] = useState<Seat[]>([]),
        [selected, setSelected] = useState<number[]>([]),
        [pending, setPending] = useState<Pending | null>(() => {
            try {
                return JSON.parse(
                    sessionStorage.getItem(storage(user)) ?? "null",
                );
            } catch {
                return null;
            }
        }),
        [busy, setBusy] = useState(false),
        [error, setError] = useState(""),
        [clock, setClock] = useState(Date.now()),
        [loading, setLoading] = useState(true);
    const lock = useRef(false);
    const save = (value: Pending | null) => {
        setPending(value);
        if (value) sessionStorage.setItem(storage(user), JSON.stringify(value));
        else sessionStorage.removeItem(storage(user));
    };
    const refresh = () => api.seats(user.tenant, show.id).then(setSeats);
    useEffect(() => {
        refresh()
            .catch((e) => setError(e.message))
            .finally(() => setLoading(false));
    }, [show.id, user.tenant]);
    useEffect(() => {
        const timer = setInterval(() => setClock(Date.now()), 1000);
        return () => clearInterval(timer);
    }, []);
    const remaining = pending?.hold
        ? Math.max(
              0,
              Math.ceil((Date.parse(pending.hold.vence) - clock) / 1000),
          )
        : 0;
    const expired = !!pending?.hold && remaining === 0;
    const selection = pending?.selected ?? selected;
    const fares =
        pending?.fares ??
        selected.map((asiento) => ({ asiento, tipo: "ADULTO" as const }));
    const total = fares.reduce(
        (sum, s) => sum + (s.tipo === "ADULTO" ? 80 : 50),
        0,
    );
    async function act(task: () => Promise<void>) {
        if (lock.current) return;
        lock.current = true;
        setBusy(true);
        setError("");
        try {
            await task();
        } catch (e) {
            setError(
                e instanceof Error
                    ? e.message
                    : "No pudimos completar la operación.",
            );
            if (e instanceof ApiError && e.status === 401) onSessionExpired();
        } finally {
            lock.current = false;
            setBusy(false);
        }
    }
    function toggle(n: number) {
        if (pending || busy) return;
        setError("");
        setSelected((old) =>
            old.includes(n)
                ? old.filter((x) => x !== n)
                : old.length < 10
                  ? [...old, n].sort((a, b) => a - b)
                  : old,
        );
    }
    async function reserve() {
        await act(async () => {
            const value = pending ?? {
                show,
                key: crypto.randomUUID(),
                selected,
                fares: selected.map((asiento) => ({
                    asiento,
                    tipo: "ADULTO" as const,
                })),
            };
            save(value);
            try {
                const hold = await api.hold(show.id, value.key, value.selected);
                save({ ...value, hold });
                if (hold.estado !== "ACTIVA")
                    setError(
                        "Esta reserva ya no está activa. Consulta tus boletos o comienza otra selección.",
                    );
            } catch (e) {
                if (e instanceof ApiError && e.status === 409) {
                    save(null);
                    setSelected([]);
                    await refresh();
                }
                throw e;
            }
        });
    }
    async function cancel() {
        await act(async () => {
            if (pending?.hold) await api.cancel(pending.key);
            save(null);
            setSelected([]);
            await refresh();
        });
    }
    async function pay() {
        await act(async () => {
            if (!pending) return;
            const value = { ...pending, attempted: true };
            save(value);
            try {
                const sale = await api.pay(value.key, value.fares);
                save(null);
                onDone(sale);
            } catch (e) {
                if (
                    e instanceof ApiError &&
                    [
                        "SALDO_INSUFICIENTE",
                        "RESERVA_NO_VIGENTE",
                        "CUENTA_INACTIVA",
                    ].includes(e.code)
                )
                    save({ ...value, attempted: false });
                throw e;
            }
        });
    }
    const stage = pending?.hold ? "02" : "01";
    return (
        <section className="checkout">
            <div className="section-heading">
                <div>
                    <button className="text-button" onClick={onBack}>
                        ← Cartelera
                    </button>
                    <p className="eyebrow">
                        TU EXPERIENCIA · PASO {stage} DE 02
                    </p>
                    <h1>
                        {pending?.hold
                            ? "Ya casi empieza la historia."
                            : "Elige tu lugar favorito."}
                    </h1>
                    <p className="muted">
                        {show.titulo} · {show.sucursal} · {show.sala}
                        <br />
                        {date(show.inicio)} · Hora de Ciudad de México
                    </p>
                </div>
                <span className="pill">
                    {pending?.hold
                        ? "Reserva temporal"
                        : "Selección de asientos"}
                </span>
            </div>
            <div className="checkout-grid">
                <div className="panel auditorium">
                    <div className="screen">PANTALLA</div>
                    {loading && (
                        <p role="status" className="muted">
                            Consultando asientos…
                        </p>
                    )}
                    <div className="seat-grid">
                        {seats.map((seat) => (
                            <button
                                key={seat.numero}
                                aria-label={`Asiento ${seat.numero}${!seat.disponible && !selection.includes(seat.numero) ? ", ocupado" : ""}`}
                                aria-pressed={selection.includes(seat.numero)}
                                disabled={!!pending || busy || !seat.disponible}
                                className={`seat ${selection.includes(seat.numero) ? "selected" : ""} ${!seat.disponible && !selection.includes(seat.numero) ? "occupied" : ""}`}
                                onClick={() => toggle(seat.numero)}
                            >
                                {seat.numero}
                            </button>
                        ))}
                    </div>
                    <div className="legend">
                        <span>
                            <i />
                            Disponible
                        </span>
                        <span>
                            <i className="chosen" />
                            Tu selección
                        </span>
                        <span>
                            <i className="taken" />
                            Ocupado
                        </span>
                    </div>
                    <p className="small muted">
                        Máximo 10 asientos por reserva. La disponibilidad se
                        confirma al reservar.
                    </p>
                </div>
                <aside className="panel order">
                    <span className="eyebrow">TU FUNCIÓN</span>
                    <h2>{show.titulo}</h2>
                    <p className="muted">
                        {selection.length
                            ? `Asientos ${selection.join(", ")}`
                            : "Selecciona tus asientos en la sala"}
                    </p>
                    {pending?.hold && (
                        <div
                            className={`timer ${expired ? "error" : ""}`}
                            role="timer"
                        >
                            {expired
                                ? "Tu reserva llegó a su límite"
                                : `Tiempo restante · ${Math.floor(remaining / 60)}:${String(remaining % 60).padStart(2, "0")}`}
                        </div>
                    )}
                    {fares.map((f) => (
                        <label className="fare" key={f.asiento}>
                            Asiento {f.asiento}
                            <select
                                aria-label={`Tarifa asiento ${f.asiento}`}
                                disabled={
                                    !pending?.hold || busy || pending.attempted
                                }
                                value={f.tipo}
                                onChange={(e) =>
                                    pending &&
                                    save({
                                        ...pending,
                                        fares: pending.fares.map((x) =>
                                            x.asiento === f.asiento
                                                ? {
                                                      ...x,
                                                      tipo: e.target
                                                          .value as Fare["tipo"],
                                                  }
                                                : x,
                                        ),
                                    })
                                }
                            >
                                <option value="ADULTO">Adulto · $80</option>
                                <option value="NINO">Niño · $50</option>
                            </select>
                        </label>
                    ))}
                    <div className="total">
                        <span>Total estimado</span>
                        <strong>{money(total)}</strong>
                    </div>
                    <p className="small muted">
                        Saldo demo: {money(user.saldoDemo)}. El servidor
                        confirma el importe final.
                    </p>
                    {error && (
                        <p className="error" role="alert">
                            {error}
                        </p>
                    )}
                    {!pending?.hold ? (
                        <button
                            className="primary full"
                            disabled={busy || selection.length === 0}
                            onClick={reserve}
                        >
                            {busy
                                ? "Reservando…"
                                : pending
                                  ? "Reintentar reserva"
                                  : "Reservar asientos"}{" "}
                            <span>→</span>
                        </button>
                    ) : (
                        <>
                            <button
                                className="primary full"
                                disabled={
                                    busy ||
                                    (expired && !pending.attempted) ||
                                    pending.hold.estado !== "ACTIVA"
                                }
                                onClick={pay}
                            >
                                {busy
                                    ? "Confirmando…"
                                    : pending.attempted
                                      ? "Reintentar el mismo pago"
                                      : "Pagar con saldo demo"}{" "}
                                <span>→</span>
                            </button>
                            {pending.attempted ? (
                                <p className="small muted">
                                    Reintentar recupera la misma operación, sin
                                    un segundo cobro. También puedes revisar Mis
                                    boletos.
                                </p>
                            ) : (
                                <button
                                    className="text-button full"
                                    disabled={busy}
                                    onClick={cancel}
                                >
                                    Liberar reserva y elegir de nuevo
                                </button>
                            )}
                        </>
                    )}
                    {pending && !pending.hold && (
                        <p className="small muted">
                            Conservamos esta solicitud para que puedas
                            reintentar sin duplicarla.
                        </p>
                    )}
                    <div className="demo-note">
                        ◇ Pago simulado. No necesitas tarjeta.
                    </div>
                </aside>
            </div>
        </section>
    );
}

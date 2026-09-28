import { useState, type FormEvent } from "react";
import { api, type Tenant, type User } from "./api";
export function Auth({
    tenant,
    onLogin,
    onBack,
}: {
    tenant: Tenant;
    onLogin: (u: User) => void;
    onBack: () => void;
}) {
    const [register, setRegister] = useState(false),
        [busy, setBusy] = useState(false),
        [error, setError] = useState(""),
        [member, setMember] = useState("");
    async function submit(e: FormEvent<HTMLFormElement>) {
        e.preventDefault();
        const form = e.currentTarget;
        const data = Object.fromEntries(new FormData(form)) as Record<
            string,
            string
        >;
        setError("");
        setBusy(true);
        try {
            let id = data.memberId?.trim();
            if (register) {
                if (new TextEncoder().encode(data.password).length > 72)
                    throw new Error(
                        "La contraseña no puede superar 72 bytes. Usa una frase más corta.",
                    );
                const result = await api.register(tenant, data);
                id = result.memberId;
                setMember(id);
                setRegister(false);
            }
            await api.login(tenant, id, data.password);
            onLogin(await api.me());
            form.reset();
        } catch (error) {
            setError(
                error instanceof Error
                    ? error.message
                    : "No pudimos iniciar sesión.",
            );
        } finally {
            setBusy(false);
        }
    }
    return (
        <section className="auth-layout">
            <div className="auth-story">
                <span className="eyebrow">TU LUGAR ESTÁ AQUÍ</span>
                <h1>
                    Más historias.
                    <br />
                    Más contigo.
                </h1>
                <p>
                    Tu cuenta reúne tus reservas y tus boletos, para que solo te
                    preocupes por disfrutar la función.
                </p>
                <div className="mini-ticket">
                    CINE-TICS <span>ADMITE UNA GRAN HISTORIA</span>
                    <b>✦</b>
                </div>
            </div>
            <div className="panel auth-form">
                <button className="text-button" onClick={onBack}>
                    ← Volver a cartelera
                </button>
                <div className="tabs">
                    <button
                        className={!register ? "active" : ""}
                        onClick={() => {
                            setRegister(false);
                            setError("");
                        }}
                    >
                        Iniciar sesión
                    </button>
                    <button
                        className={register ? "active" : ""}
                        onClick={() => {
                            setRegister(true);
                            setError("");
                        }}
                    >
                        Crear cuenta
                    </button>
                </div>
                <h2>{register ? "Nos vemos en el cine" : "Qué gusto verte"}</h2>
                <p className="muted">
                    {tenant === "CINE_TICS"
                        ? "CINE-TICS"
                        : "Cadena de demostración"}{" "}
                    · Cuenta independiente por cadena
                </p>
                {member && (
                    <div className="notice">
                        Cuenta creada. Guarda tu ID de socio:{" "}
                        <strong>{member}</strong>
                    </div>
                )}
                <form onSubmit={submit}>
                    {register ? (
                        <>
                            <div className="field-pair">
                                <label>
                                    Nombre
                                    <input
                                        name="nombre"
                                        autoComplete="given-name"
                                        required
                                        maxLength={80}
                                    />
                                </label>
                                <label>
                                    Apellidos
                                    <input
                                        name="apellidos"
                                        autoComplete="family-name"
                                        required
                                        maxLength={120}
                                    />
                                </label>
                            </div>
                            <label>
                                Dirección
                                <input
                                    name="direccion"
                                    autoComplete="street-address"
                                    required
                                    maxLength={240}
                                />
                            </label>
                            <div className="field-pair">
                                <label>
                                    Teléfono
                                    <input
                                        name="telefono"
                                        type="tel"
                                        autoComplete="tel"
                                        required
                                        pattern="[+0-9 ()\-]{7,20}"
                                        maxLength={20}
                                    />
                                </label>
                                <label>
                                    Correo
                                    <input
                                        name="correo"
                                        type="email"
                                        autoComplete="email"
                                        required
                                        maxLength={254}
                                    />
                                </label>
                            </div>
                        </>
                    ) : (
                        <label>
                            ID de socio
                            <input
                                key={member}
                                name="memberId"
                                defaultValue={member}
                                placeholder="U_…"
                                autoComplete="username"
                                required
                            />
                        </label>
                    )}
                    <label>
                        Contraseña
                        <input
                            name="password"
                            type="password"
                            autoComplete={
                                register ? "new-password" : "current-password"
                            }
                            required
                            minLength={register ? 8 : undefined}
                            maxLength={72}
                        />
                    </label>
                    {register && (
                        <p className="muted small">
                            Al menos 8 caracteres. Tu cuenta incluye $1,000 de
                            saldo ficticio para explorar esta demostración.
                        </p>
                    )}
                    {error && (
                        <p role="alert" className="error">
                            {error}
                        </p>
                    )}
                    <button className="primary full" disabled={busy}>
                        {busy
                            ? "Un momento…"
                            : register
                              ? "Crear cuenta y entrar"
                              : "Entrar a mi cuenta"}{" "}
                        <span>↗</span>
                    </button>
                </form>
                <p className="small muted">
                    Demo académica · Sin cargos reales.
                </p>
            </div>
        </section>
    );
}

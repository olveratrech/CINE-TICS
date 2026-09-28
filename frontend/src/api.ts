export type Tenant = "CINE_TICS" | "CADENA_DEMO";
export type Show = {
    id: number;
    titulo: string;
    inicio: string;
    fin: string;
    sala: string;
    sucursal: string;
};
export type Seat = { numero: number; disponible: boolean };
export type User = {
    memberId: string;
    tenant: Tenant;
    nombre: string;
    apellidos: string;
    correo: string;
    roles: string[];
    saldoDemo: number;
};
export type Hold = {
    id: string;
    estado: "ACTIVA" | "VENCIDA" | "CANCELADA" | "CONFIRMADA";
    vence: string;
    repetida: boolean;
};
export type Fare = { asiento: number; tipo: "ADULTO" | "NINO" };
export type Ticket = {
    id: string;
    funcion: number;
    asiento: number;
    tipo: Fare["tipo"];
    precio: number;
};
export type Sale = {
    venta: string;
    total: number;
    boletos: Ticket[];
    repetida: boolean;
};
const messages: Record<string, string> = {
    INVALID_CREDENTIALS:
        "El ID de socio o la contraseña no coinciden. Revisa también la cadena.",
    AUTHENTICATION_REQUIRED: "Inicia sesión para continuar.",
    SESSION_INVALID: "Tu sesión terminó. Vuelve a iniciar sesión.",
    ACCESS_DENIED:
        "No tienes permiso para realizar esta operación. Prueba iniciar sesión de nuevo.",
    INVALID_REQUEST: "Revisa los datos del formulario.",
    REGISTRATION_CONFLICT:
        "Ya existe una cuenta con ese correo en esta cadena.",
    TOO_MANY_ATTEMPTS:
        "Demasiados intentos. Espera un minuto antes de continuar.",
    NO_DISPONIBLE:
        "Alguien acaba de reservar uno de esos asientos. Actualiza la selección.",
    RESERVA_NO_VIGENTE:
        "La reserva venció o fue cancelada. Selecciona asientos de nuevo.",
    FUNCION_INICIADA: "Esta función ya comenzó.",
    SALDO_INSUFICIENTE: "Tu saldo de demostración no alcanza para esta compra.",
    CUENTA_INACTIVA: "Esta cuenta está inactiva.",
    RESERVA_CONFIRMADA:
        "Esta reserva ya tiene boletos emitidos. Consúltalos en Mis boletos.",
    NO_EXISTE: "No encontramos esa reserva o función.",
    SELECCION_DISTINTA: "La selección no coincide con la compra original.",
    DATABASE_UNAVAILABLE_RETRY_SAME_KEY:
        "No podemos confirmar el resultado ahora. Reintenta para recuperar la misma operación.",
};
export class ApiError extends Error {
    constructor(
        public status: number,
        public code: string,
    ) {
        super(
            messages[code] ??
                "No se pudo completar la operación. Inténtalo de nuevo.",
        );
    }
}
async function response<T>(url: string, options: RequestInit = {}): Promise<T> {
    let result: Response;
    try {
        result = await fetch(url, {
            signal: AbortSignal.timeout(20000),
            ...options,
            credentials: "same-origin",
            headers: { Accept: "application/json", ...options.headers },
        });
    } catch {
        throw new ApiError(0, "DATABASE_UNAVAILABLE_RETRY_SAME_KEY");
    }
    if (!result.ok) {
        const error = await result.json().catch(() => ({}));
        throw new ApiError(result.status, error.code ?? "UNKNOWN");
    }
    return result.json();
}
export async function post<T>(
    path: string,
    body: unknown,
    form = false,
): Promise<T> {
    const csrf = await response<{ token: string; headerName: string }>(
        "/api/auth/csrf",
    );
    return response<T>(path, {
        method: "POST",
        headers: {
            [csrf.headerName]: csrf.token,
            "Content-Type": form
                ? "application/x-www-form-urlencoded"
                : "application/json",
        },
        body: form ? String(body) : JSON.stringify(body),
    });
}
export const api = {
    me: () => response<User>("/api/me"),
    shows: (tenant: Tenant) => response<Show[]>(`/api/public/${tenant}/shows`),
    seats: (tenant: Tenant, id: number) =>
        response<Seat[]>(`/api/public/${tenant}/shows/${id}/seats`),
    login: (tenant: Tenant, id: string, password: string) =>
        post(
            "/api/auth/login",
            new URLSearchParams({ username: `${tenant}:${id}`, password }),
            true,
        ),
    register: (tenant: Tenant, data: Record<string, string>) =>
        post<{ memberId: string }>(`/api/public/${tenant}/register`, data),
    logout: () => post("/api/auth/logout", {}),
    hold: (show: number, key: string, seats: number[]) =>
        post<Hold>("/api/client/holds", {
            funcion: show,
            clave: key,
            asientos: seats,
        }),
    cancel: (key: string) =>
        post<Hold>(`/api/client/holds/${encodeURIComponent(key)}/cancel`, {}),
    pay: (key: string, seleccion: Fare[]) =>
        post<Sale>(`/api/client/holds/${encodeURIComponent(key)}/pay`, {
            seleccion,
        }),
    tickets: () => response<Ticket[]>("/api/client/tickets"),
};
export const money = (n: number) =>
    new Intl.NumberFormat("es-MX", {
        style: "currency",
        currency: "MXN",
        maximumFractionDigits: 2,
    }).format(n);
export const date = (s: string) =>
    new Intl.DateTimeFormat("es-MX", {
        dateStyle: "medium",
        timeStyle: "short",
        timeZone: "America/Mexico_City",
    }).format(new Date(s));

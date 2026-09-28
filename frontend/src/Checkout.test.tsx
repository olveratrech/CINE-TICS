import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, it, expect, vi } from "vitest";
import { Checkout } from "./Checkout";
import { api, ApiError, type Show, type User } from "./api";
const show: Show = {
    id: 1,
    titulo: "Película demo",
    inicio: "2030-01-01T19:00:00-06:00",
    fin: "2030-01-01T21:00:00-06:00",
    sala: "Sala 1",
    sucursal: "CU",
};
const user: User = {
    memberId: "U_test",
    tenant: "CINE_TICS",
    nombre: "Demo",
    apellidos: "Test",
    correo: "demo@example.test",
    roles: ["ROLE_CLIENTE"],
    saldoDemo: 1000,
};
afterEach(() => vi.restoreAllMocks());
function page() {
    vi.spyOn(api, "seats").mockResolvedValue([
        { numero: 1, disponible: true },
        { numero: 2, disponible: true },
        { numero: 3, disponible: false },
    ]);
    const done = vi.fn();
    render(
        <Checkout
            show={show}
            user={user}
            onBack={() => {}}
            onDone={done}
            onSessionExpired={() => {}}
        />,
    );
    return done;
}
describe("Checkout", () => {
    it("preserves the reservation key after a lost response", async () => {
        const hold = vi
            .spyOn(api, "hold")
            .mockRejectedValueOnce(
                new ApiError(0, "DATABASE_UNAVAILABLE_RETRY_SAME_KEY"),
            )
            .mockResolvedValue({
                id: "hold",
                estado: "ACTIVA",
                vence: new Date(Date.now() + 600000).toISOString(),
                repetida: true,
            });
        page();
        const ui = userEvent.setup();
        await ui.click(
            await screen.findByRole("button", { name: "Asiento 1" }),
        );
        await ui.click(
            screen.getByRole("button", { name: /Reservar asientos/ }),
        );
        await ui.click(
            await screen.findByRole("button", { name: /Reintentar reserva/ }),
        );
        await screen.findByRole("button", { name: /Pagar con saldo demo/ });
        expect(hold).toHaveBeenCalledTimes(2);
        expect(hold.mock.calls[0]).toEqual(hold.mock.calls[1]);
    });
    it("retries identical payment after an ambiguous result and locks fares", async () => {
        vi.spyOn(api, "hold").mockResolvedValue({
            id: "hold",
            estado: "ACTIVA",
            vence: new Date(Date.now() + 600000).toISOString(),
            repetida: false,
        });
        const pay = vi
            .spyOn(api, "pay")
            .mockRejectedValueOnce(
                new ApiError(0, "DATABASE_UNAVAILABLE_RETRY_SAME_KEY"),
            )
            .mockResolvedValue({
                venta: "sale",
                total: 130,
                boletos: [],
                repetida: true,
            });
        const done = page();
        const ui = userEvent.setup();
        await ui.click(
            await screen.findByRole("button", { name: "Asiento 1" }),
        );
        await ui.click(screen.getByRole("button", { name: "Asiento 2" }));
        await ui.click(
            screen.getByRole("button", { name: /Reservar asientos/ }),
        );
        await ui.selectOptions(
            await screen.findByLabelText("Tarifa asiento 2"),
            "NINO",
        );
        await ui.click(
            screen.getByRole("button", { name: /Pagar con saldo demo/ }),
        );
        expect(await screen.findByLabelText("Tarifa asiento 2")).toBeDisabled();
        await ui.click(
            await screen.findByRole("button", {
                name: /Reintentar el mismo pago/,
            }),
        );
        await waitFor(() => expect(done).toHaveBeenCalledOnce());
        expect(pay.mock.calls[0]).toEqual(pay.mock.calls[1]);
        expect(pay.mock.calls[1][1]).toEqual([
            { asiento: 1, tipo: "ADULTO" },
            { asiento: 2, tipo: "NINO" },
        ]);
        expect(sessionStorage.length).toBe(0);
    });
    it("blocks occupied seats and expired holds but allows their release", async () => {
        sessionStorage.setItem(
            "cine-hold:CINE_TICS:U_test",
            JSON.stringify({
                show,
                key: "expired",
                selected: [1],
                fares: [{ asiento: 1, tipo: "ADULTO" }],
                hold: {
                    id: "h",
                    estado: "ACTIVA",
                    vence: "2000-01-01T00:00:00Z",
                },
            }),
        );
        const cancel = vi
            .spyOn(api, "cancel")
            .mockResolvedValue({
                id: "h",
                estado: "VENCIDA",
                vence: "2000-01-01T00:00:00Z",
                repetida: false,
            });
        page();
        const ui = userEvent.setup();
        expect(
            await screen.findByRole("button", { name: "Asiento 3, ocupado" }),
        ).toBeDisabled();
        expect(
            screen.getByRole("button", { name: /Pagar con saldo demo/ }),
        ).toBeDisabled();
        await ui.click(screen.getByRole("button", { name: /Liberar reserva/ }));
        await waitFor(() => expect(cancel).toHaveBeenCalledWith("expired"));
        expect(
            await screen.findByRole("button", { name: /Reservar asientos/ }),
        ).toBeDisabled();
    });
    it("allows recovering a payment even when the local timer expired", async () => {
        sessionStorage.setItem(
            "cine-hold:CINE_TICS:U_test",
            JSON.stringify({
                show,
                key: "uncertain",
                selected: [1],
                fares: [{ asiento: 1, tipo: "ADULTO" }],
                attempted: true,
                hold: {
                    id: "h",
                    estado: "ACTIVA",
                    vence: "2000-01-01T00:00:00Z",
                },
            }),
        );
        page();
        expect(
            screen.getByRole("button", { name: /Reintentar el mismo pago/ }),
        ).toBeEnabled();
    });
});

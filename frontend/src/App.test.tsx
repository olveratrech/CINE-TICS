import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { it, expect, vi, afterEach } from "vitest";
import App from "./App";
import { api, ApiError } from "./api";
afterEach(() => vi.restoreAllMocks());
it("clears a previous ticket list before a failed history refresh", async () => {
    vi.spyOn(window, "scrollTo").mockImplementation(() => {});
    vi.spyOn(api, "me").mockResolvedValue({
        memberId: "U_test",
        tenant: "CINE_TICS",
        nombre: "Demo",
        apellidos: "Test",
        correo: "demo@example.test",
        roles: ["ROLE_CLIENTE"],
        saldoDemo: 1000,
    });
    vi.spyOn(api, "shows").mockResolvedValue([]);
    vi.spyOn(api, "tickets")
        .mockResolvedValueOnce([
            {
                id: "boleto_test",
                funcion: 1,
                asiento: 1,
                tipo: "ADULTO",
                precio: 80,
            },
        ])
        .mockRejectedValueOnce(
            new ApiError(503, "DATABASE_UNAVAILABLE_RETRY_SAME_KEY"),
        );
    render(<App />);
    const ui = userEvent.setup();
    await ui.click(await screen.findByRole("button", { name: "Mis boletos" }));
    await screen.findByText("boleto_test");
    await ui.click(screen.getByRole("button", { name: "Cartelera" }));
    await ui.click(screen.getByRole("button", { name: "Mis boletos" }));
    await screen.findByRole("alert");
    expect(screen.queryByText("boleto_test")).not.toBeInTheDocument();
});

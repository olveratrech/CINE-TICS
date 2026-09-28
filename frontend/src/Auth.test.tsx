import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { it, expect, vi, afterEach } from "vitest";
import { Auth } from "./Auth";
import { api } from "./api";
afterEach(() => vi.restoreAllMocks());
it("registers the profile then logs in using the returned member ID", async () => {
    const register = vi
        .spyOn(api, "register")
        .mockResolvedValue({ memberId: "U_new" });
    const login = vi.spyOn(api, "login").mockResolvedValue({});
    vi.spyOn(api, "me").mockResolvedValue({
        memberId: "U_new",
        tenant: "CINE_TICS",
        nombre: "Demo",
        apellidos: "Test",
        correo: "a@example.test",
        roles: ["ROLE_CLIENTE"],
        saldoDemo: 1000,
    });
    const done = vi.fn();
    render(<Auth tenant="CINE_TICS" onLogin={done} onBack={() => {}} />);
    const ui = userEvent.setup();
    await ui.click(screen.getByRole("button", { name: "Crear cuenta" }));
    for (const [label, value] of [
        ["Nombre", "Demo"],
        ["Apellidos", "Test"],
        ["Dirección", "Dirección ficticia"],
        ["Teléfono", "5555555555"],
        ["Correo", "a@example.test"],
        ["Contraseña", "Ficticia-2026!"],
    ])
        await ui.type(screen.getByLabelText(label, { exact: true }), value);
    await ui.click(
        screen.getByRole("button", { name: /Crear cuenta y entrar/ }),
    );
    await waitFor(() => expect(done).toHaveBeenCalledOnce());
    expect(register).toHaveBeenCalledOnce();
    expect(login).toHaveBeenCalledWith("CINE_TICS", "U_new", "Ficticia-2026!");
    expect(register.mock.calls[0][1]).not.toHaveProperty("role");
});

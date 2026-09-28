package com.penta.cinetics.identidad;

import jakarta.validation.constraints.*;

public record RegistroCliente(
    @NotBlank @Size(max=80) String nombre,
    @NotBlank @Size(max=120) String apellidos,
    @NotBlank @Size(max=240) String direccion,
    @NotBlank @Pattern(regexp="[+0-9 ()-]{7,20}") String telefono,
    @NotBlank @Email @Size(max=254) String correo,
    @NotBlank @Size(min=8,max=72) String password) {
    @Override public String toString() { return "RegistroCliente[datos privados]"; }
}

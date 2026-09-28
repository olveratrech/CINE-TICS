package com.penta.cinetics.ventas.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Immutable purchase line; quantity is independent of catalog inventory. */
public record LineaCarrito(String codigo, String nombre, String categoria,
        BigDecimal precioUnitario, int cantidad) {
    public LineaCarrito {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El producto debe tener código.");
        }
        Objects.requireNonNull(nombre);
        Objects.requireNonNull(categoria);
        Objects.requireNonNull(precioUnitario);
        if (cantidad <= 0 || precioUnitario.signum() < 0) {
            throw new IllegalArgumentException("Cantidad positiva y precio no negativo requeridos.");
        }
        // Catalog prices are expressed in MXN cents; reject silent loss of precision.
        precioUnitario = precioUnitario.setScale(2, RoundingMode.UNNECESSARY);
    }

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}

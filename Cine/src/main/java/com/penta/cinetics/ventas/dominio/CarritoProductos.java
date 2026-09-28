package com.penta.cinetics.ventas.dominio;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory cart. Stock must be checked again atomically at checkout. */
public final class CarritoProductos {
    private final Map<String, LineaCarrito> lineas = new LinkedHashMap<>();

    public void agregar(String codigo, String nombre, String categoria,
            BigDecimal precio, int cantidad, int stockDisponible) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        LineaCarrito anterior = lineas.get(codigo);
        long acumulada = (long) cantidad + (anterior == null ? 0 : anterior.cantidad());
        validarStock(acumulada, stockDisponible);
        if (anterior != null && anterior.precioUnitario().compareTo(precio) != 0) {
            throw new IllegalArgumentException("El precio cambió; elimina el producto y vuelve a agregarlo.");
        }
        lineas.put(codigo, new LineaCarrito(codigo, nombre, categoria, precio, (int) acumulada));
    }

    public void cambiarCantidad(String codigo, int cantidad, int stockDisponible) {
        LineaCarrito anterior = lineas.get(codigo);
        if (anterior == null) {
            throw new IllegalArgumentException("El producto no está en el carrito.");
        }
        validarStock(cantidad, stockDisponible);
        lineas.put(codigo, new LineaCarrito(codigo, anterior.nombre(), anterior.categoria(),
                anterior.precioUnitario(), cantidad));
    }

    private void validarStock(long cantidad, int stock) {
        if (cantidad <= 0 || stock < 0 || cantidad > stock) {
            throw new IllegalArgumentException("Cantidad inválida o stock insuficiente.");
        }
    }

    public List<LineaCarrito> lineas() {
        return List.copyOf(lineas.values());
    }

    public BigDecimal total() {
        return lineas.values().stream().map(LineaCarrito::subtotal)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    public void eliminar(String codigo) {
        lineas.remove(codigo);
    }

    public void vaciar() {
        lineas.clear();
    }
}

package com.penta.cinetics.ventas.aplicacion;

import java.math.BigDecimal;
import java.sql.SQLException;

/** Persistence port for the transitional console; SQL failures remain explicit to callers. */
public interface Compras {
    enum Estado { PROCESSING, APPROVED, INSUFFICIENT_FUNDS, OUT_OF_STOCK,
        INACTIVE_ACCOUNT, PRODUCT_NOT_FOUND, INVALID_TOTAL }
    record Resultado(String id, Estado estado, BigDecimal total, boolean repetida) {
        public Resultado { total = total.setScale(2, java.math.RoundingMode.UNNECESSARY); }
    }
    Resultado comprar(SolicitudCompra solicitud) throws SQLException;
}

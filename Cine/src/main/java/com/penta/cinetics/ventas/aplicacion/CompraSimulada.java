package com.penta.cinetics.ventas.aplicacion;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Authorizes a demo payment before allowing the caller to record a sale.
 * This boundary does not provide durable transactions or retry/idempotency guarantees.
 */
public final class CompraSimulada {
    public enum Estado { APROBADA, CUENTA_INACTIVA, SALDO_INSUFICIENTE, IMPORTE_INVALIDO }

    public record Resultado(Estado estado, BigDecimal saldoRestante) {
        public boolean aprobada() { return estado == Estado.APROBADA; }
    }

    @FunctionalInterface
    public interface Confirmacion {
        void registrar(BigDecimal saldoRestante) throws IOException, InterruptedException;
    }

    public Resultado ejecutar(BigDecimal total, BigDecimal saldo, boolean cuentaActiva,
            Confirmacion confirmacion) throws IOException, InterruptedException {
        Objects.requireNonNull(total);
        Objects.requireNonNull(saldo);
        Objects.requireNonNull(confirmacion);
        if (total.signum() <= 0 || saldo.signum() < 0) {
            return new Resultado(Estado.IMPORTE_INVALIDO, saldo);
        }
        try {
            total = total.setScale(2, RoundingMode.UNNECESSARY);
            saldo = saldo.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException invalidPrecision) {
            return new Resultado(Estado.IMPORTE_INVALIDO, saldo);
        }
        if (!cuentaActiva) {
            return new Resultado(Estado.CUENTA_INACTIVA, saldo);
        }
        if (saldo.compareTo(total) < 0) {
            return new Resultado(Estado.SALDO_INSUFICIENTE, saldo);
        }
        BigDecimal restante = saldo.subtract(total);
        confirmacion.registrar(restante);
        return new Resultado(Estado.APROBADA, restante);
    }
}

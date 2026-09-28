package com.penta.cinetics.boletos.aplicacion;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

/** One completed purchase per hold; its key is also the payment retry identity. */
public interface VentaBoletos {
    enum Tipo {
        ADULTO("80.00"), NINO("50.00");
        private final BigDecimal precio;
        Tipo(String precio) { this.precio = new BigDecimal(precio); }
        public BigDecimal precio() { return precio; }
    }
    record Seleccion(int asiento, Tipo tipo) {
        public Seleccion {
            if (asiento <= 0 || tipo == null) throw new IllegalArgumentException("Asiento o tarifa inválidos.");
        }
    }
    record Solicitud(String cliente, String reserva, List<Seleccion> seleccion) {
        public Solicitud {
            if (cliente == null || !cliente.matches("[A-Za-z0-9_-]{1,40}")
                    || reserva == null || !reserva.matches("[A-Za-z0-9_-]{1,64}")
                    || seleccion == null || seleccion.isEmpty() || seleccion.size()>10
                    || seleccion.stream().anyMatch(s -> s == null)) throw new IllegalArgumentException("Compra inválida.");
            seleccion = seleccion.stream().sorted(Comparator.comparingInt(Seleccion::asiento)).toList();
            if (seleccion.stream().map(Seleccion::asiento).distinct().count()!=seleccion.size())
                throw new IllegalArgumentException("Asiento repetido.");
        }
    }
    record Boleto(String id, long funcion, int asiento, Tipo tipo, BigDecimal precio) {
        public Boleto { precio = precio.setScale(2, java.math.RoundingMode.UNNECESSARY); }
    }
    record Resultado(String venta, BigDecimal total, List<Boleto> boletos, boolean repetida) {
        public Resultado {
            total = total.setScale(2, java.math.RoundingMode.UNNECESSARY);
            boletos = List.copyOf(boletos);
        }
    }
    enum Motivo { NO_EXISTE, RESERVA_NO_VIGENTE, SELECCION_DISTINTA, SALDO_INSUFICIENTE, CUENTA_INACTIVA }
    final class Rechazo extends RuntimeException {
        private final Motivo motivo;
        public Rechazo(Motivo motivo) { super(motivo.name()); this.motivo = motivo; }
        public Motivo motivo() { return motivo; }
    }
    Resultado comprar(Solicitud solicitud) throws SQLException;
    List<Boleto> historial(String cliente) throws SQLException;
}

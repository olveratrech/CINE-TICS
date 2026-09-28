package com.penta.cinetics.reservas.aplicacion;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.TreeSet;

/** Holds are temporary, not tickets or payments. Bound to one chain by its adapter. */
public interface Reservas {
    enum Estado { ACTIVA, VENCIDA, CANCELADA, CONFIRMADA }
    record Solicitud(String cliente, long funcion, String clave, List<Integer> asientos) {
        public Solicitud {
            if (cliente == null || !cliente.matches("[A-Za-z0-9_-]{1,40}") || funcion <= 0
                    || clave == null || !clave.matches("[A-Za-z0-9_-]{1,64}")
                    || asientos == null || asientos.isEmpty() || asientos.size() > 10
                    || asientos.stream().anyMatch(n -> n == null || n <= 0)) {
                throw new IllegalArgumentException("Selecciona de uno a diez asientos válidos.");
            }
            var sorted = new TreeSet<>(asientos);
            if (sorted.size() != asientos.size()) throw new IllegalArgumentException("Asiento repetido.");
            asientos = List.copyOf(sorted);
        }
    }
    record Resultado(String id, Estado estado, OffsetDateTime vence, boolean repetida) {}
    record Asiento(int numero, boolean disponible) {}
    enum Motivo { NO_EXISTE, NO_DISPONIBLE, FUNCION_INICIADA, CUENTA_INACTIVA, CLAVE_REUTILIZADA, HORARIO_SOLAPADO, RESERVA_CONFIRMADA }
    final class Rechazo extends RuntimeException {
        private final Motivo motivo;
        public Rechazo(Motivo motivo) { super(motivo.name()); this.motivo = motivo; }
        public Motivo motivo() { return motivo; }
    }
    Resultado reservar(Solicitud solicitud) throws SQLException;
    Resultado cancelar(String cliente, String clave) throws SQLException;
    List<Asiento> disponibilidad(long funcion) throws SQLException;
    void crearFuncion(long id, long sala, String titulo, OffsetDateTime inicio, OffsetDateTime fin) throws SQLException;
}

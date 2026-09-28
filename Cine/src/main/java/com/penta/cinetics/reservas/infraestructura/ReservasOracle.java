package com.penta.cinetics.reservas.infraestructura;

import com.penta.cinetics.reservas.aplicacion.Reservas;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** A screening lock serializes allocation/cancellation; a room lock serializes scheduling.
 * All writes must use this adapter. Direct administrative SQL can bypass overlap rules.
 * Each instance connects to exactly one PDB; HTTP tenant authorization is a future layer.
 */
public final class ReservasOracle implements Reservas {
    @FunctionalInterface public interface Conexion { Connection abrir() throws SQLException; }
    private final Conexion conexiones;
    public ReservasOracle(Conexion conexiones) { this.conexiones = conexiones; }
    private static final String HOLD_QUERY = "select id,screening_id,seat_selection,expires_at,"
        + "case when exists (select 1 from CINE_OWNER.ticket_sales v where v.hold_id=seat_holds.id) then 'CONFIRMADA' when cancelled=1 then 'CANCELADA' when expires_at<=systimestamp then 'VENCIDA' else 'ACTIVA' end "
        + "from CINE_OWNER.seat_holds where customer_id=? and request_key=?";

    @FunctionalInterface private interface Trabajo<T> { T ejecutar(Connection c) throws SQLException; }
    private <T> T transaccion(Trabajo<T> trabajo) throws SQLException {
        try (Connection c = conexiones.abrir()) {
            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                T result = trabajo.ejecutar(c);
                c.commit();
                return result;
            } catch (SQLException | RuntimeException error) {
                try { c.rollback(); } catch (SQLException rollback) { error.addSuppressed(rollback); }
                throw error;
            }
        }
    }
    private PreparedStatement preparar(Connection c, String sql, Object... values) throws SQLException {
        PreparedStatement s = c.prepareStatement(sql);
        try {
            s.setQueryTimeout(20);
            for (int i=0; i<values.length; i++) s.setObject(i+1,values[i]);
            return s;
        } catch (SQLException error) { s.close(); throw error; }
    }
    private void ejecutar(Connection c, String sql, Object... values) throws SQLException {
        try (var s = preparar(c,sql,values)) { s.executeUpdate(); }
    }
    private void bloquearFuncion(Connection c, long id) throws SQLException {
        try (var s = preparar(c,"select id from CINE_OWNER.screenings where id=? for update wait 15",id);
             var r = s.executeQuery()) {
            if (!r.next()) throw new Rechazo(Motivo.NO_EXISTE);
        }
    }
    private Resultado existente(Connection c, Solicitud request) throws SQLException {
        try (var s = preparar(c,HOLD_QUERY,request.cliente(),request.clave()); var r = s.executeQuery()) {
            if (!r.next()) return null;
            if (r.getLong(2) != request.funcion() || !r.getString(3).equals(seleccion(request)))
                throw new Rechazo(Motivo.CLAVE_REUTILIZADA);
            return resultado(r,true);
        }
    }
    private static String seleccion(Solicitud request) {
        return request.asientos().stream().map(String::valueOf).collect(Collectors.joining(","));
    }
    private Resultado resultado(ResultSet r, boolean repetida) throws SQLException {
        return new Resultado(r.getString(1),Estado.valueOf(r.getString(5)),r.getObject(4,OffsetDateTime.class),repetida);
    }

    @Override public Resultado reservar(Solicitud request) throws SQLException {
        return transaccion(c -> {
            bloquearFuncion(c,request.funcion());
            Resultado previous = existente(c,request);
            if (previous != null) return previous;
            // Read the database clock AFTER acquiring the lock, including time spent waiting.
            OffsetDateTime expiry;
            try (var s = preparar(c,"select least(starts_at,systimestamp+interval '10' minute) "
                    + "from CINE_OWNER.screenings where id=? and starts_at>systimestamp",request.funcion());
                 var r = s.executeQuery()) {
                if (!r.next()) throw new Rechazo(Motivo.FUNCION_INICIADA);
                expiry = r.getObject(1,OffsetDateTime.class);
            }
            try (var s = preparar(c,"select active from CINE_OWNER.demo_wallets where customer_id=?",request.cliente());
                 var r = s.executeQuery()) {
                if (!r.next() || r.getInt(1)!=1) throw new Rechazo(Motivo.CUENTA_INACTIVA);
            }
            for (int seat : request.asientos()) {
                try (var s = preparar(c,"select case when not exists (select 1 from CINE_OWNER.tickets t where t.screening_id=s.screening_id and t.seat_number=s.seat_number) and (h.id is null or h.cancelled=1 or h.expires_at<=systimestamp) "
                        + "then 1 else 0 end from CINE_OWNER.screening_seats s left join CINE_OWNER.seat_holds h on h.id=s.hold_id "
                        + "where s.screening_id=? and s.seat_number=?",request.funcion(),seat);
                     var r = s.executeQuery()) {
                    if (!r.next() || r.getInt(1)!=1) throw new Rechazo(Motivo.NO_DISPONIBLE);
                }
            }
            String id = UUID.randomUUID().toString();
            try {
                ejecutar(c,"insert into CINE_OWNER.seat_holds(id,screening_id,customer_id,request_key,seat_selection,expires_at) "
                    + "values (?,?,?,?,?,?)",id,request.funcion(),request.cliente(),request.clave(),seleccion(request),expiry);
            } catch (SQLException duplicate) {
                if (duplicate.getErrorCode()!=1) throw duplicate;
                c.rollback();
                Resultado winner = existente(c,request);
                if (winner == null) throw duplicate;
                return winner;
            }
            for (int seat : request.asientos()) {
                ejecutar(c,"update CINE_OWNER.screening_seats set hold_id=? where screening_id=? and seat_number=?",id,request.funcion(),seat);
            }
            Resultado saved = existente(c,request);
            return new Resultado(saved.id(),saved.estado(),saved.vence(),false);
        });
    }

    @Override public Resultado cancelar(String cliente, String clave) throws SQLException {
        return transaccion(c -> {
            long show;
            try (var s = preparar(c,HOLD_QUERY,cliente,clave); var r = s.executeQuery()) {
                if (!r.next()) throw new Rechazo(Motivo.NO_EXISTE);
                show = r.getLong(2);
            }
            bloquearFuncion(c,show);
            try (var s = preparar(c,HOLD_QUERY,cliente,clave); var r = s.executeQuery()) {
                if (!r.next()) throw new Rechazo(Motivo.NO_EXISTE);
                if (Estado.CONFIRMADA.name().equals(r.getString(5))) throw new Rechazo(Motivo.RESERVA_CONFIRMADA);
            }
            // Do not clear seat pointers: an expired hold may already have been replaced.
            ejecutar(c,"update CINE_OWNER.seat_holds set cancelled=1 where customer_id=? and request_key=? "
                + "and cancelled=0 and expires_at>systimestamp",cliente,clave);
            try (var s = preparar(c,HOLD_QUERY,cliente,clave); var r = s.executeQuery()) {
                if (!r.next()) throw new Rechazo(Motivo.NO_EXISTE);
                return resultado(r,false);
            }
        });
    }

    @Override public List<Asiento> disponibilidad(long funcion) throws SQLException {
        try (Connection c = conexiones.abrir();
             var s = preparar(c,"select s.seat_number,case when f.starts_at>systimestamp and not exists (select 1 from CINE_OWNER.tickets t where t.screening_id=s.screening_id and t.seat_number=s.seat_number) and "
                + "(h.id is null or h.cancelled=1 or h.expires_at<=systimestamp) then 1 else 0 end "
                + "from CINE_OWNER.screening_seats s join CINE_OWNER.screenings f on f.id=s.screening_id "
                + "left join CINE_OWNER.seat_holds h on h.id=s.hold_id where s.screening_id=? order by s.seat_number",funcion);
             var r = s.executeQuery()) {
            var result = new ArrayList<Asiento>();
            while (r.next()) result.add(new Asiento(r.getInt(1),r.getInt(2)==1));
            if (result.isEmpty()) throw new Rechazo(Motivo.NO_EXISTE);
            return List.copyOf(result);
        }
    }

    @Override public void crearFuncion(long id, long sala, String titulo, OffsetDateTime inicio, OffsetDateTime fin) throws SQLException {
        if (id<=0 || sala<=0 || titulo==null || titulo.isBlank() || titulo.length()>160
                || inicio==null || fin==null || !fin.isAfter(inicio)) throw new IllegalArgumentException("Función inválida.");
        transaccion(c -> {
            int capacity;
            try (var s = preparar(c,"select capacity from CINE_OWNER.auditoriums where id=? for update wait 15",sala);
                 var r = s.executeQuery()) {
                if (!r.next()) throw new Rechazo(Motivo.NO_EXISTE);
                capacity = r.getInt(1);
            }
            try (var s = preparar(c,"select count(*) from CINE_OWNER.screenings where auditorium_id=? and starts_at<? and ends_at>?",sala,fin,inicio);
                 var r = s.executeQuery()) {
                r.next();
                if (r.getInt(1)>0) throw new Rechazo(Motivo.HORARIO_SOLAPADO);
            }
            ejecutar(c,"insert into CINE_OWNER.screenings(id,auditorium_id,title,starts_at,ends_at) values (?,?,?,?,?)",id,sala,titulo,inicio,fin);
            try (var s = preparar(c,"insert into CINE_OWNER.screening_seats(screening_id,seat_number) values (?,?)")) {
                for (int seat=1;seat<=capacity;seat++) {
                    s.setLong(1,id); s.setInt(2,seat); s.addBatch();
                }
                s.executeBatch();
            }
            return null;
        });
    }
}

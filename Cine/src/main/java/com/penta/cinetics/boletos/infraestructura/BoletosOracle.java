package com.penta.cinetics.boletos.infraestructura;

import com.penta.cinetics.boletos.aplicacion.VentaBoletos;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Serializes with reservation/cancellation using the same screening lock, then wallet lock. */
public final class BoletosOracle implements VentaBoletos {
    @FunctionalInterface public interface Conexion { Connection abrir() throws SQLException; }
    private final Conexion conexiones;
    public BoletosOracle(Conexion conexiones) { this.conexiones = conexiones; }

    private PreparedStatement preparar(Connection c, String sql, Object... values) throws SQLException {
        var s = c.prepareStatement(sql);
        try {
            s.setQueryTimeout(20);
            for (int i=0;i<values.length;i++) s.setObject(i+1,values[i]);
            return s;
        } catch (SQLException error) { s.close(); throw error; }
    }
    private void ejecutar(Connection c, String sql, Object... values) throws SQLException {
        try (var s = preparar(c,sql,values)) { s.executeUpdate(); }
    }
    @Override public Resultado comprar(Solicitud request) throws SQLException {
        try (Connection c = conexiones.abrir()) {
            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                long show;
                String hold;
                try (var s = preparar(c,"select id,screening_id from CINE_OWNER.seat_holds where customer_id=? and request_key=?",request.cliente(),request.reserva());
                     var r = s.executeQuery()) {
                    if (!r.next()) throw new Rechazo(Motivo.NO_EXISTE);
                    hold = r.getString(1); show = r.getLong(2);
                }
                try (var s = preparar(c,"select id from CINE_OWNER.screenings where id=? for update wait 15",show);
                     var r = s.executeQuery()) {
                    if (!r.next()) throw new Rechazo(Motivo.NO_EXISTE);
                }
                Resultado previous = existente(c,hold,request);
                if (previous != null) { c.commit(); return previous; }
                BigDecimal balance;
                try (var s = preparar(c,"select balance,active from CINE_OWNER.demo_wallets where customer_id=? for update wait 15",request.cliente());
                     var r = s.executeQuery()) {
                    if (!r.next() || r.getInt(2)!=1) throw new Rechazo(Motivo.CUENTA_INACTIVA);
                    balance = r.getBigDecimal(1);
                }
                // Check the database clock after BOTH locks, not before a potentially long wait.
                try (var s = preparar(c,"select h.seat_selection from CINE_OWNER.seat_holds h "
                        + "join CINE_OWNER.screenings f on f.id=h.screening_id "
                        + "where h.id=? and h.cancelled=0 and h.expires_at>systimestamp and f.starts_at>systimestamp",hold);
                     var r = s.executeQuery()) {
                    if (!r.next()) throw new Rechazo(Motivo.RESERVA_NO_VIGENTE);
                    String seats = request.seleccion().stream().map(x->String.valueOf(x.asiento())).collect(Collectors.joining(","));
                    if (!seats.equals(r.getString(1))) throw new Rechazo(Motivo.SELECCION_DISTINTA);
                }
                for (var item : request.seleccion()) {
                    try (var s = preparar(c,"select hold_id from CINE_OWNER.screening_seats where screening_id=? and seat_number=?",show,item.asiento());
                         var r = s.executeQuery()) {
                        if (!r.next() || !hold.equals(r.getString(1))) throw new Rechazo(Motivo.RESERVA_NO_VIGENTE);
                    }
                }
                BigDecimal total = request.seleccion().stream().map(x->x.tipo().precio()).reduce(new BigDecimal("0.00"),BigDecimal::add);
                if (balance.compareTo(total)<0) throw new Rechazo(Motivo.SALDO_INSUFICIENTE);
                String sale = UUID.randomUUID().toString();
                ejecutar(c,"insert into CINE_OWNER.ticket_sales(id,hold_id,customer_id,screening_id,selection_signature,total) values (?,?,?,?,?,?)",
                    sale,hold,request.cliente(),show,firma(request),total);
                ejecutar(c,"update CINE_OWNER.demo_wallets set balance=balance-? where customer_id=?",total,request.cliente());
                ejecutar(c,"insert into CINE_OWNER.ticket_payments(sale_id,amount) values (?,?)",sale,total);
                for (var item : request.seleccion()) {
                    ejecutar(c,"insert into CINE_OWNER.tickets(id,sale_id,screening_id,seat_number,fare_type,price) values (?,?,?,?,?,?)",
                        UUID.randomUUID().toString(),sale,show,item.asiento(),item.tipo().name(),item.tipo().precio());
                }
                var result = new Resultado(sale,total,boletos(c,"t.sale_id=?",sale),false);
                c.commit();
                return result;
            } catch (SQLException | RuntimeException error) {
                try { c.rollback(); } catch (SQLException rollback) { error.addSuppressed(rollback); }
                throw error;
            }
        }
    }
    private static String firma(Solicitud r) {
        return r.seleccion().stream().map(x->x.asiento()+":"+x.tipo().name()).collect(Collectors.joining(","));
    }
    private Resultado existente(Connection c, String hold, Solicitud request) throws SQLException {
        try (var s = preparar(c,"select id,total,selection_signature from CINE_OWNER.ticket_sales where hold_id=?",hold);
             var r = s.executeQuery()) {
            if (!r.next()) return null;
            if (!firma(request).equals(r.getString(3))) throw new Rechazo(Motivo.SELECCION_DISTINTA);
            return new Resultado(r.getString(1),r.getBigDecimal(2),boletos(c,"t.sale_id=?",r.getString(1)),true);
        }
    }
    private List<Boleto> boletos(Connection c, String predicate, String value) throws SQLException {
        // Predicate is internal only; caller-supplied values always use bindings.
        try (var s = preparar(c,"select t.id,t.screening_id,t.seat_number,t.fare_type,t.price from CINE_OWNER.tickets t "
                + "join CINE_OWNER.ticket_sales v on v.id=t.sale_id where " + predicate + " order by v.created_at,t.seat_number",value);
             var r = s.executeQuery()) {
            var result = new ArrayList<Boleto>();
            while (r.next()) result.add(new Boleto(r.getString(1),r.getLong(2),r.getInt(3),Tipo.valueOf(r.getString(4)),r.getBigDecimal(5)));
            return List.copyOf(result);
        }
    }
    @Override public List<Boleto> historial(String cliente) throws SQLException {
        try (Connection c = conexiones.abrir()) { return boletos(c,"v.customer_id=?",cliente); }
    }
}

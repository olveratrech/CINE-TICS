package com.penta.cinetics.ventas.infraestructura;

import com.penta.cinetics.ventas.aplicacion.Compras;
import com.penta.cinetics.ventas.aplicacion.SolicitudCompra;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** One instance is bound to one PDB. No tenant identifiers are accepted from SQL input. */
public final class ComprasOracle implements Compras {
    @FunctionalInterface
    public interface Conexion { Connection abrir() throws SQLException; }
    private final Conexion conexiones;

    public ComprasOracle(Conexion conexiones) { this.conexiones = conexiones; }

    @Override
    public Resultado comprar(SolicitudCompra request) throws SQLException {
        try (Connection c = conexiones.abrir()) {
            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                Resultado existing = existente(c, request);
                if (existing != null) return existing;
                String id = UUID.randomUUID().toString();
                try (PreparedStatement s = c.prepareStatement("insert into CINE_OWNER.purchase_orders "
                        + "(id,customer_id,request_key,request_hash,branch_id,status) values (?,?,?,?,?,'PROCESSING')")) {
                    s.setString(1,id); s.setString(2,request.cliente()); s.setString(3,request.clave());
                    s.setString(4,request.huella()); s.setLong(5,request.sucursal());
                    s.setQueryTimeout(30);
                    try { s.executeUpdate(); }
                    catch (SQLException duplicate) {
                        if (duplicate.getErrorCode() != 1) throw duplicate;
                        c.rollback(); // Unique-key wait finished: the winning transaction is now visible.
                        Resultado winner = existente(c, request);
                        if (winner == null) throw duplicate;
                        return winner;
                    }
                }
                Savepoint orderCreated = c.setSavepoint();
                BigDecimal balance;
                boolean active;
                try (PreparedStatement s = c.prepareStatement("select balance,active from CINE_OWNER.demo_wallets "
                        + "where customer_id=? for update wait 15")) {
                    s.setString(1,request.cliente());
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) throw new SQLException("Unknown demo wallet");
                        balance = r.getBigDecimal(1); active = r.getInt(2) == 1;
                    }
                }
                if (!active) return rechazar(c,orderCreated,id,Estado.INACTIVE_ACCOUNT);
                record PricedItem(long product, int quantity, BigDecimal price) {}
                List<PricedItem> priced = new ArrayList<>();
                BigDecimal total = new BigDecimal("0.00");
                // Canonical ascending product IDs ensure all checkouts acquire locks in the same order.
                for (var item : request.items()) {
                    try (PreparedStatement s = c.prepareStatement("select p.price,i.quantity from CINE_OWNER.products p "
                            + "join CINE_OWNER.inventory i on i.product_id=p.id "
                            + "where i.branch_id=? and p.id=? for update of p.price,i.quantity wait 15")) {
                        s.setLong(1,request.sucursal()); s.setLong(2,item.producto());
                        try (ResultSet r = s.executeQuery()) {
                            if (!r.next()) return rechazar(c,orderCreated,id,Estado.PRODUCT_NOT_FOUND);
                            if (r.getLong(2) < item.cantidad()) return rechazar(c,orderCreated,id,Estado.OUT_OF_STOCK);
                            BigDecimal price = r.getBigDecimal(1);
                            total = total.add(price.multiply(BigDecimal.valueOf(item.cantidad())));
                            priced.add(new PricedItem(item.producto(),item.cantidad(),price));
                        }
                    }
                }
                if (total.signum() <= 0) return rechazar(c,orderCreated,id,Estado.INVALID_TOTAL);
                if (balance.compareTo(total) < 0) return rechazar(c,orderCreated,id,Estado.INSUFFICIENT_FUNDS);
                for (var item : priced) {
                    try (PreparedStatement s = c.prepareStatement("update CINE_OWNER.inventory set quantity=quantity-? "
                            + "where branch_id=? and product_id=? and quantity>=?")) {
                        s.setInt(1,item.quantity()); s.setLong(2,request.sucursal());
                        s.setLong(3,item.product()); s.setInt(4,item.quantity());
                        if (s.executeUpdate()!=1) throw new SQLException("Inventory changed unexpectedly");
                    }
                    try (PreparedStatement s = c.prepareStatement("insert into CINE_OWNER.purchase_lines "
                            + "(order_id,product_id,quantity,unit_price) values (?,?,?,?)")) {
                        s.setString(1,id); s.setLong(2,item.product()); s.setInt(3,item.quantity());
                        s.setBigDecimal(4,item.price()); s.executeUpdate();
                    }
                }
                try (PreparedStatement s = c.prepareStatement("update CINE_OWNER.demo_wallets set balance=balance-? where customer_id=?")) {
                    s.setBigDecimal(1,total); s.setString(2,request.cliente()); s.executeUpdate();
                }
                try (PreparedStatement s = c.prepareStatement("insert into CINE_OWNER.demo_payments(order_id,amount) values (?,?)")) {
                    s.setString(1,id); s.setBigDecimal(2,total); s.executeUpdate();
                }
                finalizar(c,id,Estado.APPROVED,total);
                c.commit();
                return new Resultado(id,Estado.APPROVED,total,false);
            } catch (SQLException | RuntimeException error) {
                try { c.rollback(); } catch (SQLException rollback) { error.addSuppressed(rollback); }
                throw error;
            }
        }
    }

    private Resultado existente(Connection c, SolicitudCompra request) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("select id,status,total,request_hash from CINE_OWNER.purchase_orders "
                + "where customer_id=? and request_key=?")) {
            s.setString(1,request.cliente()); s.setString(2,request.clave());
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) return null;
                if (!r.getString(4).equals(request.huella())) {
                    throw new IllegalArgumentException("La clave ya identifica una compra diferente.");
                }
                Estado status = Estado.valueOf(r.getString(2));
                if (status == Estado.PROCESSING) throw new SQLException("Unexpected unfinished persisted order");
                return new Resultado(r.getString(1),status,r.getBigDecimal(3),true);
            }
        }
    }

    private Resultado rechazar(Connection c, Savepoint savepoint, String id, Estado status) throws SQLException {
        c.rollback(savepoint);
        finalizar(c,id,status,new BigDecimal("0.00"));
        c.commit();
        return new Resultado(id,status,new BigDecimal("0.00"),false);
    }

    private void finalizar(Connection c, String id, Estado status, BigDecimal total) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("update CINE_OWNER.purchase_orders set status=?,total=? where id=?")) {
            s.setString(1,status.name()); s.setBigDecimal(2,total); s.setString(3,id); s.executeUpdate();
        }
    }
}

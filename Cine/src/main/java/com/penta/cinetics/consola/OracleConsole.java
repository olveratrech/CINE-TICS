package com.penta.cinetics.consola;

import com.penta.cinetics.ventas.aplicacion.Compras;
import com.penta.cinetics.ventas.aplicacion.SolicitudCompra;
import com.penta.cinetics.ventas.infraestructura.ComprasOracle;
import java.sql.*;
import java.util.ArrayList;

/** Explicit demo entrypoint; identity is fixed to a synthetic wallet, not authenticated. */
public final class OracleConsole {
    public static void main(String[] args) throws Exception {
        String url = System.getenv("CINE_JDBC_URL");
        String password = System.getenv("CINE_APP_PASSWORD");
        if (url == null || password == null) throw new IllegalStateException("Ejecuta mediante infra/oracle/console.py.");
        ComprasOracle.Conexion connections = () -> DriverManager.getConnection(url,"CINE_APP",password);
        String action = args.length == 0 ? "catalog" : args[0];
        if (action.equals("buy")) {
            if (args.length < 4 || args.length % 2 != 0) {
                throw new IllegalArgumentException("Uso: buy clave producto cantidad [producto cantidad ...]");
            }
            var items = new ArrayList<SolicitudCompra.Item>();
            for (int i=2;i<args.length;i+=2) items.add(new SolicitudCompra.Item(Long.parseLong(args[i]),Integer.parseInt(args[i+1])));
            var request = new SolicitudCompra("DEMO",1,args[1],items);
            try {
                var result = new ComprasOracle(connections).comprar(request);
                String message = switch(result.estado()) {
                    case APPROVED -> "Compra simulada aprobada";
                    case INSUFFICIENT_FUNDS -> "Saldo insuficiente";
                    case OUT_OF_STOCK -> "Existencias insuficientes";
                    case INACTIVE_ACCOUNT -> "Cuenta de demostración inactiva";
                    case PRODUCT_NOT_FOUND -> "Producto no disponible en la sucursal";
                    default -> "Importe inválido";
                };
                System.out.println(message + ". Pedido: " + result.id());
                if (result.estado()==Compras.Estado.APPROVED) System.out.println("Total MXN: " + result.total());
                if (result.repetida()) System.out.println("Solicitud ya procesada: se recuperó su resultado, sin un nuevo cargo.");
            } catch (SQLException error) {
                System.err.println("No se pudo confirmar el resultado. Reintenta con la misma clave: " + request.clave());
                throw error;
            }
        } else if (action.equals("catalog") || action.equals("history")) {
            try (Connection c=connections.abrir(); Statement s=c.createStatement()) {
                System.out.println("Demostración: saldo ficticio, sin tarjetas ni cargos reales.");
                try (ResultSet r=s.executeQuery("select balance from CINE_OWNER.demo_wallets where customer_id='DEMO'")) {
                    if (r.next()) System.out.println("Saldo MXN: " + r.getBigDecimal(1));
                }
                String query = action.equals("catalog")
                    ? "select p.id,p.name,p.price,i.quantity from CINE_OWNER.products p join CINE_OWNER.inventory i on i.product_id=p.id where i.branch_id=1 order by p.id"
                    : "select request_key,status,total,id from CINE_OWNER.purchase_orders where customer_id='DEMO' order by created_at";
                try (ResultSet r=s.executeQuery(query)) {
                    while(r.next()) System.out.printf("%s | %s | %s | %s%n",r.getString(1),r.getString(2),r.getString(3),r.getString(4));
                }
            }
        } else throw new IllegalArgumentException("Comandos: catalog, buy, history.");
    }
}

package com.penta.cinetics.ventas;

import com.penta.cinetics.ventas.aplicacion.Compras;
import com.penta.cinetics.ventas.aplicacion.SolicitudCompra;
import com.penta.cinetics.ventas.infraestructura.ComprasOracle;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Real Oracle, unique fictitious fixtures and scoped cleanup. Enable with -Poracle-it. */
class ComprasOracleIT {
    static Connection open(String tenant) throws SQLException {
        String password = System.getenv(tenant+"_APP_PASSWORD");
        if (password == null) throw new IllegalStateException("Run manage.py test with both tenant credentials");
        return DriverManager.getConnection("jdbc:oracle:thin:@//127.0.0.1:1522/"+tenant,"CINE_APP",password);
    }

    static final class Fixture implements AutoCloseable {
        final String tenant, customer;
        final long branch, product;
        final Connection fixtureConnection;
        Fixture(String tenant) throws SQLException {
            this(tenant,"IT_"+UUID.randomUUID().toString().replace("-",""),Math.abs(UUID.randomUUID().getLeastSignificantBits()%1000000000000L)+10000);
        }
        Fixture(String tenant, String customer, long id) throws SQLException {
            this.tenant=tenant;this.customer=customer;branch=id;product=id;
            fixtureConnection=open(tenant);
            execute("insert into CINE_OWNER.branches(id,code,name) values (?,?,?)",branch,customer,"Test branch");
            execute("insert into CINE_OWNER.products(id,code,name,price) values (?,?,?,?)",product,customer,"Test product",new BigDecimal("65.00"));
            execute("insert into CINE_OWNER.inventory(branch_id,product_id,quantity) values (?,?,?)",branch,product,5);
            execute("insert into CINE_OWNER.demo_wallets(customer_id,balance,active) values (?,?,?)",customer,new BigDecimal("1000.00"),1);
        }
        void execute(String sql,Object...values) throws SQLException {
            try(PreparedStatement s=fixtureConnection.prepareStatement(sql)) {
                for(int i=0;i<values.length;i++)s.setObject(i+1,values[i]);
                s.executeUpdate();
            }
        }
        BigDecimal number(String sql,Object...values) throws SQLException {
            try(PreparedStatement s=fixtureConnection.prepareStatement(sql)) {
                for(int i=0;i<values.length;i++)s.setObject(i+1,values[i]);
                try(ResultSet r=s.executeQuery()){assertTrue(r.next());return r.getBigDecimal(1);}
            }
        }
        BigDecimal balance() throws SQLException {return number("select balance from CINE_OWNER.demo_wallets where customer_id=?",customer).setScale(2);}
        int stock() throws SQLException {return number("select quantity from CINE_OWNER.inventory where branch_id=? and product_id=?",branch,product).intValueExact();}
        int orders() throws SQLException {return number("select count(*) from CINE_OWNER.purchase_orders where customer_id=?",customer).intValueExact();}
        int payments() throws SQLException {return number("select count(*) from CINE_OWNER.demo_payments p join CINE_OWNER.purchase_orders o on o.id=p.order_id where o.customer_id=?",customer).intValueExact();}
        SolicitudCompra request(String key,int quantity) {return new SolicitudCompra(customer,branch,key,List.of(new SolicitudCompra.Item(product,quantity)));}
        ComprasOracle service() {return new ComprasOracle(()->open(tenant));}
        public void close() throws SQLException {
            try {
            execute("delete from CINE_OWNER.demo_payments where order_id in (select id from CINE_OWNER.purchase_orders where customer_id=?)",customer);
            execute("delete from CINE_OWNER.purchase_lines where order_id in (select id from CINE_OWNER.purchase_orders where customer_id=?)",customer);
            execute("delete from CINE_OWNER.purchase_orders where customer_id=?",customer);
            execute("delete from CINE_OWNER.demo_wallets where customer_id=?",customer);
            execute("delete from CINE_OWNER.inventory where branch_id=?",branch);
            execute("delete from CINE_OWNER.products where id=?",product);
            execute("delete from CINE_OWNER.branches where id=?",branch);
            } finally { fixtureConnection.close(); }
        }
    }

    @Test void approvedPurchaseAndRetryAcrossNewConnectionChargeOnce() throws Exception {
        try(var f=new Fixture("CINE_TICS")) {
            var first=f.service().comprar(f.request("one",2));
            assertEquals(Compras.Estado.APPROVED,first.estado());
            assertEquals(new BigDecimal("130.00"),first.total());
            f.execute("update CINE_OWNER.products set price=99 where id=?",f.product);
            var retry=f.service().comprar(f.request("one",2));
            assertTrue(retry.repetida());assertEquals(first.id(),retry.id());assertEquals(first.total(),retry.total());
            assertEquals(new BigDecimal("870.00"),f.balance());assertEquals(3,f.stock());
            assertEquals(1,f.orders());assertEquals(1,f.payments());
            assertThrows(IllegalArgumentException.class,()->f.service().comprar(f.request("one",1)));
            assertEquals(1,f.payments());
        }
    }

    @Test void insufficientFundsPersistRejectionWithoutEffectsEvenAfterRefill() throws Exception {
        try(var f=new Fixture("CINE_TICS")) {
            f.execute("update CINE_OWNER.demo_wallets set balance=10 where customer_id=?",f.customer);
            var result=f.service().comprar(f.request("rejected",2));
            assertEquals(Compras.Estado.INSUFFICIENT_FUNDS,result.estado());
            assertEquals(5,f.stock());assertEquals(new BigDecimal("10.00"),f.balance());assertEquals(0,f.payments());
            f.execute("update CINE_OWNER.demo_wallets set balance=1000 where customer_id=?",f.customer);
            assertEquals(result.id(),f.service().comprar(f.request("rejected",2)).id());
            assertEquals(0,f.payments());
        }
    }

    @Test void unavailableSecondItemLeavesEntireCartUnchanged() throws Exception {
        try(var f=new Fixture("CINE_TICS")) {
            var request=new SolicitudCompra(f.customer,f.branch,"two",List.of(new SolicitudCompra.Item(f.product,2),new SolicitudCompra.Item(f.product+1,1)));
            assertEquals(Compras.Estado.PRODUCT_NOT_FOUND,f.service().comprar(request).estado());
            assertEquals(5,f.stock());assertEquals(new BigDecimal("1000.00"),f.balance());assertEquals(0,f.payments());
        }
    }

    @Test void twoConcurrentRequestsForLastUnitCannotOversell() throws Exception {
        try(var f=new Fixture("CINE_TICS");var other=new Fixture("CINE_TICS")) {
            f.execute("update CINE_OWNER.inventory set quantity=1 where branch_id=? and product_id=?",f.branch,f.product);
            var requests=List.of(f.request("a",1),new SolicitudCompra(other.customer,f.branch,"b",List.of(new SolicitudCompra.Item(f.product,1))));
            var results=concurrent(f.service(),requests);
            assertEquals(1,results.stream().filter(r->r.estado()==Compras.Estado.APPROVED).count());
            assertEquals(1,results.stream().filter(r->r.estado()==Compras.Estado.OUT_OF_STOCK).count());
            assertEquals(0,f.stock());assertEquals(1,f.payments()+other.payments());
            assertEquals(new BigDecimal("1935.00"),f.balance().add(other.balance()));
        }
    }

    @Test void concurrentDuplicateRequestsReturnSameOrder() throws Exception {
        try(var f=new Fixture("CINE_TICS")) {
            var request=f.request("same",2);
            var results=concurrent(f.service(),List.of(request,request));
            assertEquals(results.get(0).id(),results.get(1).id());
            assertEquals(1,f.orders());assertEquals(1,f.payments());assertEquals(3,f.stock());
            assertEquals(new BigDecimal("870.00"),f.balance());
        }
    }

    static List<Compras.Resultado> concurrent(Compras service,List<SolicitudCompra> requests) throws Exception {
        var start=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(2)) {
            var futures=requests.stream().map(r->pool.submit(()->{start.await();return service.comprar(r);})).toList();
            start.countDown();
            return List.of(futures.get(0).get(40,TimeUnit.SECONDS),futures.get(1).get(40,TimeUnit.SECONDS));
        }
    }

    @Test void failureAfterDebitRollsBackOrderLinesWalletAndStock() throws Exception {
        try(var f=new Fixture("CINE_TICS")) {
            var service=new ComprasOracle(()->{
                Connection real=open(f.tenant);
                return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},(proxy,method,args)->{
                    if(method.getName().equals("prepareStatement") && args[0].toString().startsWith("insert into CINE_OWNER.demo_payments"))
                        throw new SQLException("Injected failure after wallet debit");
                    try{return method.invoke(real,args);}catch(InvocationTargetException e){throw e.getCause();}
                });
            });
            assertThrows(SQLException.class,()->service.comprar(f.request("retryable",2)));
            assertEquals(0,f.orders());assertEquals(0,f.payments());assertEquals(5,f.stock());
            assertEquals(new BigDecimal("1000.00"),f.balance());
            assertEquals(Compras.Estado.APPROVED,f.service().comprar(f.request("retryable",2)).estado());
        }
    }

    @Test void identicalCustomerAndRequestInOtherPdbAreIndependent() throws Exception {
        try(var a=new Fixture("CINE_TICS");var b=new Fixture("CADENA_DEMO",a.customer,a.branch)) {
            a.service().comprar(a.request("same",2));
            assertEquals(0,b.orders());assertEquals(5,b.stock());assertEquals(new BigDecimal("1000.00"),b.balance());
            var second=b.service().comprar(b.request("same",1));
            assertFalse(second.repetida());assertEquals(new BigDecimal("65.00"),second.total());
            assertEquals(3,a.stock());assertEquals(4,b.stock());
        }
    }

    @Test void inactiveWalletCannotPurchase() throws Exception {
        try(var f=new Fixture("CINE_TICS")) {
            f.execute("update CINE_OWNER.demo_wallets set active=0 where customer_id=?",f.customer);
            assertEquals(Compras.Estado.INACTIVE_ACCOUNT,f.service().comprar(f.request("inactive",1)).estado());
            assertEquals(5,f.stock());assertEquals(0,f.payments());
        }
    }
}

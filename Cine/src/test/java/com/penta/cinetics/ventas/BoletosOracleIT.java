package com.penta.cinetics.ventas;

import com.penta.cinetics.boletos.aplicacion.VentaBoletos;
import com.penta.cinetics.boletos.aplicacion.VentaBoletos.*;
import com.penta.cinetics.boletos.infraestructura.BoletosOracle;
import com.penta.cinetics.reservas.aplicacion.Reservas;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoletosOracleIT {
    static BoletosOracle service(ReservasOracleIT.Escenario e) { return new BoletosOracle(()->ComprasOracleIT.open(e.f.tenant)); }
    static Solicitud request(ReservasOracleIT.Escenario e) {
        return new Solicitud(e.f.customer,"buy",List.of(new Seleccion(1,Tipo.ADULTO),new Seleccion(2,Tipo.NINO)));
    }
    @Test void purchaseIssuesPricedTicketsAndRetryChargesOnceEvenAfterExpiry() throws Exception {
        try(var e=new ReservasOracleIT.Escenario()) {
            var hold=e.servicio.reservar(e.solicitud("buy",1,2));
            var first=service(e).comprar(request(e));
            assertEquals(new BigDecimal("130.00"),first.total()); assertEquals(2,first.boletos().size());
            assertEquals(new BigDecimal("870.00"),e.f.balance());
            e.f.execute("update CINE_OWNER.seat_holds set expires_at=systimestamp-interval '1' second where id=?",hold.id());
            var retry=service(e).comprar(request(e));
            assertTrue(retry.repetida());assertEquals(first.boletos(),retry.boletos());assertEquals(first.venta(),retry.venta());
            assertEquals(new BigDecimal("870.00"),e.f.balance());
            assertFalse(e.servicio.disponibilidad(e.f.branch).getFirst().disponible());
            assertEquals(Reservas.Estado.CONFIRMADA,e.servicio.reservar(e.solicitud("buy",1,2)).estado());
            assertThrows(Reservas.Rechazo.class,()->e.servicio.cancelar(e.f.customer,"buy"));
            assertThrows(Reservas.Rechazo.class,()->e.servicio.reservar(e.solicitud("other",1)));
            assertEquals(first.boletos(),service(e).historial(e.f.customer));
        }
    }
    @Test void wrongOwnerOrSelectionCannotPayForHold() throws Exception {
        try(var e=new ReservasOracleIT.Escenario()) {
            e.servicio.reservar(e.solicitud("buy",1,2));
            assertEquals(Motivo.NO_EXISTE,assertThrows(Rechazo.class,()->service(e).comprar(new Solicitud("other","buy",request(e).seleccion()))).motivo());
            assertEquals(Motivo.SELECCION_DISTINTA,assertThrows(Rechazo.class,()->service(e).comprar(new Solicitud(e.f.customer,"buy",List.of(new Seleccion(1,Tipo.ADULTO))))).motivo());
            assertEquals(new BigDecimal("1000.00"),e.f.balance()); assertTrue(service(e).historial(e.f.customer).isEmpty());
            service(e).comprar(request(e));
            assertEquals(Motivo.SELECCION_DISTINTA,assertThrows(Rechazo.class,()->service(e).comprar(new Solicitud(e.f.customer,"buy",List.of(new Seleccion(1,Tipo.NINO),new Seleccion(2,Tipo.NINO))))).motivo());
        }
    }
    @Test void expiredOrCancelledHoldCannotBePurchased() throws Exception {
        try(var e=new ReservasOracleIT.Escenario()) {
            var hold=e.servicio.reservar(e.solicitud("buy",1,2));
            e.f.execute("update CINE_OWNER.seat_holds set expires_at=systimestamp-interval '1' second where id=?",hold.id());
            assertEquals(Motivo.RESERVA_NO_VIGENTE,assertThrows(Rechazo.class,()->service(e).comprar(request(e))).motivo());
            e.servicio.reservar(e.solicitud("cancel",1,2)); e.servicio.cancelar(e.f.customer,"cancel");
            assertThrows(Rechazo.class,()->service(e).comprar(new Solicitud(e.f.customer,"cancel",request(e).seleccion())));
            assertEquals(new BigDecimal("1000.00"),e.f.balance());assertTrue(service(e).historial(e.f.customer).isEmpty());
        }
    }
    @Test void insufficientFundsAndInactiveWalletLeaveHoldAndMoneyUnchanged() throws Exception {
        try(var e=new ReservasOracleIT.Escenario()) {
            e.servicio.reservar(e.solicitud("buy",1,2));
            e.f.execute("update CINE_OWNER.demo_wallets set balance=100 where customer_id=?",e.f.customer);
            assertEquals(Motivo.SALDO_INSUFICIENTE,assertThrows(Rechazo.class,()->service(e).comprar(request(e))).motivo());
            assertEquals(new BigDecimal("100.00"),e.f.balance());assertTrue(service(e).historial(e.f.customer).isEmpty());
            e.f.execute("update CINE_OWNER.demo_wallets set balance=1000,active=0 where customer_id=?",e.f.customer);
            assertEquals(Motivo.CUENTA_INACTIVA,assertThrows(Rechazo.class,()->service(e).comprar(request(e))).motivo());
            assertEquals(Reservas.Estado.ACTIVA,e.servicio.reservar(e.solicitud("buy",1,2)).estado());
        }
    }
    @Test void concurrentPaymentsForSameHoldHaveSameTickets() throws Exception {
        try(var e=new ReservasOracleIT.Escenario();var pool=Executors.newFixedThreadPool(2)) {
            e.servicio.reservar(e.solicitud("buy",1,2));var start=new CountDownLatch(1);
            Callable<Resultado> call=()->{start.await();return service(e).comprar(request(e));};
            var a=pool.submit(call);var b=pool.submit(call);start.countDown();
            assertEquals(a.get(30,TimeUnit.SECONDS).boletos(),b.get(30,TimeUnit.SECONDS).boletos());
            assertEquals(new BigDecimal("870.00"),e.f.balance());assertEquals(2,service(e).historial(e.f.customer).size());
        }
    }
    @Test void failureAfterDebitRollsBackPaymentSaleAndTickets() throws Exception {
        try(var e=new ReservasOracleIT.Escenario()) {
            e.servicio.reservar(e.solicitud("buy",1,2));
            var failing=new BoletosOracle(()->{
                var real=ComprasOracleIT.open(e.f.tenant);
                return (java.sql.Connection)java.lang.reflect.Proxy.newProxyInstance(java.sql.Connection.class.getClassLoader(),new Class<?>[]{java.sql.Connection.class},(proxy,method,args)->{
                    if(method.getName().equals("prepareStatement") && args[0].toString().startsWith("insert into CINE_OWNER.tickets"))
                        throw new java.sql.SQLException("Injected ticket issuance failure");
                    try{return method.invoke(real,args);}catch(java.lang.reflect.InvocationTargetException ex){throw ex.getCause();}
                });
            });
            assertThrows(java.sql.SQLException.class,()->failing.comprar(request(e)));
            assertEquals(new BigDecimal("1000.00"),e.f.balance());assertTrue(service(e).historial(e.f.customer).isEmpty());
            assertEquals(0,e.f.number("select count(*) from CINE_OWNER.ticket_sales where customer_id=?",e.f.customer).intValueExact());
            assertEquals(Reservas.Estado.ACTIVA,e.servicio.reservar(e.solicitud("buy",1,2)).estado());
            assertFalse(service(e).comprar(request(e)).repetida());
        }
    }
    @Test void sameCustomerInAnotherPdbHasIndependentTicketsAndBalance() throws Exception {
        try(var a=new ReservasOracleIT.Escenario();var b=new ReservasOracleIT.Escenario(new ComprasOracleIT.Fixture("CADENA_DEMO",a.f.customer,a.f.branch))) {
            a.servicio.reservar(a.solicitud("buy",1,2));b.servicio.reservar(b.solicitud("buy",1,2));
            service(a).comprar(request(a));
            assertTrue(service(b).historial(b.f.customer).isEmpty());assertEquals(new BigDecimal("1000.00"),b.f.balance());
            assertFalse(service(b).comprar(request(b)).repetida());
        }
    }
    @Test void paymentAndCancellationRaceHasOnlyOneValidOutcome() throws Exception {
        try(var e=new ReservasOracleIT.Escenario();var pool=Executors.newFixedThreadPool(2)) {
            e.servicio.reservar(e.solicitud("buy",1,2));var start=new CountDownLatch(1);
            var payment=pool.submit(()->{
                start.await();
                try {service(e).comprar(request(e));return true;}
                catch(Rechazo r) {assertEquals(Motivo.RESERVA_NO_VIGENTE,r.motivo());return false;}
            });
            var cancellation=pool.submit(()->{
                start.await();
                try {e.servicio.cancelar(e.f.customer,"buy");return true;}
                catch(Reservas.Rechazo r) {assertEquals(Reservas.Motivo.RESERVA_CONFIRMADA,r.motivo());return false;}
            });
            start.countDown();boolean paid=payment.get(30,TimeUnit.SECONDS);
            assertNotEquals(paid,cancellation.get(30,TimeUnit.SECONDS));
            assertEquals(new BigDecimal(paid?"870.00":"1000.00"),e.f.balance());
            assertEquals(paid?2:0,service(e).historial(e.f.customer).size());
            assertEquals(!paid,e.servicio.disponibilidad(e.f.branch).getFirst().disponible());
        }
    }
    @Test void concurrentDifferentShowsCannotOverdrawWallet() throws Exception {
        try(var e=new ReservasOracleIT.Escenario();var pool=Executors.newFixedThreadPool(2)) {
            e.servicio.crearFuncion(e.f.branch+1,e.f.branch,"Other",e.inicio.plusDays(1),e.inicio.plusDays(1).plusHours(2));
            e.servicio.reservar(e.solicitud("buy",1,2));
            e.servicio.reservar(new Reservas.Solicitud(e.f.customer,e.f.branch+1,"second",List.of(1,2)));
            e.f.execute("update CINE_OWNER.demo_wallets set balance=200 where customer_id=?",e.f.customer);
            var start=new CountDownLatch(1);
            var requests=List.of(request(e),new Solicitud(e.f.customer,"second",request(e).seleccion()));
            var futures=requests.stream().map(r->pool.submit(()->{
                start.await();
                try {service(e).comprar(r);return true;}
                catch(Rechazo rejected) {assertEquals(Motivo.SALDO_INSUFICIENTE,rejected.motivo());return false;}
            })).toList();
            start.countDown();assertNotEquals(futures.get(0).get(30,TimeUnit.SECONDS),futures.get(1).get(30,TimeUnit.SECONDS));
            assertEquals(new BigDecimal("70.00"),e.f.balance());assertEquals(2,service(e).historial(e.f.customer).size());
        }
    }
    @Test void expiryIsRecheckedAfterWaitingForWalletLock() throws Exception {
        try(var e=new ReservasOracleIT.Escenario();var pool=Executors.newSingleThreadExecutor()) {
            var hold=e.servicio.reservar(e.solicitud("buy",1,2));
            var waiting=new CountDownLatch(1);
            var observed=new BoletosOracle(()->{
                var real=ComprasOracleIT.open(e.f.tenant);
                return (java.sql.Connection)java.lang.reflect.Proxy.newProxyInstance(java.sql.Connection.class.getClassLoader(),new Class<?>[]{java.sql.Connection.class},(proxy,method,args)->{
                    if(method.getName().equals("prepareStatement") && args[0].toString().startsWith("select balance,active")) waiting.countDown();
                    try{return method.invoke(real,args);}catch(java.lang.reflect.InvocationTargetException ex){throw ex.getCause();}
                });
            });
            e.f.fixtureConnection.setAutoCommit(false);
            try {
                e.f.execute("update CINE_OWNER.demo_wallets set balance=balance where customer_id=?",e.f.customer);
                var attempt=pool.submit(()->assertThrows(Rechazo.class,()->observed.comprar(request(e))));
                assertTrue(waiting.await(10,TimeUnit.SECONDS));
                e.f.execute("update CINE_OWNER.seat_holds set expires_at=systimestamp-interval '1' second where id=?",hold.id());
                e.f.fixtureConnection.commit();
                assertEquals(Motivo.RESERVA_NO_VIGENTE,attempt.get(30,TimeUnit.SECONDS).motivo());
            } finally {e.f.fixtureConnection.rollback();e.f.fixtureConnection.setAutoCommit(true);}
            assertEquals(new BigDecimal("1000.00"),e.f.balance());assertTrue(service(e).historial(e.f.customer).isEmpty());
        }
    }

}

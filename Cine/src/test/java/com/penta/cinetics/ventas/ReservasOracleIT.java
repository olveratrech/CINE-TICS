package com.penta.cinetics.ventas;

import com.penta.cinetics.reservas.aplicacion.Reservas;
import com.penta.cinetics.reservas.infraestructura.ReservasOracle;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReservasOracleIT {
    static class Escenario implements AutoCloseable {
        final ComprasOracleIT.Fixture f;
        final ReservasOracle servicio;
        final OffsetDateTime inicio = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
        Escenario() throws Exception { this(new ComprasOracleIT.Fixture("CINE_TICS")); }
        Escenario(ComprasOracleIT.Fixture fixture) throws Exception {
            f = fixture;
            servicio = new ReservasOracle(() -> ComprasOracleIT.open(f.tenant));
            f.execute("insert into CINE_OWNER.auditoriums(id,branch_id,name,capacity) values (?,?,?,?)",f.branch,f.branch,"Test room",3);
            servicio.crearFuncion(f.branch,f.branch,"Test movie",inicio,inicio.plusHours(2));
        }
        Reservas.Solicitud solicitud(String clave, Integer... asientos) {
            return new Reservas.Solicitud(f.customer,f.branch,clave,List.of(asientos));
        }
        public void close() throws Exception {
            try {
                f.execute("delete from CINE_OWNER.ticket_payments where sale_id in (select id from CINE_OWNER.ticket_sales where screening_id in (select id from CINE_OWNER.screenings where auditorium_id=?))",f.branch);
                f.execute("delete from CINE_OWNER.tickets where screening_id in (select id from CINE_OWNER.screenings where auditorium_id=?)",f.branch);
                f.execute("delete from CINE_OWNER.ticket_sales where screening_id in (select id from CINE_OWNER.screenings where auditorium_id=?)",f.branch);
                f.execute("delete from CINE_OWNER.screening_seats where screening_id in (select id from CINE_OWNER.screenings where auditorium_id=?)",f.branch);
                f.execute("delete from CINE_OWNER.seat_holds where screening_id in (select id from CINE_OWNER.screenings where auditorium_id=?)",f.branch);
                f.execute("delete from CINE_OWNER.screenings where auditorium_id=?",f.branch);
                f.execute("delete from CINE_OWNER.auditoriums where id=?",f.branch);
            } finally { f.close(); }
        }
    }
    @Test void reserveAllSeatsAtomicallyAndReplayWithoutExtendingExpiry() throws Exception {
        try (var e = new Escenario()) {
            var first = e.servicio.reservar(e.solicitud("one",2,1));
            var retry = e.servicio.reservar(e.solicitud("one",1,2));
            assertEquals(first.id(),retry.id()); assertEquals(first.vence(),retry.vence()); assertTrue(retry.repetida());
            assertEquals(List.of(false,false,true),e.servicio.disponibilidad(e.f.branch).stream().map(Reservas.Asiento::disponible).toList());
            assertEquals(Reservas.Motivo.NO_DISPONIBLE,assertThrows(Reservas.Rechazo.class,()->e.servicio.reservar(e.solicitud("two",2,3))).motivo());
            assertTrue(e.servicio.disponibilidad(e.f.branch).get(2).disponible());
            assertEquals(Reservas.Motivo.CLAVE_REUTILIZADA,assertThrows(Reservas.Rechazo.class,()->e.servicio.reservar(e.solicitud("one",3))).motivo());
        }
    }
    @Test void expirationReleasesSeatsAndRetryDoesNotRenew() throws Exception {
        try (var e = new Escenario()) {
            var first=e.servicio.reservar(e.solicitud("old",1));
            e.f.execute("update CINE_OWNER.seat_holds set expires_at=systimestamp-interval '1' second where id=?",first.id());
            assertTrue(e.servicio.disponibilidad(e.f.branch).getFirst().disponible());
            assertEquals(Reservas.Estado.VENCIDA,e.servicio.reservar(e.solicitud("old",1)).estado());
            assertEquals(Reservas.Estado.ACTIVA,e.servicio.reservar(e.solicitud("new",1)).estado());
            e.servicio.cancelar(e.f.customer,"old");
            assertFalse(e.servicio.disponibilidad(e.f.branch).getFirst().disponible());
        }
    }
    @Test void cancelIsOwnedAndIdempotent() throws Exception {
        try (var e = new Escenario()) {
            e.servicio.reservar(e.solicitud("cancel",1));
            assertThrows(Reservas.Rechazo.class,()->e.servicio.cancelar("another","cancel"));
            assertEquals(Reservas.Estado.CANCELADA,e.servicio.cancelar(e.f.customer,"cancel").estado());
            assertEquals(Reservas.Estado.CANCELADA,e.servicio.cancelar(e.f.customer,"cancel").estado());
            assertTrue(e.servicio.disponibilidad(e.f.branch).getFirst().disponible());
        }
    }
    @Test void concurrentCustomersCannotHoldSameSeat() throws Exception {
        try (var e = new Escenario(); var other = new ComprasOracleIT.Fixture("CINE_TICS")) {
            var a=e.solicitud("a",1);
            var b=new Reservas.Solicitud(other.customer,e.f.branch,"b",List.of(1));
            try(var pool=Executors.newFixedThreadPool(2)) {
                var start=new CountDownLatch(1);
                Callable<Boolean> first=()->{start.await();return attempt(e.servicio,a);};
                Callable<Boolean> second=()->{start.await();return attempt(e.servicio,b);};
                var x=pool.submit(first);var y=pool.submit(second);start.countDown();
                assertNotEquals(x.get(30,TimeUnit.SECONDS),y.get(30,TimeUnit.SECONDS));
            } finally {
                // Other wallet may own the winning hold; remove it before that fixture closes.
                e.f.execute("update CINE_OWNER.screening_seats set hold_id=null where screening_id=?",e.f.branch);
                e.f.execute("delete from CINE_OWNER.seat_holds where screening_id=?",e.f.branch);
            }
        }
    }
    static boolean attempt(Reservas service,Reservas.Solicitud s) throws Exception {
        try {service.reservar(s);return true;}
        catch(Reservas.Rechazo r) {assertEquals(Reservas.Motivo.NO_DISPONIBLE,r.motivo());return false;}
    }
    @Test void concurrentSameKeyHasOneStableResult() throws Exception {
        try(var e=new Escenario();var pool=Executors.newFixedThreadPool(2)) {
            var start=new CountDownLatch(1);
            Callable<Reservas.Resultado> call=()->{start.await();return e.servicio.reservar(e.solicitud("same",1));};
            var a=pool.submit(call);var b=pool.submit(call);start.countDown();
            assertEquals(a.get(30,TimeUnit.SECONDS).id(),b.get(30,TimeUnit.SECONDS).id());
        }
    }
    @Test void sameSeatInDifferentScreeningsIsIndependentAndOverlapRejected() throws Exception {
        try(var e=new Escenario()) {
            assertEquals(Reservas.Motivo.HORARIO_SOLAPADO,assertThrows(Reservas.Rechazo.class,()->e.servicio.crearFuncion(e.f.branch+1,e.f.branch,"Overlap",e.inicio.plusHours(1),e.inicio.plusHours(3))).motivo());
            e.servicio.crearFuncion(e.f.branch+1,e.f.branch,"Next",e.inicio.plusHours(2),e.inicio.plusHours(4));
            e.servicio.reservar(e.solicitud("first",1));
            assertTrue(e.servicio.disponibilidad(e.f.branch+1).getFirst().disponible());
            assertEquals(Reservas.Estado.ACTIVA,e.servicio.reservar(new Reservas.Solicitud(e.f.customer,e.f.branch+1,"second",List.of(1))).estado());
        }
    }
    @Test void startedShowInactiveWalletAndInvalidSeatCannotBeReserved() throws Exception {
        try(var e=new Escenario()) {
            assertThrows(Reservas.Rechazo.class,()->e.servicio.reservar(e.solicitud("absent",4)));
            assertTrue(e.servicio.disponibilidad(e.f.branch).stream().allMatch(Reservas.Asiento::disponible));
            e.f.execute("update CINE_OWNER.demo_wallets set active=0 where customer_id=?",e.f.customer);
            assertEquals(Reservas.Motivo.CUENTA_INACTIVA,assertThrows(Reservas.Rechazo.class,()->e.servicio.reservar(e.solicitud("inactive",1))).motivo());
            e.f.execute("update CINE_OWNER.demo_wallets set active=1 where customer_id=?",e.f.customer);
            e.f.execute("update CINE_OWNER.screenings set starts_at=systimestamp-interval '1' hour where id=?",e.f.branch);
            assertEquals(Reservas.Motivo.FUNCION_INICIADA,assertThrows(Reservas.Rechazo.class,()->e.servicio.reservar(e.solicitud("late",1))).motivo());
            assertTrue(e.servicio.disponibilidad(e.f.branch).stream().noneMatch(Reservas.Asiento::disponible));
        }
    }
    @Test void failedSeatUpdateRollsBackEntireHold() throws Exception {
        try(var e=new Escenario()) {
            var failing=new ReservasOracle(()->{
                var real=ComprasOracleIT.open(e.f.tenant);
                return (java.sql.Connection)java.lang.reflect.Proxy.newProxyInstance(
                    java.sql.Connection.class.getClassLoader(),new Class<?>[]{java.sql.Connection.class},(proxy,method,args)->{
                        if(method.getName().equals("prepareStatement") && args[0].toString().startsWith("update CINE_OWNER.screening_seats"))
                            throw new java.sql.SQLException("Injected seat write failure");
                        try {return method.invoke(real,args);}
                        catch(java.lang.reflect.InvocationTargetException ex) {throw ex.getCause();}
                    });
            });
            assertThrows(java.sql.SQLException.class,()->failing.reservar(e.solicitud("retry",1,2)));
            assertTrue(e.servicio.disponibilidad(e.f.branch).stream().allMatch(Reservas.Asiento::disponible));
            assertEquals(0,e.f.number("select count(*) from CINE_OWNER.seat_holds where screening_id=?",e.f.branch).intValueExact());
            assertFalse(e.servicio.reservar(e.solicitud("retry",1,2)).repetida());
        }
    }
    @Test void sameIdsInOtherChainDoNotShareSeatHolds() throws Exception {
        try(var a=new Escenario();var b=new Escenario(new ComprasOracleIT.Fixture("CADENA_DEMO",a.f.customer,a.f.branch))) {
            a.servicio.reservar(a.solicitud("same",1));
            assertTrue(b.servicio.disponibilidad(b.f.branch).getFirst().disponible());
            var other=b.servicio.reservar(b.solicitud("same",1));
            assertFalse(other.repetida());
            assertEquals(Reservas.Estado.ACTIVA,other.estado());
        }
    }
    @Test void simultaneousOverlappingSchedulesCannotBothBeCreated() throws Exception {
        try(var e=new Escenario();var pool=Executors.newFixedThreadPool(2)) {
            var start=new CountDownLatch(1);
            var futures=java.util.stream.LongStream.of(1,2).mapToObj(n->pool.submit(()->{
                start.await();
                try {
                    e.servicio.crearFuncion(e.f.branch+n,e.f.branch,"Concurrent",e.inicio.plusDays(1),e.inicio.plusDays(1).plusHours(2));
                    return true;
                } catch(Reservas.Rechazo r) {assertEquals(Reservas.Motivo.HORARIO_SOLAPADO,r.motivo());return false;}
            })).toList();
            start.countDown();
            assertNotEquals(futures.get(0).get(30,TimeUnit.SECONDS),futures.get(1).get(30,TimeUnit.SECONDS));
        }
    }

}

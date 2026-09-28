package com.penta.cinetics.consola;

import com.penta.cinetics.reservas.aplicacion.Reservas;
import com.penta.cinetics.reservas.infraestructura.ReservasOracle;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/** Synthetic CLI only: DEMO identity is not authentication. */
final class ReservasConsole {
    static void ejecutar(String[] args, ReservasOracle.Conexion connections) throws Exception {
        var service = new ReservasOracle(connections);
        switch (args[0]) {
            case "schedule-demo" -> {
                if (args.length!=2) throw new IllegalArgumentException("schedule-demo AAAAMMDD");
                var day = LocalDate.parse(args[1],DateTimeFormatter.BASIC_ISO_DATE);
                var start = day.atTime(19,0).atZone(ZoneId.of("America/Mexico_City")).toOffsetDateTime();
                service.crearFuncion(Long.parseLong(args[1]),9101,"Película de demostración",start,start.plusHours(2));
                System.out.println("Función " + args[1] + ": " + start + ", 12 asientos. No modifica funciones existentes.");
            }
            case "shows" -> {
                try(var c=connections.abrir();var s=c.createStatement();var r=s.executeQuery(
                        "select id,title,starts_at from CINE_OWNER.screenings order by starts_at")) {
                    while(r.next()) System.out.printf("%s | %s | %s%n",r.getString(1),r.getString(2),r.getObject(3,java.time.OffsetDateTime.class));
                }
            }
            case "seats" -> {
                if(args.length!=2) throw new IllegalArgumentException("seats funcion");
                for(var seat:service.disponibilidad(Long.parseLong(args[1])))
                    System.out.printf("%d: %s%n",seat.numero(),seat.disponible()?"disponible":"no disponible");
            }
            case "hold" -> {
                if(args.length<4) throw new IllegalArgumentException("hold clave funcion asiento [asiento ...]");
                var seats=Arrays.stream(args,3,args.length).map(Integer::valueOf).toList();
                mostrar(service.reservar(new Reservas.Solicitud("DEMO",Long.parseLong(args[2]),args[1],seats)));
            }
            case "cancel-hold" -> {
                if(args.length!=2) throw new IllegalArgumentException("cancel-hold clave");
                mostrar(service.cancelar("DEMO",args[1]));
            }
            default -> throw new IllegalArgumentException("Comando de reservas desconocido.");
        }
    }
    private static void mostrar(Reservas.Resultado r) {
        System.out.printf("Reserva %s | %s | vence %s%s%n",r.id(),r.estado(),r.vence(),r.repetida()?" | solicitud ya procesada":"");
        System.out.println("Reserva temporal: no es boleto ni genera un cobro.");
    }
}

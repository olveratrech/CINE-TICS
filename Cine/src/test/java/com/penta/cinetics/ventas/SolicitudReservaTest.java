package com.penta.cinetics.ventas;

import com.penta.cinetics.reservas.aplicacion.Reservas.Solicitud;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SolicitudReservaTest {
    @Test void selectionIsCanonicalAndImmutable() {
        var request=new Solicitud("DEMO",1,"one",List.of(3,1,2));
        assertEquals(List.of(1,2,3),request.asientos());
        assertThrows(UnsupportedOperationException.class,()->request.asientos().add(4));
    }
    @Test void rejectsInvalidAndRepeatedSeats() {
        assertThrows(IllegalArgumentException.class,()->new Solicitud("DEMO",1,"one",List.of()));
        assertThrows(IllegalArgumentException.class,()->new Solicitud("DEMO",1,"one",List.of(1,1)));
        assertThrows(IllegalArgumentException.class,()->new Solicitud("DEMO",1,"one",List.of(0)));
        assertThrows(IllegalArgumentException.class,()->new Solicitud("DEMO",0,"one",List.of(1)));
        assertThrows(IllegalArgumentException.class,()->new Solicitud("DEMO",1,"",List.of(1)));
        assertThrows(IllegalArgumentException.class,()->new Solicitud("DEMO",1,"one",java.util.stream.IntStream.rangeClosed(1,11).boxed().toList()));
    }
}

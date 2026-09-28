package com.penta.cinetics.ventas;

import com.penta.cinetics.boletos.aplicacion.VentaBoletos.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SolicitudBoletoTest {
    @Test void adultAndChildFaresAreExactAndSelectionCanonical() {
        assertEquals(new BigDecimal("80.00"),Tipo.ADULTO.precio());
        assertEquals(new BigDecimal("50.00"),Tipo.NINO.precio());
        var request=new Solicitud("DEMO","one",List.of(new Seleccion(2,Tipo.NINO),new Seleccion(1,Tipo.ADULTO)));
        assertEquals(1,request.seleccion().getFirst().asiento());
        assertThrows(UnsupportedOperationException.class,()->request.seleccion().clear());
    }
    @Test void rejectsDuplicateSeatsAndInvalidIdentity() {
        var seat=new Seleccion(1,Tipo.ADULTO);
        assertThrows(IllegalArgumentException.class,()->new Solicitud("DEMO","one",List.of(seat,seat)));
        assertThrows(IllegalArgumentException.class,()->new Solicitud("DEMO","one",List.of()));
        assertThrows(IllegalArgumentException.class,()->new Solicitud("","one",List.of(seat)));
        assertThrows(IllegalArgumentException.class,()->new Seleccion(0,Tipo.ADULTO));
        assertThrows(IllegalArgumentException.class,()->new Seleccion(1,null));
    }
}

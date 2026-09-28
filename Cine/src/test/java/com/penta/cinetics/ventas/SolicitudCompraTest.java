package com.penta.cinetics.ventas;

import com.penta.cinetics.ventas.aplicacion.SolicitudCompra;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SolicitudCompraTest {
    @Test void canonicalPayloadMergesAndSortsLines() {
        var a = new SolicitudCompra("C",1,"key",List.of(new SolicitudCompra.Item(2,1),new SolicitudCompra.Item(1,2),new SolicitudCompra.Item(2,2)));
        var b = new SolicitudCompra("C",1,"key",List.of(new SolicitudCompra.Item(1,2),new SolicitudCompra.Item(2,3)));
        assertEquals(a.huella(),b.huella());
        assertEquals(2,a.items().size());
        assertThrows(UnsupportedOperationException.class,()->a.items().clear());
    }
    @Test void invalidPayloadNeverReachesDatabase() {
        assertThrows(IllegalArgumentException.class,()->new SolicitudCompra.Item(1,0));
        assertThrows(IllegalArgumentException.class,()->new SolicitudCompra("C",1,"key",List.of()));
        assertThrows(IllegalArgumentException.class,()->new SolicitudCompra("C",1,"bad key",List.of(new SolicitudCompra.Item(1,1))));
    }
}

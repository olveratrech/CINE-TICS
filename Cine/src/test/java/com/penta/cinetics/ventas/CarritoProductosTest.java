package com.penta.cinetics.ventas;

import static org.junit.jupiter.api.Assertions.*;
import com.penta.cinetics.ventas.dominio.CarritoProductos;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CarritoProductosTest {
    @Test
    void decimalPricesAreExactAndRepeatedProductsMerge() {
        var cart = new CarritoProductos();
        cart.agregar("P1", "Demo", "Demo", new BigDecimal("0.10"), 1, 5);
        cart.agregar("P1", "Demo", "Demo", new BigDecimal("0.10"), 2, 5);
        assertEquals(new BigDecimal("0.30"), cart.total());
        assertEquals(1, cart.lineas().size());
        assertEquals(3, cart.lineas().getFirst().cantidad());
        assertThrows(UnsupportedOperationException.class, () -> cart.lineas().clear());
    }

    @Test
    void editingQuantityRecalculatesAndRejectsInvalidChangesWithoutMutation() {
        var cart = new CarritoProductos();
        cart.agregar("P1", "Demo", "Demo", new BigDecimal("65"), 1, 5);
        cart.cambiarCantidad("P1", 3, 5);
        assertEquals(new BigDecimal("195.00"), cart.total());
        for (int invalid : new int[]{0, -1, 6}) {
            assertThrows(IllegalArgumentException.class, () -> cart.cambiarCantidad("P1", invalid, 5));
            assertEquals(new BigDecimal("195.00"), cart.total());
        }
    }

    @Test
    void changedPriceOrOverflowCannotCorruptExistingLine() {
        var cart = new CarritoProductos();
        cart.agregar("P1", "Demo", "Demo", new BigDecimal("65"), 1, Integer.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () ->
                cart.agregar("P1", "Demo", "Demo", new BigDecimal("66"), 1, 5));
        assertThrows(IllegalArgumentException.class, () ->
                cart.agregar("P1", "Demo", "Demo", new BigDecimal("65"), Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(new BigDecimal("65.00"), cart.total());
    }
}

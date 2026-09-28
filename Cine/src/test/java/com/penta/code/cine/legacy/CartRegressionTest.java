package com.penta.code.cine.legacy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import penta.code.cine.gestion.Dulceria.Carrito;
import penta.code.cine.gestion.Dulceria.Producto;

class CartRegressionTest {
    private Producto product(int stock) {
        return new Producto("P1", "Refresco", 65, "Bebidas", stock);
    }

    @Test
    void multipleUnitsAreIncludedInTotal() {
        Carrito cart = new Carrito();
        cart.agregarProducto(product(5), 2);
        assertEquals(130, cart.calcularTotal(), 0.000001);
    }

    @Test
    void negativeQuantityDoesNotChangeCart() {
        Carrito cart = new Carrito();
        cart.agregarProducto(product(5), -1);
        assertEquals(0, cart.calcularTotal(), 0.000001);
    }

    @Test
    void zeroQuantityDoesNotChangeCart() {
        Carrito cart = new Carrito();
        cart.agregarProducto(product(5), 0);
        assertEquals(0, cart.calcularTotal(), 0.000001);
    }

    @Test
    void repeatedAdditionsCannotExceedStock() {
        Carrito cart = new Carrito();
        cart.agregarProducto(product(1), 1);
        cart.agregarProducto(product(1), 1);
        assertEquals(65, cart.calcularTotal(), 0.000001);
    }
}

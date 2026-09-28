package com.penta.code.cine.legacy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import penta.code.cine.gestion.CineTICS.Asiento;
import penta.code.cine.gestion.CineTICS.Boleto;
import penta.code.cine.gestion.CineTICS.CarritoBoletos;
import penta.code.cine.gestion.CineTICS.Sala;
import penta.code.cine.gestion.Dulceria.Carrito;
import penta.code.cine.gestion.Dulceria.Producto;
import penta.code.cine.gestion.Funciones.Funcion;
import penta.code.cine.gestion.Funciones.Pelicula;
import penta.code.cine.gestion.ProgramaLealtad.ProgramaLealtad;

/** Characterizes reusable rules, without reading or writing local customer data. */
class LegacyRulesTest {
    @Test
    void ticketCartChargesAdultAndChildAndAllowsRemoval() {
        Funcion show = new Funcion("DEMO", new Pelicula("P1", "Demo", "2", "Familiar"), "18:00");
        Boleto adult = new Boleto(show, new Asiento("A1"), 80);
        Boleto child = new Boleto(show, new Asiento("A2"), 50);
        CarritoBoletos cart = new CarritoBoletos();
        cart.agregarBoleto(adult);
        cart.agregarBoleto(child);
        assertEquals(130, cart.calcularTotal(), 0.000001);
        cart.eliminarBoleto(child);
        assertEquals(80, cart.calcularTotal(), 0.000001);
        cart.limpiarCarrito();
        assertEquals(0, cart.calcularTotal(), 0.000001);
    }

    @Test
    void productCartRejectsQuantityAboveAvailableStock() {
        Carrito cart = new Carrito();
        cart.agregarProducto(new Producto("P1", "Demo", 65, "Bebidas", 2), 3);
        assertEquals(0, cart.calcularTotal(), 0.000001);
    }

    @Test
    void productCartCanRemoveOneProductWithoutRemovingAnother() {
        Carrito cart = new Carrito();
        cart.agregarProducto(new Producto("P1", "Demo 1", 65, "Bebidas", 2), 1);
        cart.agregarProducto(new Producto("P2", "Demo 2", 75, "Botana", 2), 1);
        cart.eliminarProducto("P1");
        assertEquals(75, cart.calcularTotal(), 0.000001);
    }

    @Test
    void roomCreatesUniqueSeatsWithExpectedCapacity() {
        Sala room = new Sala(1, 10, 12);
        assertEquals(120, room.getCapacidad());
        assertEquals(120, room.getAsientos().size());
        assertEquals(120L, room.getAsientos().stream().map(Asiento::getNumeroAsiento).distinct().count());
        assertTrue(room.getAsientos().stream().noneMatch(Asiento::isOcupado));
        assertEquals("A1", room.getAsientos().getFirst().getNumeroAsiento());
        assertEquals("J12", room.getAsientos().getLast().getNumeroAsiento());
    }

    @ParameterizedTest
    @CsvSource({"0,3", "5,7.5", "10,15"})
    void loyaltyAppliesDocumentedRateAtEachTier(int tier, double expectedPoints) {
        ProgramaLealtad loyalty = new ProgramaLealtad(tier, 0);
        loyalty.agregarCompra(100);
        assertEquals(expectedPoints, loyalty.getPuntos(), 0.000001);
        assertEquals(expectedPoints * 0.0027, loyalty.calcularDineroDePuntos(), 0.000001);
    }

    @Test
    void loyaltyAccumulatesPointsAcrossPurchases() {
        ProgramaLealtad loyalty = new ProgramaLealtad();
        loyalty.agregarCompra(100);
        loyalty.agregarCompra(200);
        assertEquals(9, loyalty.getPuntos(), 0.000001);
    }
}

package com.penta.code.cine.legacy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import penta.code.cine.Funciones.MetodosEnGeneral;
import penta.code.cine.gestion.Clientes.Cliente;
import penta.code.cine.gestion.CineTICS.*;
import penta.code.cine.gestion.Dulceria.*;
import penta.code.cine.gestion.Financiera.*;
import penta.code.cine.gestion.Funciones.*;

/** Runs only in the isolated directory created by ConsoleCheckoutTest. */
public class ConsoleCheckoutProbe {
    public static void main(String[] args) throws Exception {
        boolean approved = Boolean.parseBoolean(args[1]);
        System.setIn(new ByteArrayInputStream("1\n1\n".getBytes(StandardCharsets.UTF_8)));
        Cliente client = new Cliente();
        client.setIdCliente("DEMO");
        client.setNombre("Demo");
        client.setApellidoPaterno("Usuario");
        client.setApellidoMaterno("Prueba");
        client.unirseProgramaLealtad();
        Files.createDirectories(Path.of("MetodosDePago"));
        CuentaBancaria account = new CuentaBancaria("Demo Usuario Prueba", "DEMO-NO-TARJETA",
                "12/2099", 0, "Simulada", true, approved ? 200 : 100, "fixture");
        RegistroCuentaBancaria.registrarEnArchivo(client, account);
        Sucursal branch = new Sucursal("Cine-TICS 'CU'");
        double remainingCart;
        if (args[0].equals("products")) {
            Carrito cart = new Carrito();
            cart.agregarProducto(new Producto("P1", "Refresco", 65, "Bebidas", 5), 2);
            MetodosEnGeneral.ProcesoCompra.realizarCompraProductos(client, cart, branch);
            remainingCart = cart.calcularTotal();
        } else {
            CarritoBoletos cart = new CarritoBoletos();
            Funcion show = new Funcion("F1", new Pelicula("P1", "Demo", "2", "Demo"), "18:00");
            cart.agregarBoleto(new Boleto(show, new Asiento("A1"), 80));
            cart.agregarBoleto(new Boleto(show, new Asiento("A2"), 50));
            MetodosEnGeneral.ProcesoCompra.realizarCompraBoletos(client, cart, branch);
            remainingCart = cart.calcularTotal();
        }
        double balance = RegistroCuentaBancaria.cargarMetodoDePago(client).getSaldo();
        if (Math.abs(balance - (approved ? 70 : 100)) > 0.000001
                || Math.abs(remainingCart - (approved ? 0 : 130)) > 0.000001
                || Math.abs(client.getProgramaLealtad().getPuntos() - (approved ? 3.9 : 0)) > 0.000001) {
            throw new AssertionError("Unexpected account/cart/loyalty state");
        }
    }
}

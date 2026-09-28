package penta.code.cine.gestion.CineTICS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import penta.code.cine.gestion.Dulceria.Producto;

public class CarritoBoletos {
    protected List<Boleto> boletos;

    public CarritoBoletos() {
        this.boletos = new ArrayList<>();
    }

    public void agregarBoleto(Boleto boleto) {
        boletos.add(boleto);
    }

    public void eliminarBoleto(Boleto boleto) {
        if (boletos.remove(boleto)) {
            System.out.println("Boleto eliminado del carrito.");
        } else {
            System.out.println("El boleto no está en el carrito.");
        }
    }
    
    public void mostrarCarrito() {
        if (boletos.isEmpty()) {
            System.out.println("\nTu carrito está vacío. Agrega boletos a tu carrito ;).");
        } else {
            System.out.println("Boletos en tu carrito: ");
            double total = 0;
            System.out.println(String.format("%-10s %-25s %-10s %-10s", "CANT", "DESCRIPCIÓN", "PRECIO", "ASIENTO"));
            
            for (Boleto boleto : boletos) {
                double importe = boleto.getPrecio();
                total += importe;
                    System.out.println((String.format("%-10d %-25s $%-9.2f %-9s", 
                                           1, 
                                           boleto.getFuncion().getPelicula().getTitulo(), 
                                           boleto.getPrecio(), 
                                           boleto.getAsiento().numeroAsiento)));
                    System.out.println();
                }
            System.out.println((String.format("%-37s $%-15.2f", "TOTAL:", total)));
        }
    }
    
    public void guardarCarritoBoletosEnArchivo(String idCliente, String sucursal) {
        String directorio = "CarritosBoletos/" + sucursal;
        String archivo = directorio + "/" + idCliente + ".txt";

        File dir = new File(directorio);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Directorio creado: " + directorio);
            } else {
                System.out.println("No se pudo crear el directorio: " + directorio);
                return;
            }
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            writer.write(idCliente + "," + sucursal);
            writer.newLine();
            writer.write("-------------------------------");
            writer.newLine();
            for (Boleto boleto : boletos) {
                writer.write(boleto.toString());
                writer.newLine();
            }
            System.out.println("El carrito ha sido guardado exitosamente.");
        } catch (IOException e) {
            System.out.println("Error al guardar el carrito: " + e.getMessage());
        }
    }

    public double calcularTotal() {
        return calcularTotalDecimal().doubleValue();
    }

    public java.math.BigDecimal calcularTotalDecimal() {
        return boletos.stream().map(b -> java.math.BigDecimal.valueOf(b.getPrecio()))
                .reduce(new java.math.BigDecimal("0.00"), java.math.BigDecimal::add);
    }

    public void limpiarCarrito() {
        this.boletos.clear();
    }
}

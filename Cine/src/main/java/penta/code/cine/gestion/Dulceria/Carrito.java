package penta.code.cine.gestion.Dulceria;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author olveratrech
 */
public class Carrito {
    protected List<Producto> carrito;

    public Carrito() {
        this.carrito = new ArrayList<>();
    }

    public void agregarProducto(Producto producto, int cantidad/*, Dulceria dulceria, String sucursalActual*/) {
        if (producto.getStock() >= cantidad) {
            //producto.disminuirStock(cantidad);
            //dulceria.actualizarStock(producto.getCodigo(), cantidad, sucursalActual);
            carrito.add(new Producto(producto.getCodigo(), producto.getNombre(), producto.getPrecio(), producto.getCategoria(), cantidad));
            System.out.println("Se agregó " + producto.getNombre() + " al carrito.");
        } else {
            System.out.println("Stock insuficiente de " + producto.getNombre() + ", se ha levantado una alerta a adminstración del cine. ");
        }
    }

    public void mostrarCarrito() {
        if (carrito.isEmpty()) {
            System.out.println("\nTu carrito está vacío. Agrega productos a tu carrito ;).");
        } else {
            System.out.println("Productos en tu carrito: ");
            double total = 0;
            System.out.println(String.format("%-10s %-25s %-10s %-10s", "CANT", "DESCRIPCIÓN", "PRECIO", "IMPORTE"));
            
            for (Producto producto : carrito) {
            double importe = producto.getPrecio() * producto.getStock();
            total += importe;
                System.out.println((String.format("%-10d %-25s $%-9.2f $%-9.2f", 
                                       producto.getStock(), 
                                       producto.getNombre() + ", " + producto.getCategoria(), 
                                       producto.getPrecio(), 
                                       importe)));
                System.out.println();
            }
            System.out.println((String.format("%-37s $%-15.2f", "TOTAL:", total)));
        }
    }
    
    public void guardarCarritoEnArchivo(String idCliente, String sucursal) {
        String directorio = "Carritos/" + sucursal;
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
            for (Producto producto : carrito) {
                writer.write(producto.toString());
                writer.newLine();
            }
            System.out.println("El carrito ha sido guardado exitosamente.");
        } catch (IOException e) {
            System.out.println("Error al guardar el carrito: " + e.getMessage());
        }
    }

    public void eliminarProducto(String codigo) {
        carrito.removeIf(producto -> producto.getCodigo().equals(codigo));
    }
    
    public double calcularTotal() {
        return carrito.stream().mapToDouble(Producto::getPrecio).sum();
    }
    
    public void limpiarCarrito() {
        this.carrito.clear();
    }
}

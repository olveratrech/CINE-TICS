package penta.code.cine.gestion.Dulceria;
import java.io.*;
import com.penta.cinetics.ventas.dominio.CarritoProductos;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author olveratrech
 */
public class Carrito {
    private final CarritoProductos contenido = new CarritoProductos();

    public void agregarProducto(Producto producto, int cantidad) {
        try {
            contenido.agregar(producto.getCodigo(), producto.getNombre(), producto.getCategoria(),
                    BigDecimal.valueOf(producto.getPrecio()), cantidad, producto.getStock());
            System.out.println("Se agregó " + producto.getNombre() + " al carrito.");
        } catch (IllegalArgumentException | ArithmeticException error) {
            System.out.println(error.getMessage());
        }
    }

    public void cambiarCantidad(Producto producto, int cantidad) {
        contenido.cambiarCantidad(producto.getCodigo(), cantidad, producto.getStock());
    }

    /** Compatibility snapshot for the legacy TXT writers; stock here means purchased units. */
    public List<Producto> getProductos() {
        return contenido.lineas().stream().map(linea -> new Producto(linea.codigo(), linea.nombre(),
                linea.precioUnitario().doubleValue(), linea.categoria(), linea.cantidad())).toList();
    }

    public BigDecimal calcularTotalDecimal() {
        return contenido.total();
    }

    public void mostrarCarrito() {
        if (contenido.lineas().isEmpty()) {
            System.out.println("\nTu carrito está vacío. Agrega productos a tu carrito ;).");
        } else {
            System.out.println("Productos en tu carrito: ");
            BigDecimal total = calcularTotalDecimal();
            System.out.println(String.format("%-10s %-25s %-10s %-10s", "CANT", "DESCRIPCIÓN", "PRECIO", "IMPORTE"));
            
            for (Producto producto : getProductos()) {
                BigDecimal importe = BigDecimal.valueOf(producto.getPrecio())
                        .multiply(BigDecimal.valueOf(producto.getStock()));
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
            for (Producto producto : getProductos()) {
                writer.write(producto.toString());
                writer.newLine();
            }
            System.out.println("El carrito ha sido guardado exitosamente.");
        } catch (IOException e) {
            System.out.println("Error al guardar el carrito: " + e.getMessage());
        }
    }

    public void eliminarProducto(String codigo) {
        contenido.eliminar(codigo);
    }
    
    public double calcularTotal() {
        return calcularTotalDecimal().doubleValue();
    }
    
    public void limpiarCarrito() {
        contenido.vaciar();
    }
}

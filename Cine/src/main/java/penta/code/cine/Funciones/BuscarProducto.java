package penta.code.cine.Funciones;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BuscarProducto {
    private static String archivoProductos = "StockProductos/";
    
    public static String buscarProductoPorID(String idProducto, String sucursal) {
        
        String path = archivoProductos + sucursal + "/stockProductos.txt";
        
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length >= 5 && datos[0].equals(idProducto)) {
                    String nombre = datos[1];
                    int stock = Integer.parseInt(datos[4]);
                    if (stock > 0) {
                        return "Producto encontrado: " + nombre + " -----> STOCK DISPONIBLE! ;).";
                    } else {
                        return "Producto encontrado: " + nombre + " -----> SIN STOCK DISPONIBLE :(. Pronto se agregará mas stock.";
                    }
                }
            }
        } catch (IOException e) {
            return "Error al leer los datos: " + e.getMessage();
        }
        return "El producto con ID '" + idProducto + "' no existe. Por favor asegurate de teclear un ID correcto.";
    }
}


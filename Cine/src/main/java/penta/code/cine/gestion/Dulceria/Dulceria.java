package penta.code.cine.gestion.Dulceria;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Dulceria {
    private List<Producto> productos;
    private static String archivoProductos = "StockProductos/";

    public Dulceria(String sucursalActual) {
        this.productos = cargarProductosDesdeArchivo(sucursalActual);
    }

    private List<Producto> cargarProductosDesdeArchivo(String sucursalActual) {
        List<Producto> lista = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(archivoProductos + sucursalActual + "/stockProductos.txt"))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",");
                String codigo = datos[0];
                String nombre = datos[1];
                double precio = Double.parseDouble(datos[2]);
                String categoria = datos[3];
                int stock = Integer.parseInt(datos[4]);
                lista.add(new Producto(codigo, nombre, precio, categoria, stock));
            }
        } catch (IOException e) {
            System.out.println("Error al cargar productos: " + e.getMessage());
        }
        return lista;
    }

    private void guardarProductoEnArchivo(Producto producto, String sucursalActual) {
        String archivoPath = archivoProductos + sucursalActual + "/stockProductos.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoPath, true))) {
            writer.write(producto.toString());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error al guardar el producto: " + e.getMessage());
        }
    }


    public void mostrarProductos() {
        System.out.println("--- Bebidas ---");
        for (Producto producto : productos) {
            if (producto.getCategoria().equalsIgnoreCase("Bebidas")) {
                System.out.println("\tCódigo: " + producto.getCodigo() + " | " + producto.getNombre() + ": $" + producto.getPrecio());
            }
        }
        System.out.println("--- Botanas ---");
        for (Producto producto : productos) {
            if (producto.getCategoria().equalsIgnoreCase("Botana")) {
                System.out.println("\tCódigo: " + producto.getCodigo() + " | " + producto.getNombre() + ": $" + producto.getPrecio());
            }
        }
        System.out.println("--- Dulces ---");
        for (Producto producto : productos) {
            if (producto.getCategoria().equalsIgnoreCase("Dulces")) {
                System.out.println("\tCódigo: " + producto.getCodigo() + " | " + producto.getNombre() + ": $" + producto.getPrecio());
            }
        }
        System.out.println("--- Comida ---");
        for (Producto producto : productos) {
            if (producto.getCategoria().equalsIgnoreCase("Comida")) {
                System.out.println("\tCódigo: " + producto.getCodigo() + " | " + producto.getNombre() + ": $" + producto.getPrecio());
            }
        }
        System.out.println("--- Souvenirs ---");
        for (Producto producto : productos) {
            if (producto.getCategoria().equalsIgnoreCase("Souvenirs")) {
                System.out.println("\tCódigo: " + producto.getCodigo() + " | " + producto.getNombre() + ": $" + producto.getPrecio());
            }
        }
    }

    public Producto buscarProducto(String codigo) {
        for (Producto producto : productos) {
            if (producto.getCodigo().equals(codigo)) {
                return producto;
            }
        }
        return null;
    }

    public void actualizarStock(String codigo, int cantidad, String sucursalActual) {
        Producto producto = buscarProducto(codigo);
        if (producto != null) {
            if (producto.getStock() >= cantidad) {
                producto.disminuirStock(cantidad);
                actualizarProductoEnArchivo(producto, sucursalActual);
            } else {
                System.out.println("Stock insuficiente para el producto: " + producto.getNombre());
            }
        } else {
            System.out.println("Producto no encontrado.");
        }
    }
    
    public void actualizarProductoEnArchivo(Producto productoActualizado, String sucursalActual) {
        String archivoPath = archivoProductos + sucursalActual + "/stockProductos.txt";
        File archivo = new File(archivoPath);

        List<String> lineas = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.startsWith(productoActualizado.getCodigo() + ",")) {
                    lineas.add(linea);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo para actualizar: " + e.getMessage());
            return;
        }

        lineas.add(productoActualizado.toString());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoPath))) {
            for (String linea : lineas) {
                writer.write(linea);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al escribir en el archivo actualizado: " + e.getMessage());
        }
    }

    public void agregarProducto(Producto producto, String sucursalActual) {
        if (!productos.contains(producto)) {
            productos.add(producto);
            guardarProductoEnArchivo(producto, sucursalActual);
        } else {
            System.out.println("El producto ya existe en la lista.");
        }
    }

}

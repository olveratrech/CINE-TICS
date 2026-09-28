package penta.code.cine.gestion.Dulceria;
import penta.code.cine.gestion.Clientes.Cliente;
import penta.code.cine.gestion.Datos.RegistroClientes;
import penta.code.cine.gestion.ProgramaLealtad.ProgramaLealtad;
import java.io.*;
import java.time.LocalDate;
import java.util.UUID;
import static penta.code.cine.Funciones.MetodosEnGeneral.Animacion;


/**
 *
 * @author olveratrech
 */
public class Venta {
    public void registrarVenta(Carrito carrito, String sucursal, Cliente cliente) throws InterruptedException {
        String fecha = LocalDate.now().toString();
        String basePath = "Ventas/" + sucursal + "/";
        String idTicket = UUID.randomUUID().toString().substring(0, 8);

        String archivoVentasPorDia = basePath + "VentasPorDia/" + fecha + "_" + sucursal + ".txt";
        String archivoVentasTotales = basePath + "VentasTotales/VentasTotales.txt";
        String archivoTicket = basePath + "Tickets/" + fecha + "_" + sucursal + "_" + idTicket + "_" + cliente.getIdCliente() + ".txt";

        crearCarpeta(basePath + "VentasPorDia/");
        crearCarpeta(basePath + "VentasTotales/");
        crearCarpeta(basePath + "Tickets/");
        
        double totalCompra = carrito.calcularTotal();
        
        if (cliente.isInscritoProgramaLealtad()) {
            cliente.getProgramaLealtad().agregarCompra(totalCompra);
            RegistroClientes.actualizarProgramaLealtad(cliente);
        }
        
        registrarEnArchivo(archivoVentasPorDia, carrito, sucursal, fecha, idTicket, cliente.getIdCliente(), totalCompra);
        registrarEnArchivo(archivoVentasTotales, carrito, sucursal, fecha, idTicket, cliente.getIdCliente(), totalCompra);
        generarTicket(archivoTicket, carrito, sucursal, fecha, idTicket);
        
        if (cliente.isInscritoProgramaLealtad()) {
            mostrarProgramaLealtad(cliente);
        }
    }

    private void registrarEnArchivo(String archivo, Carrito carrito, String sucursal, String fecha, String idTicket, String idCliente, double  totalCompra) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo, true))) {
            writer.write(idTicket + "," + idCliente + "," + fecha + "," + sucursal + "," + totalCompra);
            writer.newLine();

            for (Producto producto : carrito.getProductos()) {
                writer.write(producto.toString());
                writer.newLine();
            }
            writer.write("--------------------------------------------------");
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error al registrar en el archivo: " + e.getMessage());
        }
    }

    private void generarTicket(String archivoTicket, Carrito carrito, String sucursal, String fecha, String idTicket) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoTicket))) {
            writer.write(cineTicsTicket());
            writer.newLine();
            writer.write(sucursalTickets(sucursal));
            writer.newLine();
            writer.write("===================================================");
            writer.newLine();
            writer.write(String.format("\t%-20s: %s", "Fecha", fecha));
            writer.newLine();
            writer.write(String.format("\t%-20s: %s", "Sucursal", sucursal));
            writer.newLine();
            writer.write(String.format("\t%-20s: %s", "ID Ticket", idTicket));
            writer.newLine();
            writer.write("===================================================");
            writer.newLine();
            writer.write(String.format("%-10s %-25s %-10s %-10s", "CANT", "DESCRIPCIÓN", "PRECIO", "IMPORTE"));
            writer.newLine();
            writer.write("---------------------------------------------------");
            writer.newLine();

            java.math.BigDecimal total = carrito.calcularTotalDecimal();
            for (Producto producto : carrito.getProductos()) {
                java.math.BigDecimal importe = java.math.BigDecimal.valueOf(producto.getPrecio())
                        .multiply(java.math.BigDecimal.valueOf(producto.getStock()));
                writer.write(String.format("%-10d %-25s $%-9.2f $%-9.2f", 
                                           producto.getStock(), 
                                           producto.getNombre() + ", " + producto.getCategoria(), 
                                           producto.getPrecio(), 
                                           importe));
                writer.newLine();
            }

            writer.write("---------------------------------------------------");
            writer.newLine();
            writer.write(String.format("%-37s $%-15.2f", "TOTAL:", total));
            writer.newLine();
            writer.write("===================================================");
            writer.newLine();
            writer.write("Gracias por su compra. ¡Vuelva pronto!");
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error al generar el ticket: " + e.getMessage());
        }
    }
    
    public static void verMisCompras(String idCliente, String sucursal) {
        String archivoVentasTotales = "Ventas/" + sucursal + "/VentasTotales/VentasTotales.txt";
        boolean comprasEncontradas = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivoVentasTotales))) {
            String linea;
            System.out.println("\n=== Compras de Producto | Cliente: " + idCliente + " en " + sucursal + " ===");

            while ((linea = reader.readLine()) != null) {
                String[] encabezado = linea.split(",");
                if (encabezado.length == 5 && encabezado[1].equals(idCliente)) {
                    comprasEncontradas = true;

                    System.out.println("ID Compra: " + encabezado[0]);
                    System.out.println("Fecha: " + encabezado[2]);
                    System.out.println("Sucursal: " + encabezado[3]);
                    System.out.println("Total: $" + encabezado[4]);
                    System.out.println("Productos:");

                    while ((linea = reader.readLine()) != null && !linea.equals("--------------------------------------------------")) {
                        String[] producto = linea.split(",");
                        if (producto.length == 5) {
                            System.out.printf("  %-10s | %-20s | $%-8s | %-15s | Cantidad: %-5s%n",
                                              producto[0], 
                                              producto[1], 
                                              producto[2],
                                              producto[3],
                                              producto[4]);
                        }
                    }
                    System.out.println("--------------------------------------------------");    
                }
            }
            if (!comprasEncontradas) {
                System.out.println("No se encontraron compras registradas para este cliente en la sucursal: " + sucursal);
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo de ventas totales: " + e.getMessage());
        }
    }
    
    private void mostrarProgramaLealtad(Cliente cliente) throws InterruptedException {
        ProgramaLealtad programa = cliente.getProgramaLealtad();
        System.out.print("Cargando programa de lealtad ");
            for(int i = 0; i < 3; i++)
                Animacion();

        System.out.println("\n=== Programa de Lealtad ===");
        System.out.println("Nivel actual: " + programa.getNivel());
        System.out.println("Puntos acumulados: " + programa.getPuntos());
        System.out.println("Valor en dinero: " + programa.calcularDineroDePuntos());
        if (programa.getNivel() == 15) {
            System.out.println("¡Recuerda que por cada $500 de compra puedes obtener un producto gratuito!");
        }
        System.out.println("===========================");
    }


    private void crearCarpeta(String rutaCarpeta) {
        File carpeta = new File(rutaCarpeta);
        if (!carpeta.exists()) {
            if (carpeta.mkdirs()) {
                System.out.println("Carpeta creada: " + rutaCarpeta);
            } else {
                System.out.println("Error al crear la carpeta: " + rutaCarpeta);
            }
        }
    }
    
    private String cineTicsTicket() {
        return ("\n░█████╗░██╗███╗░░██╗███████╗░░░░░░████████╗██╗░█████╗░░██████╗" +
                "\n██╔══██╗██║████╗░██║██╔════╝░░░░░░╚══██╔══╝██║██╔══██╗██╔════╝" +
                "\n██║░░╚═╝██║██╔██╗██║█████╗░░█████╗░░░██║░░░██║██║░░╚═╝╚█████╗░" +
                "\n██║░░██╗██║██║╚████║██╔══╝░░╚════╝░░░██║░░░██║██║░░██╗░╚═══██╗" +
                "\n╚█████╔╝██║██║░╚███║███████╗░░░░░░░░░██║░░░██║╚█████╔╝██████╔╝" +
                "\n░╚════╝░╚═╝╚═╝░░╚══╝╚══════╝░░░░░░░░░╚═╝░░░╚═╝░╚════╝░╚═════╝░");
    }
    
    private String sucursalTickets(String sucursal) {
        if (sucursal.equalsIgnoreCase("Cine-TICS 'CU'")) {
            return "\n▒█▀▀█ ▒█░▒█" +
                   "\n▒█░░░ ▒█░▒█" +
                   "\n▒█▄▄█ ░▀▄▄▀";
        } else if (sucursal.equalsIgnoreCase("Cine-TICS 'Universidad'")) {
            return "\n█░░█ █▀▀▄ ░▀░ ▀█░█▀ █▀▀ █▀▀█ █▀▀ ░▀░ █▀▀▄ █▀▀█ █▀▀▄" +
                   "\n█░░█ █░░█ ▀█▀ ░█▄█░ █▀▀ █▄▄▀ ▀▀█ ▀█▀ █░░█ █▄▄█ █░░█" +
                   "\n░▀▀▀ ▀░░▀ ▀▀▀ ░░▀░░ ▀▀▀ ▀░▀▀ ▀▀▀ ▀▀▀ ▀▀▀░ ▀░░▀ ▀▀▀░";
        } else if (sucursal.equalsIgnoreCase("Cine-TICS 'Delta'")) {
            return  "\n▒█▀▀▄ █▀▀ █░░ ▀▀█▀▀ █▀▀█" +
                    "\n▒█░▒█ █▀▀ █░░ ░░█░░ █▄▄█" +
                    "\n▒█▄▄▀ ▀▀▀ ▀▀▀ ░░▀░░ ▀░░▀";
        } else if (sucursal.equalsIgnoreCase("Cine-TICS 'Xochimilco'")) {
            return  "\n▀▄▒▄▀ █▀▀█ █▀▀ █░░█ ░▀░ █▀▄▀█ ░▀░ █░░ █▀▀ █▀▀█" +
                    "\n░▒█░░ █░░█ █░░ █▀▀█ ▀█▀ █░▀░█ ▀█▀ █░░ █░░ █░░█" +
                    "\n▄▀▒▀▄ ▀▀▀▀ ▀▀▀ ▀░░▀ ▀▀▀ ▀░░░▀ ▀▀▀ ▀▀▀ ▀▀▀ ▀▀▀▀";
        }
        return null;
    }
}

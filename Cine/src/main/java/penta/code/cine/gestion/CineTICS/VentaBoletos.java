package penta.code.cine.gestion.CineTICS;
import penta.code.cine.gestion.Clientes.Cliente;
import penta.code.cine.gestion.ProgramaLealtad.ProgramaLealtad;
import java.io.*;
import java.time.LocalDate;
import java.util.UUID;
import static penta.code.cine.Funciones.MetodosEnGeneral.Animacion;
import penta.code.cine.gestion.Datos.RegistroClientes;

public class VentaBoletos {
    public void registrarVenta(CarritoBoletos carrito, String sucursal, Cliente cliente) throws InterruptedException {
        String fecha = LocalDate.now().toString();
        String basePath = "VentasBoletos/" + sucursal + "/";
        String idTicket = UUID.randomUUID().toString().substring(0, 8);

        String archivoVentasPorDia = basePath + "VentasPorDia/" + fecha + "_" + sucursal + ".txt";
        String archivoVentasTotales = basePath + "VentasTotales/VentasTotales.txt";
        String archivoTicket = basePath + "Tickets/" + fecha + "_" + sucursal + "_" + idTicket + "_" + cliente.getIdCliente() + ".txt";

        crearCarpeta(basePath + "VentasPorDia/");
        crearCarpeta(basePath + "VentasTotales/");
        crearCarpeta(basePath + "Tickets/");

        double totalCompra = carrito.boletos.stream()
                .mapToDouble(producto -> producto.getPrecio())
                .sum();
        
        if (cliente.isInscritoProgramaLealtad()) {
            cliente.getProgramaLealtad().agregarCompra(totalCompra);
            RegistroClientes.actualizarProgramaLealtad(cliente);
        }
        
        registrarEnArchivo(archivoVentasPorDia, carrito, sucursal, fecha, idTicket, cliente.getIdCliente());
        registrarEnArchivo(archivoVentasTotales, carrito, sucursal, fecha, idTicket, cliente.getIdCliente());
        generarTicket(archivoTicket, carrito, sucursal, fecha, idTicket);
        
        if (cliente.isInscritoProgramaLealtad()) {
            mostrarProgramaLealtad(cliente);
        }
    }

    private void registrarEnArchivo(String archivo, CarritoBoletos carrito, String sucursal, String fecha, String idTicket, String idCliente) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo, true))) {
            double total = carrito.calcularTotal();
            writer.write(idTicket + "," + idCliente + "," + fecha + "," + sucursal + "," + total);
            writer.newLine();

            for (Boleto boleto : carrito.boletos) {
                writer.write(boleto.toString());
                writer.newLine();
            }
            writer.write("--------------------------------------------------");
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error al registrar en el archivo: " + e.getMessage());
        }
    }
    
    private void generarTicket(String archivoTicket, CarritoBoletos carrito, String sucursal, String fecha, String idTicket) {
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
            writer.write(String.format("%-10s %-25s %-10s %-10s", "CANT", "DESCRIPCIÓN", "PRECIO", "ASIENTO"));
            writer.newLine();
            writer.write("---------------------------------------------------");
            writer.newLine();
            double total = 0;
            for (Boleto boleto : carrito.boletos) {
                double importe = boleto.getPrecio();
                total += importe;
                writer.write(String.format("%-10d %-25s $%-9.2f %-9s", 
                                           1, 
                                           boleto.getFuncion().getPelicula().getTitulo(), 
                                           boleto.getPrecio(), 
                                           boleto.getAsiento().numeroAsiento));
                writer.newLine();
            }

            writer.write("--------------------------------------------------");
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
        String archivoVentasTotales = "VentasBoletos/" + sucursal + "/VentasTotales/VentasTotales.txt";
        boolean comprasEncontradas = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivoVentasTotales))) {
            String linea;
            System.out.println("\n=== Compras de Boletos | Cliente: " + idCliente + " en " + sucursal + " ===");

            while ((linea = reader.readLine()) != null) {
                String[] encabezado = linea.split(",");
                if (encabezado.length == 5 && encabezado[1].equals(idCliente)) {
                    comprasEncontradas = true;

                    System.out.println("ID Compra: " + encabezado[0]);
                    System.out.println("Fecha: " + encabezado[2]);
                    System.out.println("Sucursal: " + encabezado[3]);
                    System.out.println("Total: $" + encabezado[4]);
                    System.out.println("Boletos:");

                    while ((linea = reader.readLine()) != null && !linea.equals("--------------------------------------------------")) {
                        String[] boletos = linea.split(",");
                        if (boletos.length == 6) {
                            System.out.printf("  %-10s | %-10s | %-25s | %-20s | %-6s |$%-6s%n",
                                              boletos[0], 
                                              boletos[1], 
                                              boletos[2],
                                              boletos[3],
                                              boletos[4],
                                              boletos[5]);
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
        System.out.println("Valor en dinero: $" + programa.calcularDineroDePuntos());
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

package penta.code.cine.gestion.Datos;
import penta.code.cine.gestion.Clientes.Cliente;
import penta.code.cine.gestion.Financiera.CuentaBancaria;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import penta.code.cine.gestion.ProgramaLealtad.ProgramaLealtad;

/**
 *
 * @author olveratrech
 */
public class RegistroClientes {
    private static final String ARCHIVO_CLIENTES = "registroClientes/datos_Clientes.txt";
    private static final String BASE_PATH_PROGRAMA_LEALTAD = "ProgramaLealtad/";

    public static boolean registrarCliente(Cliente cliente) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_CLIENTES, true))) {
            writer.write(clienteToCSV(cliente));
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error al registrar el cliente: " + e.getMessage());
            return false;
        }
    }

    public static List<Cliente> cargarClientes() throws IOException {
        List<Cliente> clientes = new ArrayList<>();
        File archivo = new File(ARCHIVO_CLIENTES);
        if (!archivo.exists()) {
            return clientes;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_CLIENTES))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                Cliente cliente = csvToCliente(linea);
                if (cliente != null) {
                    clientes.add(cliente);
                }
            }
        }

        return clientes;
    }

    public static void mostrarClientes() throws IOException {
        List<Cliente> clientes = cargarClientes();
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados.");
        } else {
            for (Cliente cliente : clientes) {
                System.out.println(cliente);
            }
        }
    }

    private static String clienteToCSV(Cliente cliente) {
        return  cliente.getIdCliente()         + "," +
                cliente.getUsuario()           + "," +
                cliente.getCorreo()            + "," +
                cliente.getContrasena()        + "," +
                cliente.getNombre()            + "," +
                cliente.getApellidoPaterno()   + "," +
                cliente.getApellidoMaterno()   + "," +
                cliente.getFechaDeNacimiento() + "," +
                cliente.getEdad()              + "," +
                cliente.getGenero()            + "," +
                cliente.getDireccion()         + "," +
                cliente.getNumeroDeTelefono()  + "," +
                cliente.getRFC()               + "," +
                cliente.getEstadoCliente()     + "," +
                cliente.isInscritoProgramaLealtad();
    }

    private static Cliente csvToCliente(String linea) {
        try {
            String[] campo = linea.split(",");
            String idCliente =          campo[0];
            String usuario =            campo[1];
            String correo =             campo[2];
            String contrasena =         campo[3];
            String nombre =             campo[4];
            String apPaterno =          campo[5];
            String apMaterno =          campo[6];
            String fechaNacimiento =    campo[7];
            int edad = Integer.parseInt(campo[8]);
            String genero =             campo[9];
            String direccion =          campo[10];
            String numTelefono =        campo[11];
            String rfc =                campo[12];
            boolean estadoCliente = Boolean.parseBoolean(campo[13]);
            boolean inscritoProgramaLealtad = Boolean.parseBoolean(campo[14]);

            Cliente cliente = new Cliente(idCliente, usuario, correo, contrasena, nombre, apPaterno, apMaterno, 
                               fechaNacimiento, edad, genero, direccion, numTelefono, rfc, estadoCliente);
            
            if (inscritoProgramaLealtad) {
                cliente.unirseProgramaLealtad();
            }
            
            return cliente;
        } catch (Exception e) {
            System.err.println("Error al leer cliente: " + e.getMessage());
            return null;
        }
    }
    
    public static boolean actualizarCliente(Cliente clienteActualizado) {
        try {
            List<Cliente> clientes = cargarClientes();
            boolean clienteEncontrado = false;

            for (int i = 0; i < clientes.size(); i++) {
                if (clientes.get(i).getIdCliente().equals(clienteActualizado.getIdCliente())) {
                    clientes.set(i, clienteActualizado);
                    clienteEncontrado = true;
                    break;
                }
            }
            if (!clienteEncontrado) {
                System.out.println("Cliente no encontrado. No se realizó la actualización.");
                return false;
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_CLIENTES))) {
                for (Cliente cliente : clientes) {
                    writer.write(clienteToCSV(cliente));
                    writer.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error al actualizar el cliente: " + e.getMessage());
            return false;
        }
    }
    
    public static void actualizarProgramaLealtad(Cliente cliente) {
        if (!cliente.isInscritoProgramaLealtad()) {
            System.out.println("El cliente no está inscrito en el programa de lealtad.");
            return;
        }

        String archivoLealtad = BASE_PATH_PROGRAMA_LEALTAD + "/" + cliente.getIdCliente() + ".txt";

        crearCarpeta(archivoLealtad);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoLealtad, false))) {
            writer.write(cliente.getIdCliente() + "," + cliente.getNombre() + "," + cliente.getApellidoPaterno() + "," + cliente.getApellidoMaterno() + "," +
                         cliente.getProgramaLealtad().getNivel() + "," + cliente.getProgramaLealtad().getPuntos() + "," + cliente.getProgramaLealtad().calcularDineroDePuntos() + "," +
                         cliente.isInscritoProgramaLealtad());
            writer.newLine();
            //System.out.println("Se actualizó el programa de lealtad para el cliente: " + cliente.getIdCliente());
        } catch (IOException e) {
            System.out.println("Error al actualizar el  programa de lealtad: " + e.getMessage());
        }
    }
    
    public static Cliente cargarProgramaLealtadCliente(Cliente cliente) {
        String archivoLealtad = BASE_PATH_PROGRAMA_LEALTAD + "/" + cliente.getIdCliente() + ".txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(archivoLealtad))) {
            String linea;
            while ((linea = reader.readLine()) != null) {                
                String[] campos = linea.split(",");
                if (campos.length == 7) {
                    int nivel = Integer.parseInt(campos[4]);
                    double puntos = Double.parseDouble(campos[5]);
                    boolean inscrito = Boolean.parseBoolean(campos[6]);
                    return new Cliente(nivel, puntos, inscrito);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar el archivo.");
        }
        return null;
    }

    private static void crearCarpeta(String rutaCarpeta) {
        File carpeta = new File(rutaCarpeta);
        if (!carpeta.exists()) {
            if (carpeta.mkdirs()) {
                System.out.println("Carpeta creada: " + rutaCarpeta);
            } else {
                System.out.println("Error al crear la carpeta: " + rutaCarpeta);
            }
        }
    }


}

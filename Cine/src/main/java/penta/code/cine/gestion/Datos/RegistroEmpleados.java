package penta.code.cine.gestion.Datos;
import penta.code.cine.gestion.Personal.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 
 */
public class RegistroEmpleados {
    private static final String ARCHIVO_AYUDANTES = "registroEmpleados/datos_AyudanteGeneral.txt";
    private static final String ARCHIVO_CAJEROS = "registroEmpleados/datos_Cajero.txt";
    private static final String ARCHIVO_GERENTES = "registroEmpleados/datos_Gerentes.txt";

    public static void registrarAyudanteGeneral(AyudanteGeneral ayudante) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_AYUDANTES, true))) {
            writer.write(ayudanteToCSV(ayudante));
            writer.newLine();
        }
        System.out.println("Ayudante General registrado con éxito.");
    }

    public static void registrarCajero(Cajero cajero) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_CAJEROS, true))) {
            writer.write(cajeroToCSV(cajero));
            writer.newLine();
        }
        System.out.println("Cajero registrado con éxito.");
    }
    
    public static void registrarGerente(Gerente gerente) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_GERENTES, true))) {
            writer.write(gerenteToCSV(gerente));
            writer.newLine();
        }
        System.out.println("Gerente registrado con éxito.");
    }

    public static List<AyudanteGeneral> cargarAyudantesGenerales() throws IOException {
        List<AyudanteGeneral> ayudantes = new ArrayList<>();
        File archivo = new File(ARCHIVO_AYUDANTES);
        if (!archivo.exists()) {
            return ayudantes;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_AYUDANTES))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                AyudanteGeneral ayudante = csvToAyudanteGeneral(linea);
                if (ayudante != null) {
                    ayudantes.add(ayudante);
                }
            }
        }

        return ayudantes;
    }

    public static List<Cajero> cargarCajeros() throws IOException {
        List<Cajero> cajeros = new ArrayList<>();
        File archivo = new File(ARCHIVO_CAJEROS);
        if (!archivo.exists()) {
            return cajeros;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_CAJEROS))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                Cajero cajero = csvToCajero(linea);
                if (cajero != null) {
                    cajeros.add(cajero);
                }
            }
        }

        return cajeros;
    }
    
    public static List<Gerente> cargarGerentes() throws IOException {
        List<Gerente> gerentes = new ArrayList<>();
        File archivo = new File(ARCHIVO_GERENTES);
        if (!archivo.exists()) {
            return gerentes;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_GERENTES))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                Gerente gerente = csvToGerente(linea);
                if (gerente != null) {
                    gerentes.add(gerente);
                }
            }
        }

        return gerentes;
    }

    public static void mostrarAyudantesGenerales() throws IOException {
        List<AyudanteGeneral> ayudantes = cargarAyudantesGenerales();
        if (ayudantes.isEmpty()) {
            System.out.println("No hay ayudantes generales registrados.");
        } else {
            for (AyudanteGeneral ayudante : ayudantes) {
                System.out.println(ayudante);
            }
        }
    }

    public static void mostrarCajeros() throws IOException {
        List<Cajero> cajeros = cargarCajeros();
        if (cajeros.isEmpty()) {
            System.out.println("No hay cajeros registrados.");
        } else {
            for (Cajero cajero : cajeros) {
                System.out.println(cajero);
            }
        }
    }
    
    public static void mostrarGerentes() throws IOException {
        List<Gerente> gerentes = cargarGerentes();
        if (gerentes.isEmpty()) {
            System.out.println("No hay cajeros registrados.");
        } else {
            for (Gerente gerente : gerentes) {
                System.out.println(gerente);
            }
        }
    }

    private static String ayudanteToCSV(AyudanteGeneral ayudante) {
        return  ayudante.getIdEmpleado()          + "," +
                ayudante.getContrasena()          + "," +
                ayudante.getPuesto()              + "," +
                ayudante.getAreaAyudanteGeneral() + "," +
                ayudante.getSucursal()            + "," +
                ayudante.getNombre()              + "," +
                ayudante.getApellidoPaterno()     + "," +
                ayudante.getApellidoMaterno()     + "," +
                ayudante.getCorreo()              + "," +
                ayudante.getNumeroDeTelefono()    + "," +
                ayudante.getDireccion()           + "," +
                ayudante.getRFC()                 + "," +
                ayudante.getFechaDeNacimiento()   + "," +
                ayudante.getEdad()                + "," +
                ayudante.getGenero()              + "," +
                ayudante.getSalario()             + "," +
                ayudante.getFechaDeContratacion() + "," +
                ayudante.getIdNominaEmpleado()    + "," +
                ayudante.getEstadoEmpleado();
    }

    private static String cajeroToCSV(Cajero cajero) {
        return  cajero.getIdEmpleado()          + "," +
                cajero.getContrasena()          + "," +
                cajero.getPuesto()              + "," +
                cajero.getNumCaja()             + "," +
                cajero.getAreaCajero()          + "," +
                cajero.getSucursal()            + "," +
                cajero.getNombre()              + "," +
                cajero.getApellidoPaterno()     + "," +
                cajero.getApellidoMaterno()     + "," +
                cajero.getCorreo()              + "," +
                cajero.getNumeroDeTelefono()    + "," +
                cajero.getDireccion()           + "," +
                cajero.getRFC()                 + "," +
                cajero.getFechaDeNacimiento()   + "," +
                cajero.getEdad()                + "," +
                cajero.getGenero()              + "," +
                cajero.getSalario()             + "," +
                cajero.getComisionCajero()      + "," +
                cajero.getFechaDeContratacion() + "," +
                cajero.getIdNominaEmpleado()    + "," +
                cajero.getEstadoEmpleado();
    }
    
    private static String gerenteToCSV(Gerente gerente) {
        return  gerente.getIdEmpleado()          + "," +
                gerente.getContrasena()          + "," +
                gerente.getPuesto()              + "," +
                gerente.getSucursal()            + "," +
                gerente.getNombre()              + "," +
                gerente.getApellidoPaterno()     + "," +
                gerente.getApellidoMaterno()     + "," +
                gerente.getCorreo()              + "," +
                gerente.getNumeroDeTelefono()    + "," +
                gerente.getDireccion()           + "," +
                gerente.getRFC()                 + "," +
                gerente.getFechaDeNacimiento()   + "," +
                gerente.getEdad()                + "," +
                gerente.getGenero()              + "," +
                gerente.getSalario()             + "," +
                gerente.getBono()                + "," +
                gerente.getFechaDeContratacion() + "," +
                gerente.getIdNominaEmpleado()    + "," +
                gerente.getEstadoEmpleado();
    }

    private static AyudanteGeneral csvToAyudanteGeneral(String linea) {
        try {
            String[] campo = linea.split(",");

            String idEmpleado = (campo[0]);
            String contrasena = (campo[1]);
            String puesto = (campo[2]);
            String areaAyudanteGeneral = (campo[3]);
            String sucursal = (campo[4]);
            String nombre = (campo[5]);
            String apPaterno = (campo[6]);
            String apMaterno = (campo[7]);
            String correo = (campo[8]);
            String numTelefono = (campo[9]);
            String direccion = (campo[10]);
            String rfc = (campo[11]);
            String fechaDeNacimiento = (campo[12]);
            int edad = (Integer.parseInt(campo[13]));
            String genero = (campo[14]);
            double salario = (Double.parseDouble(campo[15]));
            String fechaDeContratacion = (campo[16]);
            String idNominaEmpleado = (campo[17]);
            boolean estadoEmpleado = (Boolean.parseBoolean(campo[18]));
            
            return new AyudanteGeneral(idEmpleado, contrasena, puesto, areaAyudanteGeneral, sucursal, nombre, apPaterno, apMaterno, 
                                       correo, numTelefono, direccion, rfc, fechaDeNacimiento, edad, genero, salario, fechaDeContratacion,
                                       idNominaEmpleado, estadoEmpleado);
        } catch (Exception e) {
            System.err.println("Error al leer AyudanteGeneral: " + e.getMessage());
            return null;
        }
    }

    private static Cajero csvToCajero(String linea) {
        try {
            String[] campo = linea.split(",");
            
            String idEmpleado = (campo[0]);
            String contrasena = (campo[1]);
            String puesto = (campo[2]);
            int numCaja = Integer.parseInt(campo[3]);
            String areaCajero = campo[4];
            String sucursal = (campo[5]);
            String nombre = (campo[6]);
            String apPaterno = (campo[7]);
            String apMaterno = (campo[8]);
            String correo = (campo[9]);
            String numTelefono = (campo[10]);
            String direccion = (campo[11]);
            String rfc = (campo[12]);
            String fechaDeNacimiento = (campo[13]);
            int edad = Integer.parseInt(campo[14]);
            String genero = (campo[15]);
            double salario = Double.parseDouble(campo[16]);
            double comisionCajero = Double.parseDouble(campo[17]);
            String fechaDeContratacion = (campo[18]);
            String idNominaEmpleado = (campo[19]);
            boolean estadoEmpleado = Boolean.parseBoolean(campo[20]);
            
            return new Cajero(idEmpleado, contrasena, puesto, numCaja, areaCajero, sucursal, nombre, apPaterno, apMaterno, 
                                       correo, numTelefono, direccion, rfc, fechaDeNacimiento, edad, genero, salario, comisionCajero, 
                                       fechaDeContratacion, idNominaEmpleado, estadoEmpleado);
        } catch (Exception e) {
            System.err.println("Error al leer Cajero: " + e.getMessage());
            return null;
        }
    }
    
    private static Gerente csvToGerente(String linea) {
        try {
            String[] campo = linea.split(",");
            
            String idEmpleado = (campo[0]);
            String contrasena = (campo[1]);
            String puesto = (campo[2]);
            String sucursal = (campo[3]);
            String nombre = (campo[4]);
            String apPaterno = (campo[5]);
            String apMaterno = (campo[6]);
            String correo = (campo[7]);
            String numTelefono = (campo[8]);
            String direccion = (campo[9]);
            String rfc = (campo[10]);
            String fechaDeNacimiento = (campo[11]);
            int edad = Integer.parseInt(campo[12]);
            String genero = (campo[13]);
            double salario = Double.parseDouble(campo[14]);
            double bono = Double.parseDouble(campo[15]);
            String fechaDeContratacion = (campo[16]);
            String idNominaEmpleado = (campo[17]);
            boolean estadoEmpleado = Boolean.parseBoolean(campo[18]);
            
            return new Gerente(idEmpleado, contrasena, puesto, sucursal, nombre, apPaterno, apMaterno, 
                                       correo, numTelefono, direccion, rfc, fechaDeNacimiento, edad, genero, salario, bono, 
                                       fechaDeContratacion, idNominaEmpleado, estadoEmpleado);
        } catch (Exception e) {
            System.err.println("Error al leer Cajero: " + e.getMessage());
            return null;
        }
    }
}
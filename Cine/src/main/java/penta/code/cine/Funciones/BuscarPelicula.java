package penta.code.cine.Funciones;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import penta.code.cine.gestion.Funciones.Pelicula;

/**
 *
 * @author olveratrech
 */
public class BuscarPelicula {
    public static String buscarPeliculaEnSucursal(String idPelicula, File sucursal) {
        File[] archivos = sucursal.listFiles((dir, name) -> name.endsWith(".txt"));
        if (archivos == null || archivos.length == 0) {
            return null;
        }

        for (File archivo : archivos) {
            String resultado = buscarPeliculaEnArchivo(idPelicula, archivo);
            if (resultado != null) {
                return "Sucursal: " + sucursal.getName() + "\n" + resultado;
            }
        }

        return null;
    }
    
    private static String buscarPeliculaEnArchivo(String idPelicula, File archivo) {
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datosFuncion = linea.split(",");
                if (datosFuncion[1].equals(idPelicula)) {
                    String idFuncion = datosFuncion[0];
                    String horario = datosFuncion[2];
                    return "Sala: " + archivo.getName().replaceAll("\\D", "") + "\nID Función: " + idFuncion + ", Horario: " + horario;
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo " + archivo.getName() + ": " + e.getMessage());
        }

        return null;
    }

    public static String buscarPeliculaEnTodasSucursales(String idPelicula, String directorioCines) {
        File carpetaCines = new File(directorioCines);
        File[] sucursales = carpetaCines.listFiles(File::isDirectory);

        if (sucursales == null || sucursales.length == 0) {
            return "No se encontraron sucursales.";
        }

        for (File sucursal : sucursales) {
            String resultado = buscarPeliculaEnSucursal(idPelicula, sucursal);
            if (resultado != null) {
                return resultado;
            }
        }

        return "La película con ID " + idPelicula + " no se encuentra en ninguna sucursal.";
    }

    public static void buscarPelicula() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String directorioCines = "Funciones";
        
        List<Pelicula> peliculas = Pelicula.cargarPeliculasDesdeArchivo();
        System.out.println("\tID\t    Nombre");
        for (Pelicula pelicula : peliculas) {
            System.out.println("\t" + pelicula.getId() + "\t    " + pelicula.getTitulo());
        }

        try {
            System.out.print("Ingrese el ID de la película que desea buscar (formato PE-XXX): ");
            String idPelicula = reader.readLine();

            System.out.print("¿Desea buscar en una sucursal específica? (S/N): ");
            String opcion = reader.readLine();

            if (opcion.equalsIgnoreCase("s") || opcion.equalsIgnoreCase("S")) {
                System.out.print("Ingrese el nombre de la sucursal (ejemplo: 'CU', 'Universidad', 'Delta', 'Xochimilco'): ");
                String nombreSucursal = reader.readLine();
                File sucursal = new File(directorioCines + "/Cine-TICS '" + nombreSucursal + "'");

                if (sucursal.exists() && sucursal.isDirectory()) {
                    String resultado = buscarPeliculaEnSucursal(idPelicula, sucursal);
                    if (resultado != null) {
                        System.out.println("Película encontrada:\n" + resultado);
                    } else {
                        System.out.println("La película no se encuentra en la sucursal seleccionada.");
                        System.out.print("¿Desea buscar en todas las sucursales? (S/N): ");
                        String buscarEnTodas = reader.readLine();

                        if (buscarEnTodas.equalsIgnoreCase("s") || buscarEnTodas.equalsIgnoreCase("S")) {
                            String resultadoGeneral = buscarPeliculaEnTodasSucursales(idPelicula, directorioCines);
                            System.out.println(resultadoGeneral);
                        }
                    }
                } else {
                    System.out.println("La sucursal especificada no existe.");
                }
            } else {
                String resultado = buscarPeliculaEnTodasSucursales(idPelicula, directorioCines);
                System.out.println(resultado);
            }
        } catch (IOException e) {
            System.out.println("Error al leer los datos: " + e.getMessage());
        }
    }
}

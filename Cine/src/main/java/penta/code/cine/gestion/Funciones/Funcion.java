package penta.code.cine.gestion.Funciones;
import java.io.*;
import java.util.*;

public class Funcion {
    private String id;
    private Pelicula pelicula;
    private String horaInicio;

    public Funcion(String id, Pelicula pelicula, String horaInicio) {
        this.id = id;
        this.pelicula = pelicula;
        this.horaInicio = horaInicio;
    }

    public String getId() {
        return this.id;
    }

    public Pelicula getPelicula() {
        return this.pelicula;
    }

    public String getHoraInicio() {
        return this.horaInicio;
    }
    

    @Override
    public String toString() {
        return "ID Función: " + id + "\nPelicula:\n" + pelicula + "\nHora de inicio: " + horaInicio;
    }
    
    public static void guardarFuncionEnArchivo(Funcion funcion, String nombreSuc, int idSala) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Funciones/" + nombreSuc + "/sala_" + idSala + "funciones.txt", true))) {
            writer.write(funcion.id + "," + funcion.pelicula.getId() + "," + funcion.horaInicio);
            writer.newLine();
            
            System.out.println("Funcion guardada en el archivo.");
            
        } catch (IOException e) {
            System.out.println("Error al guardar las funciones: " + e.getMessage());
        }
    }
    
    public static List<Funcion> cargarFuncionesDesdeArchivo(String nombreSuc, int idSala) {
        List<Funcion> funciones = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader("Funciones/" + nombreSuc + "/sala_" + idSala + "funciones.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String id = data[0];
                Pelicula pelicula = buscarPeliculaPorId(data[1]);
                String horaInicio = data[2];
                funciones.add(new Funcion(id, pelicula, horaInicio));
            }
        } catch (IOException e) {
            System.out.println("Error al cargar las películas: " + e.getMessage());
        }
        return funciones;
    }
    
    public static Pelicula buscarPeliculaPorId(String idBusqueda) {
        try (BufferedReader reader = new BufferedReader(new FileReader("Peliculas/peliculas.txt"))) {
            String linea;
            
            while ((linea = reader.readLine()) != null) { 
                String[] data = linea.split(",");
                String idPelicula = data[0];
                
                if (idPelicula.equals(idBusqueda)) {
                    String titulo = data[1];
                    String duracion = data[2];
                    String genero = data[3];
                    
                    return new Pelicula(idPelicula, titulo, duracion, genero);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Error en el formato del archivo: " + e.getMessage());
        }
        System.out.println("Pelicula con ID: " + idBusqueda + " no encontrada.");
        return null;
    }
    
    public static Funcion buscarFuncionPorId(String idFuncion, String nombreSucursal, int idSala) {
        List<Funcion> funciones = cargarFuncionesDesdeArchivo(nombreSucursal, idSala);
        
        for (Funcion funcion : funciones) {
            if (funcion.getId().equals(idFuncion)) {
                return funcion;
            }
        }
        System.out.println("Función con ID: " + idFuncion + " no encontrada.");
        return null;
    }
}
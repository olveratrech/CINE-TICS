package penta.code.cine.gestion.Funciones;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Pelicula implements Serializable {
    private String id;
    private String titulo;
    private String duracion;
    private String genero;
    public Pelicula(String id, String titulo, String duracion, String genero) {
        this.id = id;
        this.titulo = titulo;
        this.duracion = duracion;
        this.genero = genero;
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDuracion() {
        return duracion;
    }

    public String getGenero() {
        return genero;
    }

    @Override
    public String toString() {
        return "\n\tID Película: " + id + "\n\tTítulo: " + titulo + "\n\tDuración: " + duracion + " horas\n\tGénero: " + genero + "\n";
    }

    public static void guardarPeliculaEnArchivo(Pelicula pelicula) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Peliculas/peliculas.txt", true))) {
            writer.write(pelicula.getId() + "," + pelicula.getTitulo() + "," + pelicula.getDuracion() + "," + pelicula.getGenero());
            writer.newLine();
     
            System.out.println("Película registrada con éxito en el archivo.");
        } catch (IOException e) {
            System.out.println("Error al guardar la película: " + e.getMessage());
        }
    }

    public static List<Pelicula> cargarPeliculasDesdeArchivo() {
        List<Pelicula> peliculas = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader("Peliculas/peliculas.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String id = data[0];
                String titulo = data[1];
                String duracion = data[2];
                String genero = data[3];
                peliculas.add(new Pelicula(id, titulo, duracion, genero));
            }
        } catch (IOException e) {
            System.out.println("Error al cargar las películas: " + e.getMessage());
        }
        return peliculas;
    }
    
    public static Pelicula buscarPeliculaPorId(String idBusqueda) {
        File archivoPeliculas = new File("Peliculas/peliculas.txt");
        if (!archivoPeliculas.exists()) {
            System.out.println("El archivo de películas no existe.");
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivoPeliculas))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] data = linea.split(",");
                if (data.length < 4) {
                    System.out.println("Formato inválido en la línea: " + linea);
                    continue;
                }
                String idPelicula = data[0].trim();
                if (idPelicula.equals(idBusqueda)) {
                    String titulo = data[1].trim();
                    String duracion = data[2].trim();
                    String genero = data[3].trim();
                    return new Pelicula(idPelicula, titulo, duracion, genero);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Error en el formato del archivo: " + e.getMessage());
        }

        System.out.println("Película con ID: " + idBusqueda + " no encontrada.");
        return null;
    }

}

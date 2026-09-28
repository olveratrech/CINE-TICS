package penta.code.cine.gestion.CineTICS;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author olveratrech
 */
public class Sucursal {
    // Atributos
    protected String nombreSucursal;
    protected List<Sala> salas;
    
    public Sucursal(String nombreSucursal) {
        this.nombreSucursal = nombreSucursal;
        this.salas = new ArrayList<>();
    }
    
    // GETTERS
    
    public String getNombreSucursal() {
        return nombreSucursal;
    }

    public List<Sala> getSalas() {
        return salas;
    }
    
    // MÉTODOS
    
    public void addSalas() { 
        Scanner leeDatos = new Scanner(System.in);
        int numeroDeSalas, filas, asientosPorFila;
        System.out.println("Número de salas: ");
        numeroDeSalas = leeDatos.nextInt();
        
        for (int i = 0; i < numeroDeSalas; i++) {
            int idSala = i + 1;
            System.out.println("Numero de filas de la sala " + idSala + ": ");
            filas = leeDatos.nextInt();
            System.out.println("Numero de sientos por fila de la sala " + idSala + ": ");
            asientosPorFila = leeDatos.nextInt();
            Sala nuevaSala = new Sala(idSala, filas, asientosPorFila);
            salas.add(nuevaSala);
        }
    }
    
    public void showSalas() {
        for (Sala sala : salas) {
            System.out.println("ID Sala: " + sala.getIdSala());
        }
    } 
    
    public void showAsientosSalas(int idSala) {
        salas.get(idSala).showAsientos();
    } 
    
    public void seleccionarAsientoSucursal(int idSala, String numeroAsiento) {
        Sala sala = findSalaPorId(idSala);
        if (sala != null) {
            sala.seleccionarAsiento(numeroAsiento);
            guardarEstadoDeSalasEnArchivo();
        } else {
            System.out.println("Sala no encontrada.");
        }
    }
    
    public void liberarAsientoSucursal(int idSala, String numeroAsiento) {
        Sala sala = findSalaPorId(idSala);
        if (sala != null) {
            sala.liberarAsiento(numeroAsiento);
            guardarEstadoDeSalasEnArchivo();
        } else {
            System.out.println("Sala no encontrada.");
        }
    }
    
    public void editarAsientoSucursal(int idSala, String numeroAsientoAnterior, String numeroAsientoNuevo) {
        Sala sala = findSalaPorId(idSala);
        if (sala != null) {
            sala.liberarAsiento(numeroAsientoAnterior);
            sala.seleccionarAsiento(numeroAsientoNuevo);
            guardarEstadoDeSalasEnArchivo();
        } else {
            System.out.println("Sala no encontrada.");
        }
        
    }
    
    private Sala findSalaPorId(int idSala) {
        for (Sala sala : salas) {
            if (sala.getIdSala() == idSala) {
                return sala;  
            }
        }
        return null;
    }
    
    public void guardarEstadoDeSalasEnArchivo() {
        String nombreArchivoPorSala;
        for (Sala sala : salas) {
             nombreArchivoPorSala = "registroSucursales/" + nombreSucursal + "/-info-sala"+ sala.getIdSala();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivoPorSala))) {        

                    writer.write(this.nombreSucursal + "," + sala.getIdSala() + "," + sala.getFilas() 
                            + "," + sala.getAsientosPorFila() + "," + sala.getCapacidad());
                    writer.newLine();

                    for (int fila = 0; fila < sala.getFilas(); fila++) {
                        StringBuilder filaEstado = new StringBuilder();

                        for (int columna = 0; columna < sala.getAsientosPorFila(); columna++) {
                            int index = fila * sala.getAsientosPorFila() + columna;
                            Asiento asiento = sala.getAsientos().get(index);
                            String estado = asiento.isOcupado() ? "-" : "+";
                            filaEstado.append(asiento.getNumeroAsiento()).append("(").append(estado).append(")");

                            if (columna < sala.getAsientosPorFila() - 1) {
                                filaEstado.append(",");
                            }
                        }

                        writer.write(filaEstado.toString());
                        writer.newLine();
                    }
            } catch (IOException e) {
                System.out.println("Error al guardar la información: " + e.getMessage());
            }
        }
    }
    
    public void cargarEstadoDeSalasDesdeArchivo() {
        File directorioSucursal = new File("registroSucursales/" + nombreSucursal);
        
        if (!directorioSucursal.exists()) {
            System.out.println("El directorio de la sucursal no existe.");
            return;
        }

        File[] archivosSala = directorioSucursal.listFiles((dir, name) -> name.matches(".*-info-sala\\d+$"));
        if (archivosSala == null || archivosSala.length == 0) {
            System.out.println("No se encontraron archivos de salas.");
            return;
        }

        for (File archivoSala : archivosSala) {
            if (archivoSala.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(archivoSala))) {
                    String line;
                    line = reader.readLine();
                    if (line != null) {
                        String[] salaInfo = line.split(",");
                        String nombreSucursal = salaInfo[0];
                        int idSala = Integer.parseInt(salaInfo[1]);
                        int filas = Integer.parseInt(salaInfo[2]);
                        int asientosPorFila = Integer.parseInt(salaInfo[3]);
                        int capacidad = Integer.parseInt(salaInfo[4]);

                        Sala sala = new Sala(idSala, filas, asientosPorFila);

                        for (int i = 0; i < filas; i++) {
                            line = reader.readLine();
                            if (line != null) {
                                String[] asientos = line.split(",");
                                for (int j = 0; j < asientos.length; j++) {
                                    String asientoInfo = asientos[j].trim();
                                    String numeroAsiento = asientoInfo.split("[()]")[0];
                                    String estado = asientoInfo.split("[()]")[1];

                                    Asiento asiento = new Asiento(numeroAsiento);
                                    if ("-".equals(estado)) {
                                        asiento.setOcupado();
                                    } else {
                                        asiento.setDesocupado();
                                    }
                                    sala.addAsiento(asiento);
                                }
                            }
                        }
                        salas.add(sala);
                    }
                } catch (IOException e) {
                    System.out.println("Error al cargar la información de la sala: " + e.getMessage());
                }
            }
        }

        if (salas.isEmpty()) {
            System.out.println("No se cargaron salas.");
        }
    }
}

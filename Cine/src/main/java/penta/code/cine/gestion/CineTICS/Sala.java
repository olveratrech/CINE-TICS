package penta.code.cine.gestion.CineTICS;
import penta.code.cine.gestion.Funciones.*;
import org.fusesource.jansi.AnsiConsole;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author olveratrech
 */
public class Sala {
    protected int idSala;
    protected int filas;
    protected int asientosPorFila;
    protected int capacidad;
    protected List<Asiento> asientos;
    protected List<Funcion> funciones;

    public Sala(int idSala, int filas, int asientosPorFila) {
        this.idSala = idSala;
        this.filas = filas;
        this.asientosPorFila = asientosPorFila;
        this.capacidad = filas * asientosPorFila;
        this.asientos = new ArrayList<>();
        this.funciones = new ArrayList<>();
        initAsientos(filas, asientosPorFila);
    }

    public int getIdSala() {
        return this.idSala;
    }
    
    public int getFilas() {
        return this.filas;
    }
    
    public int getAsientosPorFila() {
        return this.asientosPorFila;
    }

    public int getCapacidad() {
        return this.capacidad;
    }
    
    public List<Asiento> getAsientos() {
        return this.asientos;
    }
    
    public List<Funcion> getFunciones() {
        return this.funciones;
    }
    
    
    // Métodos
    
    
    private void initAsientos(int filas, int asientosPorFila) {
        char letraInicial = 'A';
        for (int fila = 0; fila < filas; fila++) {
            char letra = (char) (letraInicial + fila);
            for (int numero = 1; numero <= asientosPorFila; numero++) {
                String nombreAsiento = letra + String.valueOf(numero);
                asientos.add(new Asiento(nombreAsiento));      
            }
        }
    }
    
    public void showAsientos() {
        System.out.println("Sala -" + idSala);
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < asientosPorFila; columna++) {
                int index = fila * asientosPorFila + columna;
                Asiento asiento = asientos.get(index);
                String estado = asiento.isOcupado() ? "-" : "+";
                //System.out.print("\t");
                System.out.print(asiento.getNumeroAsiento() + "(" + estado + ") ");
            }
            System.out.println();
        }
    }
    
    public void seleccionarAsiento(String numeroAsiento) {                                             
        for (Asiento asiento : asientos) {
            if (asiento.getNumeroAsiento().equals(numeroAsiento)) {
                if (!asiento.isOcupado()) {
                    asiento.setOcupado();
                    System.out.println("El asiento " + numeroAsiento + " ha sido reservado.");
                    return;
                } else {
                    System.out.println("El asiento " + numeroAsiento + " ya se encuentra reservado.");
                    return;
                }
            }
        }
        System.out.println("El asiento " + numeroAsiento + " no  fué encontrado.");
    }

    public void liberarAsiento(String numeroAsiento) {
        for (Asiento asiento : asientos) {
            if (asiento.getNumeroAsiento().equals(numeroAsiento)) {
                if (asiento.isOcupado()) {
                    asiento.setDesocupado();
                    System.out.println("El asiento " + numeroAsiento + " se ha liberado.");
                    return;
                } else {
                    System.out.println("El asiento " + numeroAsiento + " se encuentra libre.");
                    return;
                }
            }
        }
        System.out.println("El asiento " + numeroAsiento + " no fué encontrado.");
    }
    
    public void addAsiento(Asiento asiento) {
        asientos.add(asiento);
    }
    
    public void addFuncion(Funcion funcion) {
        this.funciones.add(funcion);
       
    }
    
    public void showFunciones() {
        System.out.println("Funciones para la sala " + idSala + ": ");
        for (Funcion funcion : funciones) {
            System.out.println(funcion); 
            System.out.println("Costo por boleto: Adulto $80   | Niño: $50");
        }
    }
}
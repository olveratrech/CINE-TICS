package penta.code.cine.gestion.CineTICS;
import penta.code.cine.gestion.Funciones.Funcion;
import java.util.UUID;

public class Boleto {
    private String id;
    private Funcion funcion;
    private Asiento asiento;
    private double precio;

    
    public Boleto(Funcion funcion, Asiento asiento, double precio) {
        this.id = UUID.randomUUID().toString().substring(0, 8); 
        this.funcion = funcion;
        this.asiento = asiento;
        this.precio = precio;
    }

    // Getters
    public String getId() {
        return id;
    }

    public Funcion getFuncion() {
        return funcion;
    }

    public Asiento getAsiento() {
        return asiento;
    }

    public double getPrecio() {
        return precio;
    }

    // Setters
    public void setPrecio(double precio) {
        this.precio = precio;
    }

    @Override
    public String toString() {
        return id + "," + funcion.getId() + "," + funcion.getPelicula().getTitulo() + "," + funcion.getHoraInicio() + "," + asiento.getNumeroAsiento() + "," + precio;          
    }
}
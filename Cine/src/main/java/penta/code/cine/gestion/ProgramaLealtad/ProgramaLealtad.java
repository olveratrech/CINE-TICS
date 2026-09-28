package penta.code.cine.gestion.ProgramaLealtad;

public class ProgramaLealtad {
    private int nivel;
    private double puntos;
    private static final double VALOR_PUNTO = 0.0027;

    public ProgramaLealtad() {
        this.nivel = 0;
        this.puntos = 0;
    }
    
    public ProgramaLealtad(int nivel, double puntos) {
        this.nivel = nivel;
        this.puntos = puntos;
    }

    public int getNivel() {
        return nivel;
    }

    public double getPuntos() {
        return puntos;
    }
    
    public void setNivel(int nivel) {
        this.nivel = nivel;
    }
    
    public void setPuntos(double puntos) { 
        this.puntos = puntos;
    }

    public double calcularDineroDePuntos() {
        return puntos * VALOR_PUNTO;
    }

    public void agregarCompra(double montoCompra) {
        double puntosGanados = 0;

        if (nivel < 5) {
            puntosGanados = montoCompra * 0.03;
        } else if (nivel < 10) {
            puntosGanados = montoCompra * 0.075;
        } else {
            puntosGanados = montoCompra * 0.15;
        }

        puntos += puntosGanados;

        actualizarNivel(montoCompra);
    }

    private void actualizarNivel(double montoCompra) {
        if (nivel < 5 && montoCompra >= 1000) {
            nivel = 5;
        } else if (nivel < 10 && montoCompra >= 1200) {
            nivel = 10;
        } else if (nivel < 15 && montoCompra >= 1350) {
            nivel = 15;
        }
    }
    
    
}


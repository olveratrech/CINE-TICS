package penta.code.cine.gestion.CineTICS;

/**
 *
 * @author olveratrech
 */
public class Asiento {
    protected String numeroAsiento;
    protected boolean estadoAsiento;
    
    public Asiento(String numeroAsiento) {
        this.numeroAsiento = numeroAsiento;
        this.estadoAsiento = Boolean.FALSE;
    }
    
    public String getNumeroAsiento() {
        return this.numeroAsiento;
    }
    
    public boolean isOcupado() {
        return this.estadoAsiento;
    }
    
    public void setOcupado() {
        this.estadoAsiento = Boolean.TRUE;
    }
    
    public void setDesocupado() {
        this.estadoAsiento = Boolean.FALSE;
    }
    
    @Override
    public String toString() {
        return estadoAsiento ? "-" : "+";
    }
}



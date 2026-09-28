package penta.code.cine.gestion.Personal;
import penta.code.cine.gestion.Persona.Persona;

/**
 *
 * @author PentaCode
 * @version 1.1.01
 */
public class Empleado extends Persona{
    // Atributos
    protected String idEmpleado;
    protected String puesto;
    protected String sucursal;
    protected double salario;
    protected String fechaContratacion;
    protected boolean estadoEmpleado;
    protected String idNominaEmpleado;

    // Constructor vacío
    public Empleado() {
        
    }
    
    // Constructor Empleado
    
    public Empleado(String idEmpleado, String puesto, String sucursal, double salario, 
                    String fechaContratacion, boolean estadoEmpleado) {
        this.idEmpleado = idEmpleado;
        this.puesto = puesto;
        this.sucursal = sucursal;
        this.salario = salario;
        this.fechaContratacion = fechaContratacion;
        this.estadoEmpleado = estadoEmpleado;
    }

    // Métodos getter
    public String getIdEmpleado() {
        return idEmpleado;
    }

    public String getPuesto() {
        return puesto;
    }

    public String getSucursal() {
        return sucursal;
    }

    public double getSalario() {
        return salario;
    }

    public String getFechaDeContratacion() {
        return fechaContratacion;
    }

    public boolean getEstadoEmpleado() {
        return estadoEmpleado;
    }
    
    public String getIdNominaEmpleado() {
        return this.idNominaEmpleado;
    }

    // Métodos setter
    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public void setFechaDeContratacion(int dia, int mes, int anio) {
        this.fechaContratacion = dia + "/" + mes + "/" + anio;
    }

    public void setEstadoEmpleado(boolean estadoEmpleado) {
        this.estadoEmpleado = estadoEmpleado;
    }
    
    public void setIdNominaEmpleado(String idNominaEmpleado) {
        this.idNominaEmpleado = idNominaEmpleado;
    }
    
    public String EstadoEmpleadoString() {
        if (this.estadoEmpleado == true) {
            return "Empleado activo";
        } else {
            return "Empleado inactivo";
        }
    }
}

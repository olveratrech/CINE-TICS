package penta.code.cine.gestion.Personal;

/**
 *
 * @author PentaCode
 * @version 1.1.01
 */
public class Cajero extends Empleado{
    // Atributos
    protected int numCaja;
    protected String areaCajero;
    protected double comisionCajero;
    
    
    // Constructor vacío
    public Cajero() {
        
    }
    
    // Constructor Cajero
    public Cajero(String idEmpleado, String contrasena, String puesto, int numCaja, String areaCajero,
                           String sucursal, String nombre, String apPaterno,
                           String apMaterno, String correo, String numTelefono, 
                           String direccion, String rfc, String fechaNacimiento,
                           int edad, String genero, double salario, double comisionCajero,
                           String fechaContratacion, String idNominaEmpleado, 
                           boolean estadoEmpleado) {
        this.idEmpleado = idEmpleado;
        this.contrasena = contrasena;
        this.puesto = puesto;
        this.numCaja = numCaja;
        this.areaCajero = areaCajero;
        this.sucursal = sucursal;
        this.nombre = nombre;
        this.apPaterno = apPaterno;
        this.apMaterno = apMaterno;
        this.correo = correo;
        this.numTelefono = numTelefono;
        this.direccion = direccion;
        this.rfc = rfc;
        this.fechaNacimiento = fechaNacimiento;
        this.edad = edad;
        this.genero = genero;
        this.salario = salario;
        this.fechaContratacion = fechaContratacion;
        this.idNominaEmpleado = idNominaEmpleado;
        this.estadoEmpleado = estadoEmpleado;
    }
    
    public int getNumCaja() {
        return this.numCaja;
    }

    public String getAreaCajero() {
        return this.areaCajero;
    }

    public double getComisionCajero() {
        return this.comisionCajero;
    }

    // Setters
    public void setNumCaja(int numCaja) {
        this.numCaja = numCaja;
    }

    public void setAreaCajero(String areaCajero) {
        this.areaCajero = areaCajero;
    }

    public void setComisionCajero(double comisionCajero) {
        this.comisionCajero = comisionCajero;
    }
    
    @Override
    public String toString() {
        return  "ID Empleado: " + idEmpleado +
                ", Contraseña: " + "********" +
                ", Puesto: " + puesto +
                ", Número de caja: " + numCaja +
                ", Area: " + areaCajero +
                ", Sucursal: " + sucursal +
                ", Nombre: " + nombre + 
                " " + apPaterno + 
                " " + apMaterno +
                ", Correo: " + correo +
                ", Número de Teléfono: " + numTelefono +
                ", RFC: " + rfc +
                ", Fecha de Nacimiento: " + fechaNacimiento +
                ", Edad: " + edad + " años" +
                ", Género: " + genero +
                ", Salario: $" + salario + " MXN" +
                ", % de Comisión: $" + comisionCajero + " MXN" +
                ", Fecha de Contratación: " + fechaContratacion +
                ", ID Nómina: " + idNominaEmpleado +
                ", Estado Cliente: " + EstadoEmpleadoString();
    }
}

package penta.code.cine.gestion.Personal;

/**
 *
 * @author PentaCode
 * @version 1.1.01
 */
public class Gerente extends Empleado{
    // Atributos
    protected double bono;
    
    // Constructor vacío
    public Gerente() {
        
    }

    public Gerente(String idEmpleado, String contrasena, String puesto, String sucursal, 
           String nombre, String apPaterno, String apMaterno, String correo, String numTelefono, 
           String direccion, String rfc, String fechaNacimiento, int edad, String genero, 
           double salario, double bono, String fechaContratacion, String idNominaEmpleado, 
           boolean estadoEmpleado) {
        this.idEmpleado = idEmpleado;
        this.contrasena = contrasena;
        this.puesto = puesto;
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
        this.bono = bono;
        this.fechaContratacion = fechaContratacion;
        this.idNominaEmpleado = idNominaEmpleado;
        this.estadoEmpleado = estadoEmpleado;
    }
    
    public double getBono() {
        return  this.bono;
    }
    
    public void setBono(double bono) {
        this.bono = bono;
    }
    
    @Override
    public String toString() {
        return  "ID Empleado: " + idEmpleado +
                ", Contraseña: " + "********" +
                ", Puesto: " + puesto +
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
                ", Fecha de Contratación: " + fechaContratacion +
                ", ID Nómina: " + idNominaEmpleado +
                ", Estado Cliente: " + EstadoEmpleadoString();
    }
}

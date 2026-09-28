package penta.code.cine.gestion.Personal;

/**
 *
 * @author PentaCode
 * @version 1.1.01
 */
public class AyudanteGeneral extends Empleado{
    // Atributos
    
    protected String areaAyudanteGeneral;
    
    // Constructor vacío
    public AyudanteGeneral() {
        
    }
    
    // Constructor AyudanteGeneral
    public AyudanteGeneral(String idEmpleado, String contrasena, String puesto, String areaAyudante, 
                           String sucursal, String nombre, String apPaterno,
                           String apMaterno, String correo, String numTelefono, 
                           String direccion, String rfc, String fechaNacimiento,
                           int edad, String genero, double salario, String fechaContratacion,
                           String idNominaEmpleado, boolean estadoEmpleado) {
        this.idEmpleado = idEmpleado;
        this.contrasena = contrasena;
        this.puesto = puesto;
        this.areaAyudanteGeneral = areaAyudante;
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

    public String getAreaAyudanteGeneral() {
        return  this.areaAyudanteGeneral;
    }
    
    public void setAreaAyudanteGeneral(String area) {
        this.areaAyudanteGeneral = area;
    }
    
    @Override
    public String toString() {
        return  "ID Empleado: " + idEmpleado +
                ", Contraseña: " + "********" +
                ", Puesto: " + puesto +
                ", Area: " + areaAyudanteGeneral +
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

package penta.code.cine.gestion.Persona;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 *
 * @author PentaCode
 * @version 1.2.01
 */
public class Persona {
    //Atributos
    
    protected String nombre;
    protected String apPaterno;
    protected String apMaterno;
    protected String usuario;
    protected String correo;
    protected String contrasena;
    protected String numTelefono;
    protected String direccion;
    protected String rfc;
    protected String fechaNacimiento;
    protected int edad;
    protected String genero;

    
    /**
     * Constructor vacío.
     */
    public Persona() {
    }
    
    public Persona(String nombre, String apPaterno, String apMaterno, String usuario, String contrasena) {
        this.nombre = nombre;
        this.apPaterno = apPaterno;
        this.apMaterno = apMaterno;
        this.usuario = usuario;
        this.contrasena = contrasena;
    }
    
    public Persona(String usuario, String contrasena) {
        this.usuario = usuario;
        this.contrasena = contrasena;
    }
    
    // Getters
    
    public String getNombre() {
        return this.nombre;
    }

    public String getApellidoPaterno() {
        return this.apPaterno;
    }

    public String getApellidoMaterno() {
        return this.apMaterno;
    }
    
    public String getUsuario() {
        return this.usuario;
    }

    public String getCorreo() {
        return this.correo;
    }

    public String getContrasena() {
        return this.contrasena;
    }

    public String getNumeroDeTelefono() {
        return this.numTelefono;
    }

    public String getDireccion() {
        return this.direccion;
    }

    public String getRFC() {
        return this.rfc;
    }

    public String getFechaDeNacimiento() {
        return this.fechaNacimiento;
    }

    public int getEdad() {
        return this.edad;
    }

    public String getGenero() {
        return this.genero;
    }

    // Setters
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellidoPaterno(String apPaterno) {
        this.apPaterno = apPaterno;
    }

    public void setApellidoMaterno(String apMaterno) {
        this.apMaterno = apMaterno;
    }
    
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public void setNumeroDeTelefono(String numTelefono) {
        this.numTelefono = numTelefono;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setRFC(String rfc) {
        this.rfc = rfc;
    }

    public void setFechaDeNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }
    
    protected static int calcularEdad(String fechaNacimiento) {
        try {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fechaNac = LocalDate.parse(fechaNacimiento, formato);
            LocalDate fechaActual = LocalDate.now();
            
            return Period.between(fechaNac, fechaActual).getYears();
                   
        } catch (DateTimeParseException e) {
            System.out.println("Error: Formato de fecha inválido. Asegúrate de usar DD/MM/AAAA");
            return -1;
        }
    }
}
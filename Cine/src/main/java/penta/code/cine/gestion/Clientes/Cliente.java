package penta.code.cine.gestion.Clientes;
import penta.code.cine.gestion.Financiera.*;
import penta.code.cine.gestion.ProgramaLealtad.ProgramaLealtad;
import penta.code.cine.gestion.Persona.Persona;
import java.util.Scanner;


public class Cliente extends Persona{

    protected String idCliente; 
    protected boolean estadoCliente;
    private CuentaBancaria metodoPago;
    private boolean metodoPagoRegistrado;
    private ProgramaLealtad programaLealtad;
    private boolean inscritoProgramaLealtad;

    public Cliente() {
    }
    
    public Cliente(String idCliente, String usuario, String correo, String contrasena, 
                   String nombre, String apPaterno, String apMaterno, String fechaNacimiento, 
                   int edad, String genero, String direccion, String numTelefono, 
                   String rfc, boolean estadoCliente) {
        this.idCliente = idCliente;
        this.usuario = usuario;
        this.correo = correo;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.apPaterno = apPaterno;
        this.apMaterno = apMaterno;
        this.fechaNacimiento = fechaNacimiento;
        this.edad = edad;
        this.genero = genero;
        this.direccion = direccion;
        this.numTelefono = numTelefono;
        this.rfc = rfc;
        this.estadoCliente = estadoCliente;
        this.inscritoProgramaLealtad = false;
        this.metodoPagoRegistrado = false;
    }
    
    public Cliente(CuentaBancaria metodoPago) {
        this.metodoPago = metodoPago;
        if (metodoPago != null ) {
            metodoPagoRegistrado = true;
        }
    }
    
    public Cliente(String idCliente, String usuario, String correo, String contrasena, 
                   String nombre, String apPaterno, String apMaterno, String fechaNacimiento, 
                   boolean estadoCliente) {
        this.idCliente = idCliente;
        this.usuario = usuario;
        this.correo = correo;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.apPaterno = apPaterno;
        this.apMaterno = apMaterno;
        this.fechaNacimiento = fechaNacimiento;
        this.edad = calcularEdad(fechaNacimiento);
        this.genero = "";
        this.direccion = "";
        this.numTelefono = "";
        this.rfc = "";
        this.estadoCliente = estadoCliente;
        this.programaLealtad = new ProgramaLealtad();
    }
    
    public Cliente(int nivel, double puntos, boolean inscrito) {
        this.programaLealtad.setNivel(nivel);
        this.programaLealtad.setPuntos(puntos);
        this.inscritoProgramaLealtad = inscrito;
    }

    // Getters

    public String getIdCliente() {
        return this.idCliente;
    }

    public boolean getEstadoCliente() {
        return this.estadoCliente;
    }
    
    public String getMetodoPago() {
        return this.metodoPago.getNumeroDeCuenta();
    }
    
    public ProgramaLealtad getProgramaLealtad() {
        if (inscritoProgramaLealtad) {
            return programaLealtad;
        }
        System.out.println("No estás inscrito en el programa de lealtad.");
        return null;
    }
    
    public boolean metodoDePagoRegiastrado() {
        return this.metodoPagoRegistrado;
    }

    // Setters
    
    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }
    
    public void setEstadoCliente(boolean estadoCliente) {
        this.estadoCliente = estadoCliente;
    }
    
    public void setMetodoPago(CuentaBancaria cuentaBancaria) {
        this.metodoPago = cuentaBancaria;
        this.metodoPagoRegistrado = true;
    }

    public void MostrarDetallesDeLaCuenta() {
        if (this.metodoPago != null) {
            System.out.println(this.metodoPago.toString());
        } else {
            System.out.println("No hay método de pago asignado.");
        }
    }
    
    public String EstadoClienString() {
        if (this.estadoCliente == true) {
            return "Cliente activo";
        } else {
            return "Cliente inactivo";
        }
    }
    
    public boolean isInscritoProgramaLealtad() {
        return inscritoProgramaLealtad;
    }

    public void unirseProgramaLealtad() {
        if (!inscritoProgramaLealtad) {
            this.programaLealtad = new ProgramaLealtad();
            this.inscritoProgramaLealtad = true;
            //System.out.println("¡Te has unido al programa de lealtad con éxito!");
        } else {
            System.out.println("Ya estás inscrito en el programa de lealtad.");
        }
    }
    
    public void actualizarDatos() {
        Scanner scanner = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n=== Datos Personales ===");
            System.out.println(this.toString());
            System.out.println("\n¿Qué dato deseas actualizar?");
            System.out.println("1. Nombre");
            System.out.println("2. Apellido Paterno");
            System.out.println("3. Apellido Materno");
            System.out.println("4. Correo");
            System.out.println("5. Contraseña");
            System.out.println("6. Número de Teléfono");
            System.out.println("7. Dirección");
            System.out.println("8. Fecha de Nacimiento");
            System.out.println("9. RFC");
            System.out.println("10. Terminar edición");
            System.out.print("Elige una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {
                    case 1:
                        System.out.print("Nuevo nombre: ");
                        this.setNombre(scanner.nextLine());
                        break;
                    case 2:
                        System.out.print("Nuevo apellido paterno: ");
                        this.setApellidoPaterno(scanner.nextLine());
                        break;
                    case 3:
                        System.out.print("Nuevo apellido materno: ");
                        this.setApellidoMaterno(scanner.nextLine());
                        break;
                    case 4:
                        System.out.print("Nuevo correo: ");
                        this.setCorreo(scanner.nextLine());
                        break;
                    case 5:
                        System.out.print("Nueva contraseña: ");
                        this.setContrasena(scanner.nextLine());
                        break;
                    case 6:
                        System.out.print("Nuevo número de teléfono: ");
                        this.setNumeroDeTelefono(scanner.nextLine());
                        break;
                    case 7:
                        System.out.print("Nueva dirección: ");
                        this.setDireccion(scanner.nextLine());
                        break;
                    case 8:
                        System.out.print("Nueva fecha de nacimiento (DD/MM/AAAA): ");
                        String nuevaFecha = scanner.nextLine();
                        this.setFechaDeNacimiento(nuevaFecha);
                        this.setEdad(calcularEdad(nuevaFecha));
                        break;
                    case 9:
                        System.out.print("Nuevo RFC: ");
                        this.setRFC(scanner.nextLine());
                        break;
                    case 10:
                        System.out.println("Edición de datos finalizada.");
                        
                        return;
                    default:
                        System.out.println("Opción no válida. Inténtalo de nuevo.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, ingresa un número.");
                opcion = -1;
            }
        } while (true);
    }
    
    public void agregarCompra(double montoCompra) {
        programaLealtad.agregarCompra(montoCompra);
    }
    
    @Override
    public String toString() {
        return  "\nID Cliente: " + idCliente +
                "\nUsuario: " + usuario +
                "\nCorreo: " + correo +
                "\nContraseña: " + "********" +
                "\nNombre: " + nombre + 
                " " + apPaterno + 
                " " + apMaterno +
                "\nFecha de Nacimiento: " + fechaNacimiento +
                "\nEdad: " + edad +
                "\nGénero: " + genero +
                "\nDirección: " + direccion +
                "\nNúmero de Teléfono: " + numTelefono +
                "\nRFC: " + rfc +
                "\nEstado Cliente: " + EstadoClienString();
    }
}

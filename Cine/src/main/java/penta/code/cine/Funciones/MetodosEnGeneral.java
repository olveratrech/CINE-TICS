package penta.code.cine.Funciones;
import Administrador.Administrador;
import java.io.*;
import java.sql.SQLOutput;
import java.util.*;
import penta.code.cine.gestion.Funciones.*;
import penta.code.cine.gestion.Datos.*;
import penta.code.cine.gestion.CineTICS.*;
import penta.code.cine.gestion.Dulceria.*;
import penta.code.cine.gestion.Clientes.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import penta.code.cine.gestion.Financiera.CuentaBancaria;
import penta.code.cine.gestion.Financiera.RegistroCuentaBancaria;
import penta.code.cine.gestion.Personal.AutenticacionEmpleado;
import penta.code.cine.gestion.Personal.AyudanteGeneral;
import penta.code.cine.gestion.Personal.Cajero;
import penta.code.cine.gestion.Personal.Empleado;
import penta.code.cine.gestion.ProgramaLealtad.ProgramaLealtad;

/**
 *
 * @author olveratrech
 */
public class MetodosEnGeneral {
    
    public static String generarId(String tipoId) throws IOException {
        String archivoId;

        switch (tipoId.toLowerCase()) {
            case "cl":
                archivoId = "contadores/id_clientes.txt";
                break;
            case "ag":
                archivoId = "contadores/id_ayudantes_generales.txt";
                break;
            case "cj":
                archivoId = "contadores/id_cajeros.txt";
                break;
            case "pe":
                archivoId = "contadores/id_peliculas.txt";
                break;
            case "fn":
                archivoId = "contadores/id_funciones.txt";
                break;
            case "ge":
                archivoId = "contadores/id_gerentes.txt";
                break;
            default:
                throw new IllegalArgumentException("Tipo de ID no válido: " + tipoId);
        }

        File archivo = new File(archivoId);
        int idActual;

        if (!archivo.exists()) {
            idActual = 1;
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
                idActual = Integer.parseInt(reader.readLine().trim());
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            writer.write(String.valueOf(idActual + 1));
        }

        switch (tipoId.toLowerCase()) {
            case "cl":
                return "CL-" + String.format("%03d", idActual);
            case "ag":
                return "AG-" + String.format("%03d", idActual);
            case "cj":
                return "CJ-" + String.format("%03d", idActual);
            case "pe": 
                return "PE-" + String.format("%03d", idActual);
            case "fn": 
                return "FN-" + String.format("%03d", idActual);
            case "ge": 
                return "GE-" + String.format("%03d", idActual);
            default:
                throw new IllegalArgumentException("Tipo de ID no válido: " + tipoId);
        }
    }
    
    public static String generarIdNomina(String puesto) {
        String prefijo;
        switch (puesto.toLowerCase()) {
            case "ag":
                prefijo = "AG";
                break;
            case "cj":
                prefijo = "CJ";
                break;
            case "ge":
                prefijo = "GE";
                break;
            default:
                throw new IllegalArgumentException("Puesto no válido: " + puesto);
        }

        String letras = generarLetras(3);
        String numeros = generarNumeros(3);
        String especiales = generarCaracteresEspeciales(3);

        return prefijo + "-" + letras + numeros + especiales;
    }

    public static String generarLetras(int longitud) {
        String alfabeto = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder letras = new StringBuilder();

        for (int i = 0; i < longitud; i++) {
            int index = random.nextInt(alfabeto.length());
            letras.append(alfabeto.charAt(index));
        }

        return letras.toString();
    }

    public static String generarNumeros(int longitud) {
        String digitos = "0123456789";
        Random random = new Random();
        StringBuilder numeros = new StringBuilder();

        for (int i = 0; i < longitud; i++) {
            int index = random.nextInt(digitos.length());
            numeros.append(digitos.charAt(index));
        }

        return numeros.toString();
    }

    public static String generarCaracteresEspeciales(int longitud) {
        String caracteresEspeciales = "!@#$%^&*";
        Random random = new Random();
        StringBuilder especiales = new StringBuilder();

        for (int i = 0; i < longitud; i++) {
            int index = random.nextInt(caracteresEspeciales.length());
            especiales.append(caracteresEspeciales.charAt(index));
        }

        return especiales.toString();
    }
    
    public static String generarContrasena() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder contrasena = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(caracteres.length());
            contrasena.append(caracteres.charAt(index));
        }

        return contrasena.toString();
    }
    
    public static void menuGeneral() {
        System.out.println("Proyecto Final" +
                           ", Programación Orientada a Objetos" +
                           "\nGrupo: 09" +
                           "\nSemestre 2025-1" +
                           "\nEquipo 01: PentaCode" +
                           "\n\nIntegrantes: " +
                           "\n\tNochebuena Sosa Kevin Israel: " +
                           "\n\tSotomayor Suárez Edgar Antonio: " +
                           "\n\tTrejo Olvera Emmanuel: ");
    }
    
    public static void funcionGeneral() throws InterruptedException, IOException {
        int sucursal;
        Administrador admin = new Administrador("Emmanuel","Trejo","Olvera","admin","admin");
        String reintentar = "n";
        Scanner leeDato = new Scanner(System.in);
        
        do {   
            menuGeneral();
            System.out.println("  1. Cliente");
            System.out.println("  2. Administrador");
            System.out.println("  3. Empleado");
            System.out.println("  4. Salir");
            System.out.print("\n  Iniciar sistema como: ");
            try {
                String iniciaSistema = leeDato.nextLine();
                
                switch (iniciaSistema) {
                    case "1":
                        funcionCliente();
                        break;
                    case "2":
                        funcionAdministrador(admin);
                        break;
                    case "3":
                        funcionEmpleado();
                        break;
                    case "4":
                        System.out.println("Saliendo del sistema. Gracias por usar Cine-TICS! ");
                        for(int i = 0; i < 5; i++)
                            Animacion();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Selecciona un número de sucursal válido.");
                }
            } catch (NumberFormatException e) {
                System.out.print("Entrada no válida. Por favor, ingresa un número.");
            }
        } while (true); 
    }
    
    public static void funcionCliente() throws InterruptedException, IOException {
        int sucursal;
        String reintentar = "n";
        Scanner leeDato = new Scanner(System.in);
        
        Sucursal cineticsCU = new Sucursal("Cine-TICS 'CU'");
        Sucursal cineticsUNIVERSIDAD = new Sucursal("Cine-TICS 'Universidad'");
        Sucursal cineticsDELTA = new Sucursal("Cine-TICS 'Delta'");
        Sucursal cineticsXOCHIMILCO = new Sucursal("Cine-TICS 'Xochimilco'");
        cineticsCU.cargarEstadoDeSalasDesdeArchivo();
        cineticsUNIVERSIDAD.cargarEstadoDeSalasDesdeArchivo();
        cineticsDELTA.cargarEstadoDeSalasDesdeArchivo();
        cineticsXOCHIMILCO.cargarEstadoDeSalasDesdeArchivo();
        
        
        do {   
            System.out.println();
            MetodosEnGeneral.BienvenidoACineTics();
            MetodosEnGeneral.listaSucursales();
            System.out.print("Por favor, elige la sucursal de su preferencia: ");
            try {
                String entrada = leeDato.nextLine();
                sucursal = Integer.parseInt(entrada);
                
                switch (sucursal) {
                    case 1:
                        funcionMenuPrincipalNoAutenticado(cineticsCU);
                        break;
                    case 2:
                        funcionMenuPrincipalNoAutenticado(cineticsUNIVERSIDAD);
                        break;
                    case 3:
                        funcionMenuPrincipalNoAutenticado(cineticsDELTA);
                        break;
                    case 4:
                        funcionMenuPrincipalNoAutenticado(cineticsXOCHIMILCO);
                        break;
                    case 5: 
                        System.out.println("Saliendo del sistema. Gracias por usar Cine-TICS! ");
                        for(int i = 0; i < 5; i++)
                            Animacion();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Selecciona un número de sucursal válido.");
                }
            } catch (NumberFormatException e) {
                System.out.print("Entrada no válida. Por favor, ingresa un número.");
            }
        } while (true);
    }
    
    public static void funcionAdministrador(Administrador administrador) {
        Scanner leeDato = new Scanner(System.in);
        String sucursal;
        
        System.out.println();
        System.out.println("Bienvenido, a continuación, ingresará con alguna de las credenciales de administrador.");
        System.out.print("Ingrese su usuario: ");
        String usuario = leeDato.nextLine();
        System.out.print("Ingrese su contraseña: ");
        String contrasena = leeDato.nextLine();

        try {
            if (administrador.getUsuario().equalsIgnoreCase(usuario) && administrador.getContrasena().equalsIgnoreCase(contrasena)) {
                funcionMenuPrincipalAdministrador(administrador);
            } else {
                System.out.println("Usuario y/o contraseña invalida. Por favor, intente nuevamente o regístrate.");
            }
        } catch (Exception e) {
            System.out.println("Error al procesar la autenticación: " + e.getMessage());
        }
    }
    
    public static void funcionEmpleado() throws IOException {
        Scanner leeDato = new Scanner(System.in);
        String sucursal;
        
        System.out.println();
        MetodosEnGeneral.BienvenidoACineTics();
        System.out.print("Ingrese su usuario: ");
        String usuario = leeDato.nextLine();
        System.out.print("Ingrese su contraseña: ");
        String contrasena = leeDato.nextLine();

        try {
            Empleado empleadoAutenticado = AutenticacionEmpleado.autenticar(usuario, contrasena);
            if (empleadoAutenticado != null) {
                sucursal = "Cine-TICS '" + empleadoAutenticado.getSucursal() + "'";
                Sucursal sucursalActual = obtenerSucursalPorNombre(sucursal);
                sucursalActual.cargarEstadoDeSalasDesdeArchivo();
                funcionMenuPrincipalEmpleado(empleadoAutenticado, sucursalActual);
            } else {
                System.out.println("Usuario y/o contraseña invalida. Por favor, intente nuevamente o regístrate.");
            }
        } catch (IOException e) {
            System.out.println("Error al procesar la autenticación: " + e.getMessage());
        }
    }
    
    public static void funcionMenuPrincipalAdministrador(Administrador administrador) {
        int opcion = 0;
        Scanner scanner = new Scanner(System.in);
        
        while (opcion != 9) {
            menuAdministrador();
            System.out.print("Seleccione una opción: ");
            String input = scanner.nextLine();

            try {
                opcion = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Opción inválida. Por favor, ingrese un número del 1 al 9.");
                continue;
            }

            switch (opcion) {
                case 1:
                    administrador.ActualizarCartelera();
                    break;
                case 2:
                    administrador.VerProductos();
                    break;
                case 3:
                    administrador.ActualizarStockProducto();
                    break;
                case 4:
                    administrador.AgregarProducto();
                    break;
                case 5:
                    administrador.VerEmpleados();
                    break;
                case 6:
                    //agregarEmpleado();
                    break;
                case 7:
                    //verEstadisticasVentas();
                    break;
                case 8:
                    //generarReporteVentas();
                    break;
                case 9:
                    System.out.println("¡Hasta luego!");
                    break;
                default:
                    System.out.println("Opción no válida. Por favor, seleccione una opción del 1 al 9.");
            }
            System.out.println(); // Línea en blanco para separar iteraciones
        }
    }
    
    public static void funcionMenuPrincipalEmpleado(Empleado empleado, Sucursal sucursal) {
            System.out.println("bienvenido" + empleado.getNombre() + ", perteneces a " + sucursal.getNombreSucursal());
    }
    
    public static void menuAdministrador() {
        System.out.println("===== Menú de Administrador =====");
        System.out.println("1. Actualizar cartelera");
        System.out.println("2. Ver productos");
        System.out.println("3. Actualizar Stock de productos");
        System.out.println("4. Agregar Producto");
        System.out.println("5. Ver Empleados");
        System.out.println("6. Agregar Empleado");
        System.out.println("7. Ver estadísticas de ventas");
        System.out.println("8. Generar Reporte de Ventas");
        System.out.println("9. Salir");
        System.out.println("=================================");

    }
    
    public static void menuClienteNoAutenticado() {
        System.out.println("  1. Ver Cartelera");
        System.out.println("  2. Buscar película");
        System.out.println("  3. Buscar producto");
        System.out.println("  4. Cambiar Sucursal");
        System.out.println("  5. Iniciar Sesión");
        System.out.println("  6. ¿No tienes una cuenta? Regístrate");
        System.out.println("  7. Salir");
    }
    
    public static void menuClienteAutenticado() {
        System.out.println("  1. Ver Cartelera");
        System.out.println("  2. Buscar película");
        System.out.println("  3. Buscar producto");
        System.out.println("  4. Cambiar Sucursal");
        System.out.println("  5. Ver Carrito");
        System.out.println("  6. Actualizar datos personales");
        System.out.println("  7. Ver Mis compras");
        System.out.println("  8. Ver Mis puntos");
        System.out.println("  9. Salir");
    }
    
    public static void BienvenidoACineTics() {
        System.out.println("░█████╗░██╗███╗░░██╗███████╗░░░░░░████████╗██╗░█████╗░░██████╗");
        System.out.println("██╔══██╗██║████╗░██║██╔════╝░░░░░░╚══██╔══╝██║██╔══██╗██╔════╝");
        System.out.println("██║░░╚═╝██║██╔██╗██║█████╗░░█████╗░░░██║░░░██║██║░░╚═╝╚█████╗░");
        System.out.println("██║░░██╗██║██║╚████║██╔══╝░░╚════╝░░░██║░░░██║██║░░██╗░╚═══██╗");
        System.out.println("╚█████╔╝██║██║░╚███║███████╗░░░░░░░░░██║░░░██║╚█████╔╝██████╔╝");
        System.out.println("░╚════╝░╚═╝╚═╝░░╚══╝╚══════╝░░░░░░░░░╚═╝░░░╚═╝░╚════╝░╚═════╝░\n");
        System.out.println(" ▒█▀▀█ ▀█▀ ▒█▀▀▀ ▒█▄░▒█ ▒█░░▒█ ▒█▀▀▀ ▒█▄░▒█ ▀█▀ ▒█▀▀▄ ▒█▀▀▀█");
        System.out.println(" ▒█▀▀▄ ▒█░ ▒█▀▀▀ ▒█▒█▒█ ░▒█▒█░ ▒█▀▀▀ ▒█▒█▒█ ▒█░ ▒█░▒█ ▒█░░▒█");
        System.out.println(" ▒█▄▄█ ▄█▄ ▒█▄▄▄ ▒█░░▀█ ░░▀▄▀░ ▒█▄▄▄ ▒█░░▀█ ▄█▄ ▒█▄▄▀ ▒█▄▄▄█\n"); 
    }
    
    public static void listaSucursales() {
        System.out.println("1. Cine-TICS CU");
        System.out.println("2. Cine-TICS Universidad");
        System.out.println("3. Cine-TICS Delta");
        System.out.println("4. Cine-TICS Xochimilco");
        System.out.println("5. Salir");
    }
    
    public static void funcionMenuPrincipalNoAutenticado(Sucursal sucursal) throws InterruptedException, IOException {
        Scanner leeDato = new Scanner(System.in);
        int opcionMenu = -1;
        
        do {      
            try {
                System.out.println("\n======================================================");
                System.out.printf("  HOLA, te damos la bienvenida a %s\n", sucursal.getNombreSucursal());
                System.out.println("======================================================");
                System.out.printf("  No olvides que puedes iniciar sesión o registrarte.\n  Es totalmente gratis! :v)\n");
                System.out.printf("  %s\n", fechaHoraFormateada());
                System.out.println("======================================================");
                menuClienteNoAutenticado();
                System.out.print("Elige una opción: ");
                String entrada = leeDato.nextLine();
                opcionMenu = Integer.parseInt(entrada);
                
                switch (opcionMenu) {
                    case 1:
                        VerCartelera.mostrarCartelera(sucursal);
                        break;
                    case 2:
                        BuscarPelicula.buscarPelicula();
                        break;
                    case 3:
                        String idProducto;
                        Dulceria dulceriaActual = new Dulceria(sucursal.getNombreSucursal());
                        dulceriaActual.mostrarProductos();
                        System.out.print("\nPor favor, teclea el numero de ID del producto a buscar en la sucursal: ");
                        idProducto = leeDato.nextLine();
                        System.out.println("\n" + BuscarProducto.buscarProductoPorID(idProducto, sucursal.getNombreSucursal()) + "\n");
                        break;
                    case 4:
                        System.out.print("Regresando al menú principal ");
                        for(int i = 0; i < 5; i++)
                            Animacion();
                        return;
                    case 5:
                        String archivoClientes = "registroClientes/datos_Clientes.txt";
                        System.out.print("Ingrese su usuario: ");
                        String usuario = leeDato.nextLine();
                        System.out.print("Ingrese su contraseña: ");
                        String contrasena = leeDato.nextLine();
                        
                        Cliente clienteAutenticado = AutenticacionCliente.autenticarCliente(usuario, contrasena, archivoClientes);
                        if (clienteAutenticado != null) {
                            funcionMenuPrincipalAutenticado(clienteAutenticado, sucursal);
                        } else {
                            System.out.println("Usuario y/o contraseña invalida. Por favor, intente nuevamente o regístrate.");
                        }
                        break;
                    case 6:
                        if (registrarClienteNuevo()) {
                            System.out.println("Has sido registrado con éxito!, que comience la aventura :)");
                            System.out.println("Por favor, inicia sesión.");
                        } else {
                            System.out.println("Ha ocurrido un error :O... Intentalo nuevamente.");
                        }
                        break;
                    case 7:
                        System.out.print("Saliendo del sistema ");
                        for(int i = 0; i < 5; i++)
                            Animacion();
                        System.exit(0);
                        break;
                    default:
                    System.out.print("Selecciona un número de sucursal válido.");
                } 
            } catch (NumberFormatException e) {
                System.out.print("Entrada no válida. Por favor, ingresa un número.");
            }
        } while (true);  
    }
    
    private static boolean registrarClienteNuevo() throws IOException {
        Scanner leeDato = new Scanner(System.in);
        
        System.out.print("Nombre: ");
        String nombre = leeDato.nextLine();
        System.out.print("Apellido paterno: ");
        String apPaterno = leeDato.nextLine();
        System.out.print("Apellido materno: ");
        String apMaterno = leeDato.nextLine();
        System.out.print("Nombre de usuario: ");
        String usuario = leeDato.nextLine();
        System.out.print("Correo: ");
        String correo = leeDato.nextLine();
        System.out.print("Contraseña: ");
        String contrasena = leeDato.nextLine();
        System.out.print("Fecha de nacimiento: ");
        String fechaNacimiento = leeDato.nextLine();
        
        Cliente clienteNuevo = new Cliente(generarId("cl"), usuario, correo, contrasena, nombre, apPaterno, apMaterno, fechaNacimiento, true);
       
        boolean saved = RegistroClientes.registrarCliente(clienteNuevo);
        
        return saved;
    }
    
    private static void funcionMenuPrincipalAutenticado(Cliente cliente, Sucursal sucursal) throws InterruptedException, IOException {
        Scanner leeDato = new Scanner(System.in);
        Dulceria dulceriaActual = new Dulceria(sucursal.getNombreSucursal());
        CarritoBoletos carritoBoletosActual = new CarritoBoletos();
        RegistroClientes.cargarProgramaLealtadCliente(cliente);
        Carrito carritoActual = new Carrito();
        String idProducto;
        String respuestaBoleto;
        int opcionMenu = -1;
        
        do {      
            try {
                System.out.println("\n======================================================");
                System.out.printf("  HOLA, %s %s %s\n", cliente.getNombre(), cliente.getApellidoPaterno(), cliente.getApellidoMaterno());
                System.out.println("======================================================");
                System.out.printf("  %-5s %-20s %-5s %s\n", "Usuario", cliente.getUsuario(), "Sucursal", sucursal.getNombreSucursal());
                System.out.printf("  %s\n", fechaHoraFormateada());
                System.out.println("======================================================");
                menuClienteAutenticado();
                System.out.print("Elige una opción: ");
                String entrada = leeDato.nextLine();
                opcionMenu = Integer.parseInt(entrada);
                
                switch (opcionMenu) {
                    case 1:
                        VerCartelera.mostrarCartelera(sucursal);
                        break;
                    case 2:
                        BuscarPelicula.buscarPelicula();
                        break;
                    case 3:
                        dulceriaActual.mostrarProductos();
                        System.out.print("\nPor favor, teclea el numero de ID del producto a buscar en la sucursal: ");
                        idProducto = leeDato.nextLine();
                        System.out.println("\n" + BuscarProducto.buscarProductoPorID(idProducto, sucursal.getNombreSucursal()) + "\n");
                        break;
                    case 4:
                        MetodosEnGeneral.BienvenidoACineTics();
                        MetodosEnGeneral.listaSucursales();
                        System.out.print("Por favor, elige la sucursal de su preferencia: ");
                        int nuevaSucursal = Integer.parseInt(leeDato.nextLine());
                        
                        Sucursal sucursalSeleccionada = obtenerSucursalPorSeleccion(nuevaSucursal);
                        if (sucursalSeleccionada != null) {
                            sucursal = sucursalSeleccionada;
                            dulceriaActual = new Dulceria(sucursal.getNombreSucursal());
                            carritoActual = new Carrito();
                            System.out.println("Has cambiado a la sucursal: " + sucursal.getNombreSucursal() + ". Bienvenido.");
                        } else {
                            System.out.println("La sucursal seleccionada no es válida. Por favor, intenta nuevamente.");
                        }
                        break;
                    case 5:
                        verCarrito(carritoActual, carritoBoletosActual, dulceriaActual, sucursal, cliente);
                        break;
                    case 6:
                        cliente.actualizarDatos();
                        RegistroClientes.actualizarCliente(cliente);
                        break;
                    case 7:
                        Venta.verMisCompras(cliente.getIdCliente(), sucursal.getNombreSucursal());
                        VentaBoletos.verMisCompras(cliente.getIdCliente(), sucursal.getNombreSucursal());
                        break;
                    case 8:
                        if (!cliente.isInscritoProgramaLealtad()) {
                            System.out.print("¿Deseas unirte al programa de lealtad? (S/N): ");
                            String respuesta = leeDato.nextLine();
                            if (respuesta.equalsIgnoreCase("s") || respuesta.equalsIgnoreCase("S")) {
                                cliente.unirseProgramaLealtad();
                                RegistroClientes.actualizarCliente(cliente);
                            } else {
                                System.out.println("Has decidido no unirte al programa de lealtad.");
                                System.out.println("No te preocupes, puedes hacerlo después :).");
                            }
                        } else {
                            System.out.println("======================================================");
                            System.out.println("  Tu nivel: " + cliente.getProgramaLealtad().getNivel());
                            System.out.println("  Tus puntos: " + cliente.getProgramaLealtad().getPuntos());
                            System.out.println("  Tu dinero en puntos: " + cliente.getProgramaLealtad().calcularDineroDePuntos());
                            System.out.println("======================================================");
                        }
                        
                        break;
                    case 9: 
                        System.out.print("Saliendo del sistema ");
                        for(int i = 0; i < 5; i++)
                            Animacion();
                        System.exit(0);
                        break;
                    default:
                    System.out.print("Selecciona un número de sucursal válido.");
                } 
            } catch (NumberFormatException e) {
                System.out.print("Entrada no válida. Por favor, ingresa un número.");
            }
        } while (true);  
    }
    
    public static Sucursal obtenerSucursalPorSeleccion(int seleccion) {
        List<Sucursal> sucursales = Arrays.asList(
            new Sucursal("Cine-TICS 'CU'"),
            new Sucursal("Cine-TICS 'Universidad'"),
            new Sucursal("Cine-TICS 'Delta'"),
            new Sucursal("Cine-TICS 'Xochimilco'")
        );

            if (seleccion == 1) {
                return sucursales.get(0);
            } else if (seleccion == 2) {
                return sucursales.get(1);
            } else if (seleccion == 3) {
                return sucursales.get(2);
            } else if (seleccion == 4) {
                return sucursales.get(3);
            }
        return null;
    }
    
    public static Sucursal obtenerSucursalPorNombre(String nombreSucursal) {
        List<Sucursal> sucursales = Arrays.asList(
            new Sucursal("Cine-TICS 'CU'"),
            new Sucursal("Cine-TICS 'Universidad'"),
            new Sucursal("Cine-TICS 'Delta'"),
            new Sucursal("Cine-TICS 'Xochimilco'")
        );

            if (nombreSucursal.equalsIgnoreCase("Cine-TICS 'CU'")) {
                return sucursales.get(0);
            } else if (nombreSucursal.equalsIgnoreCase("Cine-TICS 'Universidad'")) {
                return sucursales.get(1);
            } else if (nombreSucursal.equalsIgnoreCase("Cine-TICS 'Delta'")) {
                return sucursales.get(2);
            } else if (nombreSucursal.equalsIgnoreCase("Cine-TICS 'Xochimilco'")) {
                return sucursales.get(3);
            }
        return null;
    }
   
    public static void verCarrito(Carrito carritoActual, CarritoBoletos carritoBoletosActual, Dulceria dulceriaActual, Sucursal sucursal, Cliente cliente) throws InterruptedException, IOException {
        int opcionCarrito = 0;
        Scanner scanner = new Scanner(System.in);
        while (opcionCarrito != 1 && opcionCarrito != 2) {
            System.out.print("\n  1. Productos\n  2. Boletos\n  Que carrito quieres ver: ");            
            try {
                opcionCarrito = Integer.parseInt(scanner.nextLine());
                if (opcionCarrito == 1) {
                    carritoActual.mostrarCarrito();
                    comprarProductos(carritoActual, dulceriaActual, sucursal, cliente);
                } 
                
                else if (opcionCarrito == 2) {
                    carritoBoletosActual.mostrarCarrito();
                    comprarBoletos(sucursal, carritoBoletosActual, cliente);
                } 
                
                else {
                    System.out.println("Opción no válida. Ingresa 1 para ver el carrito de productos o 2 para ver el carrito de boletos.");
                }
            } catch (NumberFormatException e) {
                System.out.println("  Entrada inválida. Debes ingresar un número.");
            }
        }
    }

    public static void Animacion() throws InterruptedException {
        System.out.print(".");
        Thread.sleep(500);
    } 
    
    public static String fechaHoraFormateada() {
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM hh:mm a", new Locale("es", "ES"));
        String fechaHoraFormateada = fechaHoraActual.format(formato);
        return fechaHoraFormateada;
    }
    
    public static void comprarProductos(Carrito carritoActual, Dulceria dulceriaActual, Sucursal sucursal, Cliente cliente) throws IOException, InterruptedException {
        Scanner leeDato = new Scanner(System.in);
        String agregarProductos, idProducto;
        int cantidad;

        do {
            System.out.print("\n¿Deseas agregar productos al carrito? (S/N): ");
            agregarProductos = leeDato.nextLine();
            if (agregarProductos.equalsIgnoreCase("s") || agregarProductos.equalsIgnoreCase("S")) {
                dulceriaActual.mostrarProductos();
                System.out.print("\nPor favor, teclea el numero de ID del producto que deseas agregar: ");
                idProducto = leeDato.nextLine();

                Producto productoEncontrado = dulceriaActual.buscarProducto(idProducto);
                if (productoEncontrado == null) {
                    System.out.println("Producto no encontrado. Por favor, verifica el ID.");
                    continue;
                }
                System.out.print("Cantidad: ");
                try {
                   cantidad = Integer.parseInt(leeDato.nextLine());
                    if (cantidad <= 0) {
                        System.out.println("La cantidad debe ser mayor a 0.");
                        continue;
                    }
                    carritoActual.agregarProducto(productoEncontrado, cantidad);
                } catch (NumberFormatException e) {
                    System.out.println("Cantidad inválida. Por favor, ingresa un número entero.");
                }  
            } else if (!agregarProductos.equalsIgnoreCase("n") && agregarProductos.equalsIgnoreCase("N")) {
                System.out.println("Opción invalida. Por favor, elige S o N.");
            }
        } while (!agregarProductos.equalsIgnoreCase("n"));

        String accionCarrito;
        do {
            System.out.println("\n¿Deseas comprar tu carrito?");
            System.out.println("1. SÍ, comprar carrito");
            System.out.println("2. No, dejar para después");
            System.out.print("Elige una opción: ");
            accionCarrito = leeDato.nextLine();

            switch (accionCarrito) {
                case "1":
                    ProcesoCompra.realizarCompraProductos(cliente, carritoActual, sucursal);
                    break;

                case "2":
                    carritoActual.guardarCarritoEnArchivo(cliente.getIdCliente(), sucursal.getNombreSucursal());
                    break;

                default:
                    System.out.println("Opción inválida. Por favor, elige 1 o 2.");
            }
        } while (!accionCarrito.equals("1") && !accionCarrito.equals("2"));
    }
    
    public static void comprarBoletos(Sucursal sucursal, CarritoBoletos carritoBoletos, Cliente cliente) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        String agregarBoletos, cantidad;
        
        do {
            System.out.print("\n¿Deseas agregar boletos al carrito? (S/N): ");
            agregarBoletos = scanner.nextLine();
            if (agregarBoletos.equalsIgnoreCase("s") || agregarBoletos.equalsIgnoreCase("S")) {
                System.out.println("\n  Salas disponibles en la sucursal: " + sucursal.getNombreSucursal());
                sucursal.showSalas();

                System.out.print("  Elige el ID de la sala: ");
                int idSala;
                try {
                    idSala = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("  Entrada inválida. Debes ingresar un número.");
                    return;
                }

                Sala sala = sucursal.getSalas().get(idSala-1);

                if (sala == null) {
                    System.out.println("  Sala no encontrada.");
                    return;
                }

                System.out.println("\n  Funciones disponibles:");
                VerCartelera.mostrarCarteleraPorSala(sucursal, idSala);

                System.out.print("  Elige el ID de la función: ");
                String idFuncion = scanner.nextLine();

                Funcion funcion = Funcion.buscarFuncionPorId(idFuncion, sucursal.getNombreSucursal(), idSala);

                if (funcion == null) {
                    System.out.println("  Función no encontrada.");
                    return;
                }
                
                System.out.print("Cargando asientos disponibles ");
                for(int i = 0; i < 5; i++)
                    Animacion();
                
                System.out.println("\n  Asientos disponibles:");
                sala.showAsientos();
                 
                cantidad = "0";
                System.out.print("¿Cuantos asientos deseas reservar?: ");
                while (Integer.parseInt(cantidad) < 1) {            
                    try {
                        cantidad = scanner.nextLine();
                        if (Integer.parseInt(cantidad) < 1) {
                            System.out.println("Debes ingresar un número positivo.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("  Entrada inválida. Debes ingresar un número positivo.");
                    }
                }
                try {
                    for (int i = 0; i < Integer.parseInt(cantidad); i++) {
                        System.out.print("  Elige un asiento: ");
                        String numeroAsiento = scanner.nextLine();
                        
                        Asiento asiento = sala.getAsientos().stream()
                                .filter(a -> a.getNumeroAsiento().equalsIgnoreCase(numeroAsiento) && !a.isOcupado())
                                .findFirst()
                                .orElse(null);
                        if (asiento == null) {
                            System.out.println("  El asiento no está disponible o no existe. Elige otro asiento.");
                            return;
                        }
                        sucursal.seleccionarAsientoSucursal(idSala, numeroAsiento);
                        
                        System.out.print("\n  1. Adulto\n  2. Niño\n  Tipo de boleto: ");
                        int tipoBoleto = 0;
                        double precio = 0.0;

                        while (tipoBoleto != 1 && tipoBoleto != 2) {            
                            try {
                                tipoBoleto = Integer.parseInt(scanner.nextLine());
                                if (tipoBoleto == 1) {
                                precio = 80;
                                } else if (tipoBoleto == 2) {
                                    precio = 50;
                                } else {
                                    System.out.println("Opción no válida. Ingresa 1 para adulto o 2 para niño.");
                                    tipoBoleto = 0;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("  Entrada inválida. Debes ingresar un número.");
                                tipoBoleto = 0;
                            }
                        }
                        
                        Boleto boleto = new Boleto(funcion, asiento, precio);
                        carritoBoletos.agregarBoleto(boleto);
                        System.out.println("\n  ¡Boleto añadido al carrito con éxito!");
                    }
                } catch (Exception e) {
                    System.out.println("Ocurrió un error al procesar la reserva de boletos: " + e.getMessage());
                }             
            } else if (!agregarBoletos.equalsIgnoreCase("n") && agregarBoletos.equalsIgnoreCase("N")) {
                System.out.println("Opción invalida. Por favor, elige S o N.");
            }
        } while (!agregarBoletos.equalsIgnoreCase("n"));
        
        String accionCarritoBoletos;
        do {
            System.out.println("\n¿Deseas comprar tu carrito?");
            System.out.println("1. SÍ, comprar carrito");
            System.out.println("2. No, dejar para después");
            System.out.print("Elige una opción: ");
            accionCarritoBoletos = scanner.nextLine();
            
            try {
                switch (accionCarritoBoletos) {
                    case "1":
                        ProcesoCompra.realizarCompraBoletos(cliente, carritoBoletos, sucursal);
                        break;
                    case "2":
                        carritoBoletos.guardarCarritoBoletosEnArchivo(cliente.getIdCliente(), sucursal.getNombreSucursal());
                        break;

                    default:
                        System.out.println("Opción inválida. Por favor, elige 1 o 2.");
                }
            } catch (Exception e) {
                System.out.println("Ha ocurrido un error inesperado." + e.getMessage());
            }
        } while (!accionCarritoBoletos.equals("1") && !accionCarritoBoletos.equals("2"));
    }
    
    public class ProcesoCompra {
        private static final Scanner leeDato = new Scanner(System.in);
        
        public static void realizarCompraBoletos(Cliente cliente, CarritoBoletos carritoBoletos, Sucursal sucursal) throws IOException, InterruptedException{
            CuentaBancaria metodoPago = RegistroCuentaBancaria.cargarMetodoDePago(cliente);
            cliente.setMetodoPago(metodoPago);
            
            System.out.print("Verificando método de pago ");
                for(int i = 0; i < 5; i++)
                    Animacion();
                
            if (cliente.metodoDePagoRegiastrado() == false) {
                System.out.println("\n\nNo tienes un método de pago registrado. Por favor, ingresa los detalles de tu cuenta bancaria.\n");
                metodoPago = registrarMetodoPago(cliente, sucursal.getNombreSucursal());
                RegistroCuentaBancaria.registrarEnArchivo(cliente, metodoPago);
            } else {
                System.out.println("\n\nMétodo de pago registrado: " + cliente.getMetodoPago() + "\n");
            }
            
            System.out.println("¿Deseas usar este método de pago?");
            System.out.println("1. Sí");
            System.out.println("2. No");
            System.out.print("Elige una opción: ");
            
            String opcionMetodoPago = leeDato.nextLine();
            
            if (opcionMetodoPago.equalsIgnoreCase("1")) {
                VentaBoletos ventaBoleto = new VentaBoletos();
                ventaBoleto.registrarVenta(carritoBoletos, sucursal.getNombreSucursal(), cliente);
                procesarPago(cliente, metodoPago, carritoBoletos, sucursal);
            } else {
                metodoPago = registrarMetodoPago(cliente, sucursal.getNombreSucursal());
                RegistroCuentaBancaria.registrarEnArchivo(cliente, metodoPago);
                VentaBoletos ventaBoleto = new VentaBoletos();
                ventaBoleto.registrarVenta(carritoBoletos, sucursal.getNombreSucursal(), cliente);
                procesarPago(cliente, metodoPago, carritoBoletos, sucursal);
            }
        }
        
        public static void realizarCompraProductos(Cliente cliente, Carrito carritoProductos, Sucursal sucursal) throws IOException, InterruptedException{
            CuentaBancaria metodoPago = RegistroCuentaBancaria.cargarMetodoDePago(cliente);
            cliente.setMetodoPago(metodoPago);
            
            System.out.print("Verificando método de pago ");
                for(int i = 0; i < 5; i++)
                    Animacion();
                
            if (cliente.metodoDePagoRegiastrado() == false) {
                System.out.println("\n\nNo tienes un método de pago registrado. Por favor, ingresa los detalles de tu cuenta bancaria.\n");
                metodoPago = registrarMetodoPago(cliente, sucursal.getNombreSucursal());
                RegistroCuentaBancaria.registrarEnArchivo(cliente, metodoPago);
            } else {
                System.out.println("\n\nMétodo de pago registrado: " + cliente.getMetodoPago() + "\n");
            }
            
            System.out.println("¿Deseas usar este método de pago?");
            System.out.println("1. Sí");
            System.out.println("2. No");
            System.out.print("Elige una opción: ");
            
            String opcionMetodoPago = leeDato.nextLine();
            
            if (opcionMetodoPago.equalsIgnoreCase("1")) {
                Venta venta = new Venta();
                venta.registrarVenta(carritoProductos, sucursal.getNombreSucursal(), cliente);
                procesarPagoProductos(cliente, metodoPago, carritoProductos, sucursal);
            } else {
                metodoPago = registrarMetodoPago(cliente, sucursal.getNombreSucursal());
                RegistroCuentaBancaria.registrarEnArchivo(cliente, metodoPago);
                Venta venta = new Venta();
                venta.registrarVenta(carritoProductos, sucursal.getNombreSucursal(), cliente);
                procesarPagoProductos(cliente, metodoPago, carritoProductos, sucursal);
            }
        }
        
        private static CuentaBancaria registrarMetodoPago(Cliente cliente, String sucursal) {
            String titular = cliente.getNombre() + " " + cliente.getApellidoPaterno() + " " + cliente.getApellidoMaterno();

            System.out.print("Ingrese el número de cuenta: ");
            String numeroCuenta = leeDato.nextLine();

            System.out.print("Ingrese la fecha de vencimiento (MM/AAAA): ");
            String fechaVencimiento = leeDato.nextLine();

            System.out.print("Ingrese el CVV: ");
            int cvv = Integer.parseInt(leeDato.nextLine());

            System.out.println("Tipo de cuenta");
            System.out.println("1. Débito");
            System.out.println("2. Crédito");
            System.out.print("Elige una opción: ");
            int respuesta = 0;
            String tipoCuenta = " ";
            
            while (respuesta != 1 && respuesta != 2) {            
                try {
                    respuesta = Integer.parseInt(leeDato.nextLine());
                    if (respuesta == 1) {
                        tipoCuenta = "Debito";
                    } else if (respuesta == 2) {
                        tipoCuenta = "Credito";
                    } else {
                        System.out.println("Opción no válida. Ingresa 1 para debito o 2 para credito.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("  Entrada inválida. Debes ingresar un número.");
                }
            }

            boolean estadoCuenta = true;

            System.out.print("Ingrese el saldo disponible: ");
            double saldo = Double.parseDouble(leeDato.nextLine());

            String ultimaTransaccion = "Registro de método de pago en " + sucursal;

            return new CuentaBancaria(titular, numeroCuenta, fechaVencimiento, cvv, tipoCuenta, estadoCuenta, saldo, ultimaTransaccion);
        }
        
        private static void procesarPago(Cliente cliente, CuentaBancaria metodoPago, CarritoBoletos carritoBoletos, Sucursal sucursal) throws IOException, InterruptedException {
            double totalCompra = carritoBoletos.calcularTotal();
            double saldoDisponible = metodoPago.getSaldo();

            if (saldoDisponible >= totalCompra) {
                System.out.print("\nPago exitoso, realizando la venta ");
                for(int i = 0; i < 5; i++)
                    Animacion();

                carritoBoletos.limpiarCarrito();

                metodoPago.setSaldo(saldoDisponible - totalCompra);

                RegistroCuentaBancaria.registrarEnArchivo(cliente, metodoPago);

                System.out.println("\n¡Compra realizada con éxito! El saldo restante en tu cuenta es: " + metodoPago.getSaldo());
            } else {
                System.out.println("No tienes suficiente saldo para realizar la compra.");
                System.out.println("¿Deseas guardar el carrito para después o eliminarlo?");
                System.out.println("1. Guardar carrito");
                System.out.println("2. Eliminar carrito");
                String opcion = leeDato.nextLine();

                if (opcion.equals("1")) {
                    carritoBoletos.guardarCarritoBoletosEnArchivo(cliente.getIdCliente(), sucursal.getNombreSucursal());
                } else {
                    carritoBoletos.limpiarCarrito();
                    System.out.println("El carrito ha sido eliminado.");
                }
            }
        }
        
        private static void procesarPagoProductos(Cliente cliente, CuentaBancaria metodoPago, Carrito carritoProductos, Sucursal sucursal) throws IOException, InterruptedException {
            double totalCompra = carritoProductos.calcularTotal();
            double saldoDisponible = metodoPago.getSaldo();

            if (saldoDisponible >= totalCompra) {
                System.out.print("\nPago exitoso, realizando la venta ");
                for(int i = 0; i < 5; i++)
                    Animacion();
                carritoProductos.limpiarCarrito();

                metodoPago.setSaldo(saldoDisponible - totalCompra);

                RegistroCuentaBancaria.registrarEnArchivo(cliente, metodoPago);

                System.out.println("\n¡Compra realizada con éxito! El saldo restante en tu cuenta es: " + metodoPago.getSaldo());
            } else {
                System.out.println("No tienes suficiente saldo para realizar la compra.");
                System.out.println("¿Deseas guardar el carrito para después o eliminarlo?");
                System.out.println("1. Guardar carrito");
                System.out.println("2. Eliminar carrito");
                String opcion = leeDato.nextLine();

                if (opcion.equals("1")) {
                    carritoProductos.guardarCarritoEnArchivo(cliente.getIdCliente(), sucursal.getNombreSucursal());
                } else {
                    carritoProductos.limpiarCarrito();
                    System.out.println("El carrito ha sido eliminado.");
                }
            }
        }
    }
}
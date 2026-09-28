package Administrador;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import penta.code.cine.Funciones.MetodosEnGeneral;
import penta.code.cine.Funciones.VerCartelera;
import penta.code.cine.gestion.CineTICS.Sala;
import penta.code.cine.gestion.CineTICS.Sucursal;
import penta.code.cine.gestion.Datos.RegistroEmpleados;
import penta.code.cine.gestion.Dulceria.Dulceria;
import penta.code.cine.gestion.Dulceria.Producto;
import penta.code.cine.gestion.Funciones.Funcion;
import penta.code.cine.gestion.Funciones.Pelicula;
import penta.code.cine.gestion.Persona.Persona;
import penta.code.cine.gestion.Personal.AyudanteGeneral;
import penta.code.cine.gestion.Personal.Cajero;
import penta.code.cine.gestion.Personal.Gerente;

public class Administrador extends Persona{
    
    public Administrador(String nombreString, String apPaternoString, String apMaternoString, String user, String pass) {
        this.nombre = nombreString;
        this.apPaterno = apPaternoString;
        this.apMaterno = apMaternoString;
        this.usuario = user;
        this.contrasena = pass;
    }
    
    public void ActualizarCartelera() {
        Scanner scanner = new Scanner(System.in);

        try {
            MetodosEnGeneral.listaSucursales();
            System.out.print("Ingresa el numero de la sucursal: ");
            String numeroSucursal = scanner.nextLine();
            Sucursal sucursal = MetodosEnGeneral.obtenerSucursalPorSeleccion(Integer.parseInt(numeroSucursal));

            sucursal.cargarEstadoDeSalasDesdeArchivo();
            VerCartelera.mostrarCartelera(sucursal);
            
            System.out.print("Ingrese el número de la sala que desea actualizar (1, 2, 3): ");
            String inputSala = scanner.nextLine();
            int numeroSala;

            try {
                numeroSala = Integer.parseInt(inputSala);
                if (numeroSala <= 0) {
                    System.out.println("El número de sala debe ser un entero positivo.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Número de sala inválido. Debe ser un número entero.");
                return;
            }

            String rutaFuncionesSucursal = "Funciones/" + sucursal.getNombreSucursal();
            File carpetaSucursal = new File(rutaFuncionesSucursal);
            if (!carpetaSucursal.exists()) {
                System.out.println("La sucursal especificada no existe. Creando nueva sucursal...");
                boolean creada = carpetaSucursal.mkdirs();
                if (!creada) {
                    System.out.println("No se pudo crear la carpeta de la sucursal. Verifique los permisos.");
                    return;
                }
            }

            List<Pelicula> peliculasDisponibles = Pelicula.cargarPeliculasDesdeArchivo();
            if (peliculasDisponibles.isEmpty()) {
                System.out.println("No hay películas disponibles para asignar.");
                return;
            }

            System.out.println("\n--- Lista de Películas Disponibles ---");
            for (Pelicula p : peliculasDisponibles) {
                System.out.println(p.toString());
            }

            System.out.println("\nIngrese los IDs de las películas que desea asignar a la sala, separados por comas.");
            System.out.print("Ejemplo: 1,3,5: ");
            String inputIds = scanner.nextLine();

            if (inputIds.isEmpty()) {
                System.out.println("No se ingresaron IDs de películas.");
                return;
            }

            String[] idsSeleccionados = inputIds.split(",");
            List<Pelicula> peliculasSeleccionadas = new ArrayList<>();

            for (String idStr : idsSeleccionados) {
                String idTrim = idStr.trim();
                if (idTrim.isEmpty()) continue;

                Pelicula pelicula = Pelicula.buscarPeliculaPorId(idTrim);
                if (pelicula != null) {
                    peliculasSeleccionadas.add(pelicula);
                } else {
                    System.out.println("Pelicula con ID " + idTrim + " no encontrada. Se omitirá.");
                }
            }

            if (peliculasSeleccionadas.isEmpty()) {
                System.out.println("No se seleccionaron películas válidas para la sala.");
                return;
            }

            List<Funcion> funcionesSeleccionadas = new ArrayList<>();
            int contadorFunciones = 1;
            for (Pelicula p : peliculasSeleccionadas) {
                System.out.print("Ingrese el horario para '" + p.getTitulo() + "' (formato HH:MM a HH:MM): ");
                String horaInicio = scanner.nextLine().trim();

                String idFuncion = MetodosEnGeneral.generarId("fn");
                Funcion funcion = new Funcion(idFuncion, p, horaInicio);
                funcionesSeleccionadas.add(funcion);
            }

            if (funcionesSeleccionadas.isEmpty()) {
                System.out.println("No se crearon funciones válidas para la sala.");
                return;
            }

            String rutaArchivoFunciones = rutaFuncionesSucursal + "/sala_" + numeroSala + "funciones.txt";
            File archivoFunciones = new File(rutaArchivoFunciones);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoFunciones, false))) {
                for (Funcion funcion : funcionesSeleccionadas) {
                    writer.write(funcion.getId() + "," + funcion.getPelicula().getId() + "," + funcion.getHoraInicio());
                    writer.newLine();
                }
                System.out.println("\nCartelera actualizada exitosamente para la sala " + numeroSala + " en la sucursal '" + sucursal.getNombreSucursal() + "'.");
            } catch (IOException e) {
                System.out.println("Error al escribir en el archivo de funciones: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Ocurrió un error inesperado: " + e.getMessage());
        } finally {
        }
    }
    
    public static void VerProductos() {
        Scanner scanner = new Scanner(System.in);

        try {
            MetodosEnGeneral.listaSucursales();
            System.out.print("Ingresa el numero de la sucursal: ");
            String numeroSucursal = scanner.nextLine();
            Sucursal sucursal = MetodosEnGeneral.obtenerSucursalPorSeleccion(Integer.parseInt(numeroSucursal));
            Dulceria dulceria = new Dulceria(sucursal.getNombreSucursal());
            sucursal.cargarEstadoDeSalasDesdeArchivo();
            System.out.println("Cartelera de la sucursal " + sucursal.getNombreSucursal());
            VerCartelera.mostrarCartelera(sucursal);
            System.out.println("Dulceria de la sucursal " + sucursal.getNombreSucursal());
            dulceria.mostrarProductos();
        } catch (Exception e) {
            System.out.println("Ocurrió un error inesperado: " + e.getMessage());
        } finally {
        }
    }
    
    public void ActualizarStockProducto() {
        Scanner scanner = new Scanner(System.in);

        try {
            MetodosEnGeneral.listaSucursales();
            System.out.print("Ingresa el numero de la sucursal: ");
            String numeroSucursal = scanner.nextLine();
            Sucursal sucursal = MetodosEnGeneral.obtenerSucursalPorSeleccion(Integer.parseInt(numeroSucursal));
            Dulceria dulceria = new Dulceria(sucursal.getNombreSucursal());

            System.out.println("\n--- Productos Disponibles en " + sucursal.getNombreSucursal() + " ---");
            dulceria.mostrarProductos();
            
            System.out.print("\nIngrese el código del producto que desea actualizar: ");
            String codigoProducto = scanner.nextLine().trim();
            if (codigoProducto.isEmpty()) {
                System.out.println("El código del producto no puede estar vacío.");
                return;
            }
            
            Producto producto = dulceria.buscarProducto(codigoProducto);
            if (producto == null) {
                System.out.println("Producto no encontrado.");
                return;
            }

            System.out.println("Producto seleccionado: " + producto.getNombre() + " | Stock actual: " + producto.getStock());

            System.out.print("¿Desea (I)ncrementar o (D)ecrementar el stock? (I/D): ");
            String accion = scanner.nextLine().trim().toUpperCase();

            if (!accion.equals("I") && !accion.equals("D")) {
                System.out.println("Acción inválida. Debe ingresar 'I' para incrementar o 'D' para decrementar.");
                return;
            }

            System.out.print("Ingrese la cantidad a " + (accion.equals("I") ? "incrementar" : "decrementar") + ": ");
            String inputCantidad = scanner.nextLine().trim();
            int cantidad;

            try {
                cantidad = Integer.parseInt(inputCantidad);
                if (cantidad <= 0) {
                    System.out.println("La cantidad debe ser un número entero positivo.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Cantidad inválida. Debe ser un número entero.");
                return;
            }

            if (accion.equals("I")) {
                // Incrementar stock
                int nuevoStock = producto.getStock() + cantidad;
                producto.setStock(nuevoStock);
                dulceria.actualizarProductoEnArchivo(producto, sucursal.getNombreSucursal());
                System.out.println("Stock incrementado exitosamente. Nuevo stock: " + producto.getStock());
            } else {
                // Decrementar stock
                if (producto.getStock() >= cantidad) {
                    dulceria.actualizarStock(codigoProducto, cantidad, sucursal.getNombreSucursal());
                    System.out.println("Stock decrementado exitosamente. Nuevo stock: " + producto.getStock());
                } else {
                    System.out.println("Stock insuficiente para el producto: " + producto.getNombre());
                }
            }

        } catch (Exception e) {
            System.out.println("Ocurrió un error inesperado: " + e.getMessage());
        }
    }
        
    public void AgregarProducto() {
        Scanner scanner = new Scanner(System.in);

        try {
            MetodosEnGeneral.listaSucursales();
            System.out.print("Ingresa el numero de la sucursal: ");
            String numeroSucursal = scanner.nextLine();
            Sucursal sucursal = MetodosEnGeneral.obtenerSucursalPorSeleccion(Integer.parseInt(numeroSucursal));
            Dulceria dulceria = new Dulceria(sucursal.getNombreSucursal());

            System.out.print("Ingrese el código del nuevo producto: ");
            String codigo = scanner.nextLine().trim();
            if (codigo.isEmpty()) {
                System.out.println("El código del producto no puede estar vacío.");
                return;
            }

            Producto existente = dulceria.buscarProducto(codigo);
            if (existente != null) {
                System.out.println("Ya existe un producto con el código " + codigo + " en la sucursal '" + sucursal.getNombreSucursal() + "'.");
                return;
            }

            System.out.print("Ingrese el nombre del producto: ");
            String nombre = scanner.nextLine().trim();
            if (nombre.isEmpty()) {
                System.out.println("El nombre del producto no puede estar vacío.");
                return;
            }

            System.out.print("Ingrese el precio del producto: ");
            String inputPrecio = scanner.nextLine().trim();
            double precio;

            try {
                precio = Double.parseDouble(inputPrecio);
                if (precio < 0) {
                    System.out.println("El precio no puede ser negativo.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Precio inválido. Debe ser un número.");
                return;
            }

            System.out.print("Ingrese la categoría del producto (Bebidas/Botanas/Dulces/Comida/Souvenirs): ");
            String categoria = scanner.nextLine().trim();
            if (categoria.isEmpty()) {
                System.out.println("La categoría del producto no puede estar vacía.");
                return;
            }

            System.out.print("Ingrese el stock del producto: ");
            String inputStock = scanner.nextLine().trim();
            int stock;

            try {
                stock = Integer.parseInt(inputStock);
                if (stock < 0) {
                    System.out.println("El stock no puede ser negativo.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Stock inválido. Debe ser un número entero.");
                return;
            }

            Producto nuevoProducto = new Producto(codigo, nombre, precio, categoria, stock);

            dulceria.agregarProducto(nuevoProducto, sucursal.getNombreSucursal());

            System.out.println("Producto agregado exitosamente a la sucursal '" + sucursal.getNombreSucursal() + "'.");

        } catch (Exception e) {
            System.out.println("Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    public void VerEmpleados() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n  --- Ver Empleados ---");
        System.out.println("  Seleccione el tipo de empleado a ver:");
        System.out.println("  1. Ayudantes Generales");
        System.out.println("  2. Cajeros");
        System.out.println("  3. Gerentes");
        System.out.println("  4. Todos");
        System.out.print("  Seleccione una opción (1-4): ");
        String opcion = scanner.nextLine().trim();

        try {
            switch (opcion) {
                case "1":
                    mostrarAyudantesGenerales();
                    break;
                case "2":
                    mostrarCajeros();
                    break;
                case "3":
                    mostrarGerentes();
                    break;
                case "4":
                    mostrarTodosLosEmpleados();
                    break;
                default:
                    System.out.println("Opción no válida. Por favor, seleccione una opción del 1 al 4.");
            }
        } catch (IOException e) {
            System.out.println("Error al cargar los empleados: " + e.getMessage());
        }
    }

    public static void mostrarAyudantesGenerales() throws IOException {
        List<AyudanteGeneral> ayudantes = RegistroEmpleados.cargarAyudantesGenerales();
        if (ayudantes.isEmpty()) {
            System.out.println("No hay ayudantes generales registrados.");
        } else {
            String formato = "| %-12s | %-15s | %-17s | %-33s | %-15s |%n";
            String separador = "+--------------+-----------------+-------------------+-----------------------------------+-----------------+";                                                          
            System.out.println(separador);
            System.out.format(formato, "ID Empleado", "Nombre", "Apellido Paterno", "Correo", "Teléfono");
            System.out.println(separador);

            for (AyudanteGeneral ayudante : ayudantes) {
                System.out.format(formato,
                    ayudante.getIdEmpleado(),
                    ayudante.getNombre(),
                    ayudante.getApellidoPaterno(),
                    ayudante.getCorreo(),
                    ayudante.getNumeroDeTelefono()
                );
            }
            System.out.println(separador);
        }
    }

    public static void mostrarCajeros() throws IOException {
        List<Cajero> cajeros = RegistroEmpleados.cargarCajeros();
        if (cajeros.isEmpty()) {
            System.out.println("No hay cajeros registrados.");
        } else {
            String formato = "| %-12s | %-15s | %-17s | %-33s | %-15s |%n";
            String separador = "+--------------+-----------------+-------------------+-----------------------------------+-----------------+";  
            System.out.println(separador);
            System.out.format(formato, "ID Empleado", "Nombre", "Apellido Paterno", "Correo", "Teléfono");
            System.out.println(separador);

            for (Cajero cajero : cajeros) {
                System.out.format(formato,
                    cajero.getIdEmpleado(),
                    cajero.getNombre(),
                    cajero.getApellidoPaterno(),
                    cajero.getCorreo(),
                    cajero.getNumeroDeTelefono()
                );
            }
            System.out.println(separador);
        }
    }

    public static void mostrarGerentes() throws IOException {
        List<Gerente> gerentes = RegistroEmpleados.cargarGerentes();
        if (gerentes.isEmpty()) {
            System.out.println("No hay gerentes registrados.");
        } else {
            String formato = "| %-12s | %-15s | %-17s | %-33s | %-15s |%n";
            String separador = "+--------------+-----------------+-------------------+-----------------------------------+-----------------+";  
            System.out.println(separador);
            System.out.format(formato, "ID Empleado", "Nombre", "Apellido Paterno", "Correo", "Teléfono");
            System.out.println(separador);

            for (Gerente gerente : gerentes) {
                System.out.format(formato,
                    gerente.getIdEmpleado(),
                    gerente.getNombre(),
                    gerente.getApellidoPaterno(),
                    gerente.getCorreo(),
                    gerente.getNumeroDeTelefono()
                );
            }
            System.out.println(separador);
        }
    }

    public static void mostrarTodosLosEmpleados() throws IOException {
        System.out.println("\n--- Ayudantes Generales ---");
        mostrarAyudantesGenerales();

        System.out.println("\n--- Cajeros ---");
        mostrarCajeros();

        System.out.println("\n--- Gerentes ---");
        mostrarGerentes();
    }
    
    public static void AgregarEmpleados() {
        
    }
    
    public static void VerEstadisticasDeVentas() {
        
    }
    
    public static void GenerarReporteDeVentas() {
        
    }
}

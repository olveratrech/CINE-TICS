# Inventario de fuentes

Generado con `python3 scripts/audit_inventory.py`. No incluye contenido de archivos de datos.

Fuentes Java: 33. Líneas: 4739.

| Archivo | Líneas | Destino previsto |
|---|---:|---|
| `Administrador/Administrador.java` | 444 | Casos de uso administrativos |
| `SIGA/ProcesamientoPedidos.java` | 16 | Pedidos y simulación |
| `com/penta/code/cine/AppCineTics.java` | 11 | Punto de entrada de consola |
| `penta/code/cine/Funciones/BuscarPelicula.java` | 114 | Cartelera y casos de uso |
| `penta/code/cine/Funciones/BuscarProducto.java` | 33 | Cartelera y casos de uso |
| `penta/code/cine/Funciones/MetodosEnGeneral.java` | 1074 | Separar menús, compra, identidad y navegación |
| `penta/code/cine/Funciones/VerCartelera.java` | 33 | Cartelera y casos de uso |
| `penta/code/cine/gestion/CineTICS/Asiento.java` | 38 | Cartelera, reservas y ventas |
| `penta/code/cine/gestion/CineTICS/Boleto.java` | 45 | Cartelera, reservas y ventas |
| `penta/code/cine/gestion/CineTICS/CarritoBoletos.java` | 88 | Cartelera, reservas y ventas |
| `penta/code/cine/gestion/CineTICS/Sala.java` | 130 | Cartelera, reservas y ventas |
| `penta/code/cine/gestion/CineTICS/Sucursal.java` | 194 | Cartelera, reservas y ventas |
| `penta/code/cine/gestion/CineTICS/VentaBoletos.java` | 202 | Cartelera, reservas y ventas |
| `penta/code/cine/gestion/Clientes/AutenticacionCliente.java` | 46 | Identidad y perfiles |
| `penta/code/cine/gestion/Clientes/Cliente.java` | 243 | Identidad y perfiles |
| `penta/code/cine/gestion/Datos/RegistroClientes.java` | 191 | Adaptadores e importador de legado |
| `penta/code/cine/gestion/Datos/RegistroEmpleados.java` | 301 | Adaptadores e importador de legado |
| `penta/code/cine/gestion/Dulceria/Carrito.java` | 90 | Inventario y ventas |
| `penta/code/cine/gestion/Dulceria/Dulceria.java` | 139 | Inventario y ventas |
| `penta/code/cine/gestion/Dulceria/Producto.java` | 90 | Inventario y ventas |
| `penta/code/cine/gestion/Dulceria/Venta.java` | 207 | Inventario y ventas |
| `penta/code/cine/gestion/Financiera/CuentaBancaria.java` | 108 | Pago simulado; reemplazar almacenamiento sensible |
| `penta/code/cine/gestion/Financiera/RegistroCuentaBancaria.java` | 62 | Pago simulado; reemplazar almacenamiento sensible |
| `penta/code/cine/gestion/Funciones/Funcion.java` | 100 | Cartelera y casos de uso |
| `penta/code/cine/gestion/Funciones/Pelicula.java` | 103 | Cartelera y casos de uso |
| `penta/code/cine/gestion/Persona/Persona.java` | 160 | Perfiles; revisar herencia |
| `penta/code/cine/gestion/Personal/AutenticacionEmpleado.java` | 53 | Personal y permisos |
| `penta/code/cine/gestion/Personal/AyudanteGeneral.java` | 75 | Personal y permisos |
| `penta/code/cine/gestion/Personal/Cajero.java` | 98 | Personal y permisos |
| `penta/code/cine/gestion/Personal/Empleado.java` | 101 | Personal y permisos |
| `penta/code/cine/gestion/Personal/Gerente.java` | 71 | Personal y permisos |
| `penta/code/cine/gestion/ProgramaLealtad/ProgramaLealtad.java` | 66 | Lealtad |
| `penta/code/rob/Rob.java` | 13 | Asistente |

## Métodos públicos vacíos detectados

- `Administrador/Administrador.java`: `AgregarEmpleados` (línea 433).
- `Administrador/Administrador.java`: `VerEstadisticasDeVentas` (línea 437).
- `Administrador/Administrador.java`: `GenerarReporteDeVentas` (línea 441).
- `SIGA/ProcesamientoPedidos.java`: `ProcesarCompraDeBoleto` (línea 9).
- `SIGA/ProcesamientoPedidos.java`: `ProcesarCompraDeAlimentos` (línea 13).

La detección es orientativa; las clases vacías, lógica incompleta y código sin uso requieren revisión adicional. Un método no se elimina solo por estar vacío.

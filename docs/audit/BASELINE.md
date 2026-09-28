# Auditoría inicial

Referencia: `baseline-java25`, creada antes de cambios de modernización. La inspección es estática salvo donde se menciona una prueba. Ver `INVENTORY.md` para rutas exactas y tamaños.

## Qué conservar y qué reemplazar

| Área | Decisión | Motivo |
|---|---|---|
| Película, función, sala, asiento y sucursal | Conservar conceptos; rediseñar persistencia y disponibilidad | Funcion solo guarda hora y Sala mantiene ocupación física; faltan fecha y disponibilidad por función |
| Clientes y personal | Conservar campos útiles; separar identidad, perfil y permisos | Contraseñas y operaciones de consola están mezcladas con modelos |
| Carritos y ventas | Reemplazar implementación gradualmente | Importes, cantidades, persistencia y pago no forman una operación coherente |
| Programa de lealtad | Conservar porcentajes; sustituir progresión | Usuario confirmó acumulación adicional por nivel |
| RegistroClientes/RegistroEmpleados y lectores TXT | Mantener hasta disponer de importador verificado | Evitar perder datos y relaciones al cambiar a Oracle |
| Rob y S.I.G.A. | Implementar; no eliminar por estar vacíos | Son capacidades expresamente requeridas |
| MetodosEnGeneral/Administrador | Descomponer por casos de uso | Menús, archivos y negocio acoplados; funciones administrativas vacías |
| target, cachés y respaldos | Excluir de Git | Son generados o privados; no borrarlos como parte del diagnóstico |
| Datos históricos | Preservar localmente; importar después de validación | No asumir que son fixtures ni incorporarlos al repositorio |

## Defectos confirmados por lectura

| ID | Prioridad | Evidencia | Resultado esperado y prueba pendiente |
|---|---|---|---|
| BUG-01 | P0 | `Dulceria/Carrito.calcularTotal` suma precio sin cantidad | 2 productos de $65 deben totalizar $130, igual que la presentación del carrito |
| BUG-02 | P0 | `MetodosEnGeneral.ProcesoCompra` registra venta antes de `procesarPago` | Pago rechazado no crea venta exitosa, ticket ni puntos |
| BUG-03 | P0 | `RegistroCuentaBancaria` serializa/carga CVV; autenticadores comparan texto plano | Modelo nuevo no guarda CVV; autenticación con hash y credenciales ficticias |
| BUG-04 | P1 | `Carrito.agregarProducto` solo compara stock >= cantidad | Rechazar cantidades cero/negativas y exceso al agregar varias veces |
| BUG-05 | P1 | `Sala` contiene una única lista de asientos ocupados; Funcion no tiene fecha | Dos funciones tienen disponibilidad independiente; dos compradores no confirman el mismo asiento |
| BUG-06 | P1 | `ProgramaLealtad.actualizarNivel` compara montoCompra individual | Compras de $600 y $400 alcanzan nivel 5; luego $1,200 adicionales nivel 10 |
| BUG-07 | P1 | `AutenticacionCliente` no valida longitud del registro y compara usuario, no idCliente | Archivo inválido produce error controlado; login por ID de socio conforme a especificación |
| BUG-08 | P1 | `Pelicula` solo tiene id/título/duración/género | Añadir clasificación, director, actores y sinopsis; duración tipada |
| BUG-09 | P1 | `Administrador.AgregarEmpleados`, `VerEstadisticasDeVentas`, `GenerarReporteDeVentas` vacíos | Implementar y verificar autorización y resultados |
| BUG-10 | P1 | `Rob` vacío; ambos métodos de `ProcesamientoPedidos` vacíos | Casos de uso reales; alertas y simulación comprobables |

Estos defectos quedan registrados, no corregidos en H0/H1. Las pruebas verdes de caracterización no certifican que una compra completa sea correcta.

## Pruebas iniciales

`LegacyRulesTest`: total y eliminación de boletos, rechazo de cantidad superior al stock, eliminación de producto, capacidad e identificadores de sala, porcentajes de lealtad por nivel y acumulación de puntos. No se fija como correcto el cálculo defectuoso de múltiples unidades ni la progresión antigua.

`scripts/smoke_console.py`: arranque y salida en directorio temporal sin escritura de datos. No valida compra, persistencia ni interfaz web.

No había pruebas Java en `src/test` al iniciar. Los flujos funcionales restantes deben comprobarse con fixtures ficticias; no se inspeccionan valores de clientes ni tarjetas para este inventario.

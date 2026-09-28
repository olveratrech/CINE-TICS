# Matriz de requisitos

Fuente: PDF académico de siete páginas aportado por el propietario. Estado inicial basado en inspección; ver README de esta carpeta para interpretar los estados.

Las rutas de evidencia son relativas a los paquetes históricos de `Cine/src/main/java`. Los criterios describen el resultado futuro exigido; no afirman que ya esté implementado.

Total: 60 grupos de requisitos.

## R01 — Cuatro sucursales CU, Universidad, Delta, Xochimilco

Página: 1 · Estado: **Parcial** · Prioridad: P1 · Hito: H3

Evidencia: CineTICS/Sucursal; datos locales.

Aceptación: Seed con cuatro sucursales y IDs únicos.

## R02 — Cinco funciones por sucursal y horarios del cuadro

Página: 1 · Estado: **Parcial** · Prioridad: P1 · Hito: H3

Evidencia: Funciones/Funcion; archivos Funciones.

Aceptación: 20 funciones con fecha, sala y horario; detectar solapamientos.

## R03 — Capacidades de las 12 salas del cuadro

Página: 2 · Estado: **Parcial** · Prioridad: P1 · Hito: H3

Evidencia: CineTICS/Sala; registroSucursales.

Aceptación: Capacidades 120/150/200,100/160/175,80/100/150,90/125/140.

## R04 — Registro de cliente y campos personales obligatorios

Página: 2 · Estado: **Parcial** · Prioridad: P1 · Hito: H4

Evidencia: Clientes/Cliente; Datos/RegistroClientes.

Aceptación: Validar nombre, apellidos, dirección, celular y correo; persistir.

## R05 — Contraseña de al menos ocho caracteres

Página: 2 · Estado: **Por verificar** · Prioridad: P0 · Hito: H4

Evidencia: Persona/Persona; Clientes/Cliente.

Aceptación: Rechazar longitud menor; almacenar hash en versión nueva.

## R06 — Cartelera pública y compra con cuenta

Página: 2 · Estado: **Parcial** · Prioridad: P1 · Hito: H4

Evidencia: Funciones/MetodosEnGeneral.

Aceptación: Visitante consulta; compra exige identidad autenticada.

## R07 — Método de pago asociado antes de comprar

Página: 2 · Estado: **Parcial** · Prioridad: P0 · Hito: H4

Evidencia: Financiera; ProcesoCompra.

Aceptación: Token simulado asociado; no CVV persistente; sin cargo real.

## R08 — Rob encuentra sucursal, sala y horario disponibles

Página: 2 · Estado: **Pendiente** · Prioridad: P1 · Hito: H7

Evidencia: rob/Rob vacío.

Aceptación: Resultados de disponibilidad reales y elección de sucursal.

## R09 — Boleto adulto $80 y niño $50

Página: 2 · Estado: **Parcial** · Prioridad: P1 · Hito: H2

Evidencia: Sala.showFunciones; Boleto; LegacyRulesTest.

Aceptación: Precios únicos en política de dominio y ticket consistente.

## R10 — Ficha completa de película

Página: 2 · Estado: **Parcial** · Prioridad: P1 · Hito: H4

Evidencia: Funciones/Pelicula (BUG-08).

Aceptación: Título, clasificación, director, duración, actores y sinopsis visibles.

## R11 — Inicio de sesión por ID de socio/empleado

Página: 2 · Estado: **Parcial** · Prioridad: P0 · Hito: H4

Evidencia: AutenticacionCliente; AutenticacionEmpleado.

Aceptación: Ambos roles acceden por ID; permisos verificados en backend.

## R12 — Inscripción opcional a lealtad

Página: 2 · Estado: **Parcial** · Prioridad: P1 · Hito: H5

Evidencia: Cliente.unirseProgramaLealtad.

Aceptación: Persistir inscripción y recuperar puntos al iniciar sesión.

## R13 — Porcentajes 3%, 7.5%, 15% y valor $0.0027 por punto

Página: 2 · Estado: **Parcial** · Prioridad: P1 · Hito: H5

Evidencia: ProgramaLealtad; LegacyRulesTest.

Aceptación: Ejemplos de conversión y redondeo aprobados; saldo persistente.

## R14 — Ascensos 0→5→10→15

Página: 2 · Estado: **Parcial** · Prioridad: P1 · Hito: H2

Evidencia: ProgramaLealtad (BUG-06); D-02.

Aceptación: Metas acumuladas $1,000, $2,200, $3,550 con compras múltiples.

## R15 — Regalo aleatorio cada $500 en nivel 15

Página: 2 · Estado: **Pendiente** · Prioridad: P1 · Hito: H5

Evidencia: ProgramaLealtad sin premios.

Aceptación: Un premio por tramo; stock y elegibilidad; sorteo reproducible en test.

## R16 — Ver cartelera de sucursal seleccionada

Página: 3 · Estado: **Parcial** · Prioridad: P1 · Hito: H4

Evidencia: Funciones/VerCartelera.

Aceptación: Selección se conserva y muestra funciones de esa sucursal.

## R17 — Buscar película y ampliar búsqueda a otras sucursales

Página: 3 · Estado: **Parcial** · Prioridad: P1 · Hito: H4

Evidencia: Funciones/BuscarPelicula.

Aceptación: Proponer ampliación cuando no exista y cambiar al seleccionar resultado.

## R18 — Cambiar sucursal

Página: 3 · Estado: **Parcial** · Prioridad: P1 · Hito: H4

Evidencia: MetodosEnGeneral.obtenerSucursalPorSeleccion.

Aceptación: Evitar mezclar carritos e inventario de distintas sucursales.

## R19 — Buscar productos y añadir cantidad al carrito

Página: 3 · Estado: **Parcial** · Prioridad: P0 · Hito: H2

Evidencia: Funciones/BuscarProducto; Dulceria/Carrito; H2 carrito/autorización con pruebas, flujo completo sigue parcial.

Aceptación: Cantidad positiva; disponibilidad y total coherentes.

## R20 — Ver, editar y comprar carrito

Página: 3 · Estado: **Parcial** · Prioridad: P0 · Hito: H4

Evidencia: Carrito; CarritoBoletos; ProcesoCompra; H2 carrito/autorización con pruebas, flujo completo sigue parcial.

Aceptación: Añadir, quitar, cambiar cantidad y comprar con total correcto.

## R21 — Actualizar datos personales

Página: 3 · Estado: **Parcial** · Prioridad: P1 · Hito: H4

Evidencia: Cliente.actualizarDatos; RegistroClientes.

Aceptación: Solo dueño autenticado cambia datos válidos y persisten.

## R22 — Consultar compras propias

Página: 3 · Estado: **Parcial** · Prioridad: P1 · Hito: H4

Evidencia: Venta.verMisCompras; VentaBoletos.verMisCompras.

Aceptación: Historial completo; no consultar compras de otro cliente.

## R23 — Consultar puntos propios

Página: 3 · Estado: **Parcial** · Prioridad: P1 · Hito: H5

Evidencia: Cliente; ProgramaLealtad.

Aceptación: Saldo recuperado tras nueva sesión y aislado por usuario.

## R24 — Ofrecer alimentos con boleto e identificador de consumo

Página: 3 · Estado: **Parcial** · Prioridad: P1 · Hito: H5

Evidencia: ProcesoCompra; carritos separados.

Aceptación: Compra combinada y código de entrega único sin doble canje.

## R25 — Diez productos, códigos, precios y stock del cuadro

Página: 3 · Estado: **Por verificar** · Prioridad: P1 · Hito: H3

Evidencia: StockProductos local; Producto.

Aceptación: Seed respeta tabla por sucursal; erratas resueltas (D-07).

## R26 — Almacenamiento Productos, Clientes, Empleados, Gerentes y adicionales

Página: 3 · Estado: **Adaptado** · Prioridad: P0 · Hito: H3

Evidencia: Registros TXT → Oracle propuesto.

Aceptación: Esquema y migración conservan entidades y relaciones; conciliación.

## R27 — Tickets con nombre Año_Mes_Día_IDTienda_IDTicket.txt

Página: 4 · Estado: **Parcial** · Prioridad: P2 · Hito: H5

Evidencia: Venta.generarTicket; VentaBoletos.generarTicket.

Aceptación: Exportación con nombre requerido, codificación y datos completos.

## R28 — Validar existencias antes de agregar al carrito

Página: 4 · Estado: **Parcial** · Prioridad: P0 · Hito: H2

Evidencia: Carrito.agregarProducto; LegacyRulesTest; H2 carrito/autorización con pruebas, flujo completo sigue parcial.

Aceptación: Rechazar falta, acumulación excesiva y stock agotado al confirmar.

## R29 — Solicitar factura, RFC, dirección fiscal y forma de pago

Página: 4 · Estado: **Pendiente** · Prioridad: P1 · Hito: H5

Evidencia: Sin flujo fiscal localizado; D-01.

Aceptación: Solicitud y comprobante simulado; confirmar forma de pago.

## R30 — Campos de empleado/gerente y sucursal

Página: 4 · Estado: **Parcial** · Prioridad: P1 · Hito: H5

Evidencia: Personal; Persona; Datos/RegistroEmpleados.

Aceptación: Nombre, dirección, correo, teléfono, RFC, número, tipo y sucursal.

## R31 — Ticket virtual al finalizar pago

Página: 4 · Estado: **Parcial** · Prioridad: P0 · Hito: H4

Evidencia: Venta; VentaBoletos; BUG-02; H2 carrito/autorización con pruebas, flujo completo sigue parcial.

Aceptación: Ticket solo tras aprobación; fecha, ID, sucursal, detalle e importes.

## R32 — Eliminar producto y modificar unidades

Página: 4 · Estado: **Parcial** · Prioridad: P1 · Hito: H2

Evidencia: Carrito.eliminarProducto; no operación específica de cantidad; H2 carrito/autorización con pruebas, flujo completo sigue parcial.

Aceptación: Modificar y eliminar recalcula sin alterar el stock del catálogo.

## R33 — 24 usuarios: 3 clientes, 2 empleados, 1 gerente por sucursal

Página: 4 · Estado: **Por verificar** · Prioridad: P1 · Hito: H3

Evidencia: Archivos históricos no usados como fixtures.

Aceptación: Seed ficticio: 12 clientes, 8 empleados y 4 gerentes.

## R34 — Acerca de con equipo, integrantes y contribuciones

Página: 4 · Estado: **Parcial** · Prioridad: P2 · Hito: H8

Evidencia: MetodosEnGeneral.menuGeneral.

Aceptación: Pantalla con información y contribución de cada integrante.

## R35 — Alerta S.I.G.A. cuando stock menor a cinco

Página: 4 · Estado: **Pendiente** · Prioridad: P1 · Hito: H6

Evidencia: ProcesamientoPedidos vacío.

Aceptación: Transición a 4 genera alerta; 5 no; alerta accesible al administrador.

## R36 — Actualizar cartelera

Página: 5 · Estado: **Parcial** · Prioridad: P1 · Hito: H5

Evidencia: Administrador.ActualizarCartelera.

Aceptación: Cambios válidos autorizados; horarios sin choque.

## R37 — Ver productos, actualizar stock y agregar productos

Página: 5 · Estado: **Parcial** · Prioridad: P1 · Hito: H5

Evidencia: Administrador.VerProductos/ActualizarStockProducto/AgregarProducto.

Aceptación: Permisos, cantidades válidas, auditoría y persistencia.

## R38 — Ver empleados y agregar empleados

Página: 5 · Estado: **Parcial** · Prioridad: P1 · Hito: H5

Evidencia: VerEmpleados existe; AgregarEmpleados vacío.

Aceptación: Alta válida, ID único y visibilidad limitada a cadena/sucursal.

## R39 — Estadísticas por sucursal, función, producto y total

Página: 5 · Estado: **Pendiente** · Prioridad: P1 · Hito: H5

Evidencia: Administrador.VerEstadisticasDeVentas vacío.

Aceptación: Agregados coinciden con ventas confirmadas y filtros de tenant.

## R40 — Reportes por sucursal, función, producto y total

Página: 5 · Estado: **Pendiente** · Prioridad: P1 · Hito: H5

Evidencia: Administrador.GenerarReporteDeVentas vacío.

Aceptación: Exportación reproducible de resultados y periodo explícito.

## R41 — Simular compra de alimentos, ingreso, inicio y fin de función

Página: 5 · Estado: **Pendiente** · Prioridad: P1 · Hito: H6

Evidencia: S.I.G.A. vacío; no orquestador localizado.

Aceptación: Secuencia de estados y eventos determinista.

## R42 — Cinco cajas con identificador y cajero; precompras primero

Página: 5 · Estado: **Pendiente** · Prioridad: P1 · Hito: H6

Evidencia: Cajero tiene numCaja; sin planificador.

Aceptación: Asignar cajas y procesar precompras antes de compras en sitio; D-06.

## R43 — Subtítulos durante diez segundos y fin de función

Página: 5 · Estado: **Pendiente** · Prioridad: P2 · Hito: H6

Evidencia: No implementación localizada.

Aceptación: Duración observable de 10 s; reloj controlable en pruebas.

## R44 — Bloquear inicio si falta mínimo 38 por producto, excepto peluche

Página: 5 · Estado: **Pendiente** · Prioridad: P1 · Hito: H6

Evidencia: No validación 38 localizada.

Aceptación: 37 bloquea, 38 permite; reposición habilita; excluir peluche.

## R45 — Mostrar alertas generadas al finalizar simulación

Página: 5 · Estado: **Pendiente** · Prioridad: P1 · Hito: H6

Evidencia: Sin registro de alertas localizado.

Aceptación: Historial de alertas consultable por administrador de la cadena.

## R46 — Mostrar marca, sucursal y lema durante uso

Página: 5 · Estado: **Parcial** · Prioridad: P2 · Hito: H4

Evidencia: Menús y tickets.

Aceptación: Encabezado web mantiene sucursal activa y marca en recorridos.

## R47 — Documentación PDF con portada, índice e introducción

Página: 6 · Estado: **Por verificar** · Prioridad: P2 · Hito: H8

Evidencia: No entregable localizado en la copia de código.

Aceptación: Documento de versión final generado y revisado visualmente.

## R48 — UML, código, datos de prueba y descripción de archivos

Página: 6 · Estado: **Por verificar** · Prioridad: P2 · Hito: H8

Evidencia: No documentación original dentro del proyecto.

Aceptación: UML/ER/API, código versionado y fixtures ficticias documentados.

## R49 — Descripción y capturas de simulación/pruebas, errores y conclusiones

Página: 6 · Estado: **Pendiente** · Prioridad: P2 · Hito: H8

Evidencia: Nueva documentación inicial únicamente.

Aceptación: Evidencias reales y catálogo de errores; conclusiones de autoría humana.

## R50 — Manuales cliente y administrador

Página: 6 · Estado: **Pendiente** · Prioridad: P2 · Hito: H8

Evidencia: Sin manuales en copia de código.

Aceptación: Recorridos documentados y reproducidos por otro usuario.

## R51 — Video técnico hasta veinte minutos y recursos

Página: 6 · Estado: **Histórico** · Prioridad: P2 · Hito: H8

Evidencia: Entregable externo al código; no inspeccionado.

Aceptación: Guion y demo; grabación/publicación coordinadas con propietario.

## R52 — Presentación de empresa y exposición hasta doce minutos

Página: 7 · Estado: **Histórico** · Prioridad: P2 · Hito: H8

Evidencia: Entregable académico externo.

Aceptación: Material de portafolio adaptado; no asumir entrega escolar vigente.

## R53 — Paquete de entregables organizado, fuente y datos de prueba

Página: 6–7 · Estado: **Adaptado** · Prioridad: P2 · Hito: H8

Evidencia: Estructura documental nueva.

Aceptación: Release ZIP con documentación, manuales, diagramas y demo sin datos privados.

## N01 — Oracle Multitenant en Docker

Página: Usuario · Estado: **Pendiente** · Prioridad: P0 · Hito: H3

Evidencia: ADR-001.

Aceptación: Dos cadenas aisladas, migraciones, volumen, reinicio y restauración.

## N02 — Interfaz web

Página: Usuario · Estado: **Pendiente** · Prioridad: P1 · Hito: H4

Evidencia: ADR-001.

Aceptación: Cliente y personal usan flujos sin consola; accesibilidad básica.

## N03 — Refactorización y patrones justificados

Página: Usuario · Estado: **En curso** · Prioridad: P1 · Hito: H2

Evidencia: Dominio de carrito y caso de uso CompraSimulada integrados a consola; H2 parcial.

Aceptación: Reglas separadas de UI/persistencia y dependencias entre módulos controladas.

## N04 — Eliminar elementos innecesarios sin perder capacidades

Página: Usuario · Estado: **En curso** · Prioridad: P1 · Hito: H2

Evidencia: Inventario; respaldo.

Aceptación: Eliminar solo tras mapa de uso y reemplazo probado.

## R54 — Rob asesora a empleados en entrega y gerentes en estadísticas

Página: 1 · Estado: **Pendiente** · Prioridad: P1 · Hito: H7

Evidencia: Rob vacío.

Aceptación: Consultar pedidos y estadísticas reales según rol; no exponer datos de otra cadena.

## R55 — Alimentos antes y durante la función; Rob comunica pedidos a S.I.G.A.

Página: 1 · Estado: **Pendiente** · Prioridad: P1 · Hito: H6

Evidencia: ProcesamientoPedidos vacío.

Aceptación: Pedido con origen, función y estado; validación de horario y entrega antes/durante función.

## R56 — Software escalable para licenciar a otras cadenas como SaaS

Página: 1 · Estado: **Pendiente** · Prioridad: P1 · Hito: H3

Evidencia: Sin modelo tenant; ADR-001.

Aceptación: Dos cadenas configurables, aislamiento y alta reproducible; límites operativos documentados.

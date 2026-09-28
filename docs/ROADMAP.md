# Roadmap de modernización

Fecha: 2026-09-28. Etapas pequeñas con evidencia; no se considera terminada una función por tener un botón o una clase.

| Hito | Alcance | Criterio de salida | Estado |
|---|---|---|---|
| H0 Diagnóstico | Respaldo, inventario, matriz y decisiones | Trazabilidad de requisitos; decisiones abiertas identificadas | Entregado como diagnóstico inicial; flujos parciales requieren pruebas |
| H1 Base | Git, Wrapper, convenciones, pruebas iniciales, workflow | Compilación limpia y arranque sin datos personales | Implementado localmente; CI remoto pendiente de repositorio remoto |
| H2 Dominio comercial | Dinero, cantidades, orden de pago, reservas por función, lealtad acumulada | Regresiones de BUG-01 a BUG-07 y reglas separadas de consola | En curso: carrito, autorización previa y reservas temporales Oracle; venta atómica de boletos implementada; falta lealtad |
| H3 Oracle | Compose, PDB, esquema, migraciones, importador y seed ficticio | Dos cadenas aisladas, datos conciliados y recuperación verificada | En curso: Oracle local, catálogo y compras transaccionales verificados; importación y restauración pendientes |
| H4 Compra web | Identidad, catálogo, reservas, carrito, pago demo y ticket | E2E visitante → registro → compra → historial; sin doble reserva | Pendiente |
| H5 Operación | Inventario, empleados, lealtad completa, datos fiscales y reportes | Requisitos comerciales y administrativos verificados, permisos por rol | Pendiente |
| H6 S.I.G.A. | Pedidos, cinco cajas, prioridades, alertas y simulación | Escenarios deterministas de stock, entrega y fin de función | Pendiente |
| H7 Rob | Búsqueda determinista, luego IA con herramientas limitadas | Respuestas con datos reales y acciones autorizadas | Pendiente |
| H8 Entrega | Accesibilidad, carga, despliegue, respaldo, manuales y demostración | Instalación limpia por tercero y matriz funcional cerrada | Pendiente |

## Próxima iteración: H2

1. Reproducir cantidades incorrectas y pago rechazado con pruebas que fallen.
2. Extraer carrito de productos con cantidad separada del stock; Money/BigDecimal, cantidades positivas y suma correcta.
3. Crear caso de uso de compra simulado con estados e idempotencia; no escribir ventas ni puntos antes de aprobación.
4. Modelar reserva de asiento por función y vencimiento. Diseñar restricción Oracle antes de exponer compra concurrente.
5. Implementar lealtad acumulada con metas adicionales confirmadas; resolver D-03 y D-04 antes de codificar beneficios de frontera.
6. Mantener la consola utilizable mientras delega a los nuevos casos de uso.

Cada entrega debe contener código, prueba del criterio, documentación mínima y registro de decisiones. Los tiempos se estimarán al desglosar cada hito; no existe todavía una estimación fiable del proyecto entero.

## Entrega final

- Recorrido cliente, empleado y administrador; datos ficticios reproducibles.
- Pruebas unitarias, integración Oracle, aislamiento, concurrencia y navegador.
- Logs sin secretos, manejo de errores, métricas y límites de operación documentados.
- API documentada, modelo entidad-relación, UML y decisiones de arquitectura.
- Manual de usuario y operación; restauración probada.
- Demo de pagos/facturación identificada como simulada.
- Material académico y de portafolio trazado en R47–R53.

Se adelantó la infraestructura de H3 a petición del propietario para implementar las garantías transaccionales pendientes de H2 directamente sobre Oracle. La consola histórica conserva persistencia TXT; existe una consola demo independiente de compras Oracle. Funciones, asientos y reservas con vencimiento ya tienen adaptador Oracle y consola demo. La reserva vigente ya se convierte en boletos pagados de forma atómica. El siguiente corte puede preparar identidad/autorización y API web sobre estos casos de uso; compra combinada y lealtad siguen pendientes.

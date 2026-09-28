# Decisiones de producto

Fuente: documento académico `8_ProyectoFinalPOO_2025-1-2.pdf`, siete páginas, proporcionado por el usuario. El PDF es una especificación histórica; la modernización añade web y Oracle con autorización del propietario.

## Confirmadas por el propietario (2026-09-28)

**D-01 — Pagos y facturación simulados.** La primera versión no cobrará dinero ni emitirá comprobantes fiscales reales. Se conservarán datos de solicitud fiscal y comprobantes claramente identificados como simulados. La futura pasarela será un adaptador; usar tokens ficticios, nunca trasladar CVV histórico a la nueva base.

**D-02 — Lealtad por gasto acumulado y metas adicionales por nivel.** Del nivel 0 al 5 se requieren $1,000; del 5 al 10 otros $1,200; del 10 al 15 otros $1,350. Los umbrales acumulados son $1,000, $2,200 y $3,550. La implementación actual compara una sola compra y debe reemplazarse.

## Propuestas de diseño, pendientes de validar antes de su implementación

- **D-03 — Remanentes y puntos:** conservar gasto excedente y permitir varios ascensos con una compra. Calcular los puntos con el nivel al inicio de la compra; documentar si se prefiere dividirla por umbrales. El texto no define cómo convertir porcentajes de compra a puntos; mantener provisionalmente la fórmula histórica y mostrar ejemplos antes de programarla.
- **D-04 — Nivel 15:** acumular $500 adicionales por regalo; sortear solo productos elegibles con stock. Definir exclusiones, efectos de devolución y si el gasto que cruza el nivel 15 cuenta para el primer regalo.
- **D-05 — Funciones:** asignar fechas de demostración explícitas, validar cruces de medianoche y solapamientos por sala. El PDF solo proporciona horas y menciona días diferentes.
- **D-06 — Personal de simulación:** conservar los 24 usuarios exigidos (12 clientes, 8 empleados, 4 gerentes). Resolver cómo atender cinco cajas por sucursal con los dos empleados iniciales; no inventar que existen cinco cajeros en el requisito.
- **D-07 — Erratas:** el texto dice tres sucursales pero enumera cuatro; tomar cuatro. El stock de nachos Xochimilco aparece como `´210`; propuesta: 210. El peluche está en categoría Galletas; propuesta: Mercancía. Registrar aceptación antes de generar el seed definitivo.
- **D-08 — Identidad:** respetar ID de socio e ID de empleado para inicio de sesión; correo adicional opcional. Acceso de administrador definido por permisos, no por una cuenta compartida codificada.
- **D-09 — Multitenancy:** una cadena es un tenant; sucursales pertenecen a esa cadena. PDB por cadena para la demo, sujeta a verificar capacidad real de la edición Oracle elegida.
- **D-10 — Interfaz:** cartelera pública y carrito consultable; compra, historial, puntos y actualización de perfil requieren sesión. El menú del PDF mezcla acciones de visitantes y usuarios autenticados.

## Adaptaciones explícitas

- Oracle sustituirá archivos TXT como almacenamiento principal. Los nombres de tickets y exportaciones del PDF se conservarán donde aplique.
- Una solicitud fiscal simulada no equivale a una factura fiscal emitida.
- Las contraseñas de demo serán ficticias; en la aplicación nueva se almacenarán hashes. No migrar credenciales en texto plano sin un procedimiento de restablecimiento.
- Documentación, manuales y UML se mantienen en el alcance. Video, presentación, ZIP y fechas académicas se registran como entregables históricos; su preparación de portafolio se planifica para el cierre y no implica publicar ni contactar a nadie.

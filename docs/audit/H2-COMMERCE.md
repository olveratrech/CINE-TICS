# H2 — Primera entrega comercial

Fecha: 2026-09-28. H2 continúa en curso; esta entrega aborda BUG-01, BUG-02 y BUG-04 dentro de la consola existente.

## Cambios

- `CarritoProductos` y `LineaCarrito` separan cantidad comprada del stock de catálogo. Líneas inmutables, un producto por código, validación de cantidad positiva y stock acumulado.
- Totales y autorización con BigDecimal. Precios expresados en centavos: se rechaza precisión extra en lugar de redondearla silenciosamente. El legado sigue exponiendo double en sus adaptadores; no se afirma una migración monetaria completa.
- Carrito de consola delega reglas al dominio; los escritores TXT reciben copias de compatibilidad. Mostrar carrito, generar ticket y autorizar alimentos usan el mismo total.
- Existe operación de cambio de cantidad en dominio/adaptador. Su opción en el menú todavía no está conectada; R32 sigue parcial.
- `CompraSimulada` autoriza antes de invocar el registro. Cuenta inactiva, saldo insuficiente e importe no positivo/inválido no ejecutan la confirmación.
- Los dos flujos públicos de consola registran ventas y puntos únicamente tras autorización. Se mantiene la opción explícita del cliente de guardar o eliminar carrito tras rechazo.

## Evidencia

- Cuatro regresiones del carrito fallaron sobre la implementación anterior: dos unidades de $65 daban $65; cantidades cero/negativas se aceptaban; agregar repetidamente excedía el stock.
- Pruebas de carrito decimal, edición, acumulación, precisión y límites de cantidad.
- Pruebas de autorización aprobada/rechazada, saldo exacto y propagación de excepción del adaptador.
- Cuatro pruebas de consola en procesos separados y carpetas temporales: alimentos/boletos × aprobado/rechazado. Verifican saldo persistido, total del carrito, puntos y presencia/ausencia de ventas. Las aprobadas producen un ticket de $130 y dejan saldo $70 partiendo de $200.
- Todos los usuarios, cuentas y productos de prueba son ficticios. Los procesos de compra nunca se ejecutan sobre los datos locales originales.

## Límites que siguen abiertos

- Los escritores históricos capturan errores de I/O y las múltiples escrituras TXT no son atómicas. No existe todavía rollback durable; una aprobación no garantiza recuperación segura ante fallo de disco. La excepción propagada por el caso de uso solo ayuda si el adaptador la comunica.
- No hay idempotencia durable ni defensa frente a compras concurrentes. La invocación única del callback en un test no equivale a protección de reintentos.
- Stock del carrito es una comprobación al agregar/editar, no una reserva. Falta descontar/revalidar inventario atómicamente al confirmar.
- Disponibilidad por función, expiración de reservas, conversión completa a dinero decimal y lealtad acumulada siguen pendientes.
- La consola mantiene el almacenamiento histórico de datos de tarjeta y contraseñas: usar exclusivamente datos ficticios. La sustitución por tokens simulados y hashes pertenece a la migración de identidad/persistencia.
- Los tickets históricos solo reconocen las cuatro sucursales originales para su cabecera; nuevas cadenas requieren reemplazar esa dependencia de nombres.

## Siguiente corte

Extraer compra con identificador y estados persistibles; diseñar el contrato de repositorio transaccional, eliminar efectos parciales y definir la recuperación. Continuar después con reservas por función y la política de lealtad acordada. No marcar H2 completo hasta cerrar sus criterios restantes.

## Resultado final de verificación

`clean verify`: 28 pruebas, 0 fallos, 0 errores, 0 omitidas. Arranque/salida de consola verificado. Sobre la etiqueta original, las mismas cuatro pruebas de compra detectan tres fallos; con esta entrega pasan las cuatro. Se contrastaron los SHA-256 de 105 archivos históricos de datos contra el respaldo: permanecen sin cambios.

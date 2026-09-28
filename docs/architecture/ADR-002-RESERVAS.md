# ADR-002 — Reservas temporales por función

Fecha: 2026-09-28. Alcance: R02/R03 y reserva de asiento por función de H2. Implementado como corte de desarrollo; no sustituye la venta de boletos del legado.

## Modelo y garantías

Una sala pertenece a una sucursal y tiene capacidad. Cada función tiene inicio y fin con zona/offset; conserva su propio conjunto de asientos numerados, creado en la misma transacción. Cambiar la capacidad de una sala no cambia las funciones existentes. La demo usa una sala de 12 lugares; no pretende representar las 12 salas académicas.

Cada asiento de función tiene una sola referencia a reserva, protegida por clave primaria `(screening_id, seat_number)`. La referencia compuesta obliga a que reserva y asiento correspondan a la misma función. La selección histórica permanece en la reserva aunque sus asientos se reasignen al vencer.

El adaptador bloquea la fila de la función antes de comprobar o modificar reservas. Esto serializa todas las reservas/cancelaciones de esa función y evita sobreasignación. Es una decisión inicial simple; no se ha medido carga. Reservas de otras funciones pueden avanzar en paralelo. La consulta de disponibilidad es informativa: la confirmación siempre vuelve a comprobar dentro de la transacción.

La programación bloquea la sala antes de comprobar solapamientos; intervalos contiguos son válidos y los cruces de medianoche se comparan por instantes completos. Función y asientos se insertan juntos. SQL administrativo directo puede saltarse esta regla: los escritores de aplicación deben usar el adaptador. No hay reprogramación pública todavía.

## Tiempo y reintentos

- Plazo inicial de desarrollo: diez minutos, acotados por el inicio de la función. Máximo diez asientos por solicitud. Son valores elegidos para la demo, no requisitos atribuidos al PDF.
- La hora autoritativa es `SYSTIMESTAMP`, consultada después de adquirir el bloqueo. Los clientes no envían vencimientos.
- No hace falta un proceso de limpieza para liberar disponibilidad: una reserva con `expires_at <= SYSTIMESTAMP` ya no bloquea. La referencia anterior puede reemplazarse al reservar otra vez.
- Clave única por cliente dentro de cada PDB. Repetirla con los mismos asientos ordenados y la misma función recupera ID y vencimiento, sin extenderlo. El estado puede pasar a VENCIDA o CANCELADA.
- Reutilizarla con otra selección/función se rechaza. Un rechazo sin reserva no consume la clave; un fallo SQL revierte la operación completa.
- Cancelar solo busca la reserva del cliente indicado. Nunca vacía punteros a asientos: podrían pertenecer ya a una reserva posterior.

## Límites

Cliente DEMO y monederos ficticios como identidad provisional. El adaptador recibe identidad confiable; antes de exponer HTTP hace falta autenticación y derivar cliente/cadena desde la sesión, no desde datos manipulables de la petición. Cada adaptador se configura para una PDB.

No se emiten boletos, no se cobra por reservar ni se convierte todavía una reserva en venta. El próximo corte debe confirmar reserva vigente, pago y boleto en una sola transacción; un asiento vendido ya no podrá liberarse por vencimiento. No se han implementado limpieza histórica, cambios de sala, cancelación de funciones, horarios académicos completos ni pools web.

## Continuación implementada

[ADR-003](ADR-003-VENTA-BOLETOS.md) implementa la confirmación de reserva con pago simulado y boletos. El estado CONFIRMADA y los boletos vendidos impiden liberar asientos por vencimiento. Los límites anteriores describen el corte V003 original.

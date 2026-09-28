# ADR-003 — Confirmar reserva, pago simulado y boletos

Fecha: 2026-09-28. Implementación de R09 y continuación de ADR-002.

## Transacción

VentaBoletos es el contrato de aplicación; BoletosOracle implementa persistencia. La solicitud identifica cliente, clave de reserva y tarifa por asiento. Los precios se calculan en el servidor: ADULTO 80.00 MXN, NINO 50.00 MXN. Debe incluir exactamente los asientos de la reserva, sin repetidos; el cliente no envía importes.

El adaptador localiza la reserva del cliente, bloquea su función y luego su monedero. Tras adquirir ambos bloqueos comprueba con SYSTIMESTAMP que la reserva no haya vencido, no esté cancelada y la función no haya comenzado. Comprueba además que los asientos sigan asignados a esa reserva y que la cuenta esté activa y tenga saldo suficiente.

Venta, débito, pago simulado y boletos se confirman en una transacción. Una excepción revierte todos los cambios. La reserva permanece activa tras un rechazo por saldo insuficiente mientras no alcance su vencimiento; no se persiste una venta fallida. Esta política permite reintentar tras corregir el saldo. Difiere de los rechazos persistidos del adaptador previo de productos.

La clave de la reserva identifica también el reintento de pago. V004 impone una sola venta por reserva y un solo boleto por `(screening_id, seat_number)`. La selección de tarifas ordenada se conserva: repetirla devuelve la venta y boletos originales aunque la reserva ya haya superado su plazo; cambiarla después de una venta se rechaza. Los precios históricos están guardados en cada boleto.

## Concurrencia y estados

Pago, reserva y cancelación adquieren el mismo bloqueo de función. El bloqueo del monedero impide sobregiro incluso al pagar funciones diferentes en paralelo. Los adaptadores de compras de productos también bloquean ese monedero.

La existencia de una venta convierte el estado consultado de reserva en CONFIRMADA. La disponibilidad y la asignación comprueban boletos vendidos además de reservas vigentes: el vencimiento no libera un asiento pagado. Cancelar una reserva confirmada se rechaza; no representa un reembolso.

La validez se evalúa después de esperar los bloqueos. Una reserva válida en ese momento puede finalizar su compra bajo esos bloqueos; no se vuelve a abrir la asignación durante la escritura. No hay llamada a una pasarela externa dentro de esta transacción.

## Límites de este corte

Monedero, pagos y boletos son de demostración. El historial devuelve únicamente boletos del cliente indicado, pero todavía no existe autenticación HTTP: el futuro backend deberá obtener cliente y PDB desde la sesión autenticada. No exponer parámetros de identidad del adaptador directamente al navegador.

La compra de productos y la venta de boletos son operaciones separadas. Quedan pendientes compra combinada, descuentos/lealtad, reembolsos, factura simulada, exportación de comprobantes con el formato académico, control de acceso y web. Tampoco se considera completo el seed de salas y horarios del PDF.

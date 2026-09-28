# H4 — Primer backend HTTP autenticado

Fecha: 2026-09-28. Frontend gráfico todavía pendiente.

## Entregado

- Spring Boot 4.1.1, Java 25, JAR web ejecutable y consola histórica preservada.
- V005 aplicada/validada en CINE_TICS y CADENA_DEMO; registro y monedero ficticio en una transacción.
- BCrypt coste 12; registro público CLIENTE, provisión local de EMPLEADO/ADMIN sin contraseña predeterminada.
- Sesiones con rotación al login, logout, CSRF, cookie HttpOnly/SameSite, límite de intentos y comprobación de cuenta/rol en cada petición.
- Pools Hikari separados, máximo cuatro conexiones por cadena. Identidad y PDB derivadas de la sesión; campos JSON extra rechazados.
- API para cartelera/disponibilidad, reserva/cancelación, pago/boletos, compra de productos, perfil, resumen de personal y programación administrativa.
- Errores HTTP sin contraseñas, consultas SQL ni stack traces en respuestas.

## Evidencia

71 pruebas aprobadas: 36 sin Oracle y 35 con Oracle. Dentro de estas últimas, siete pruebas HTTP levantan Spring/Tomcat en puerto temporal y conectan con ambas PDB.

1. Registro real, hash BCrypt verificable, login incorrecto/correcto, rotación del ID de sesión, cookie HttpOnly y logout.
2. Rechazo de POST sin CSRF; campos de cliente/cadena/rol introducidos en JSON rechazados. Visitante sin permisos comerciales.
3. Reserva → pago → reintento → historial: un cobro de 130 MXN, dos boletos; otro cliente no puede cancelar/pagar la reserva ni leer los boletos.
4. Credenciales de una cadena rechazadas en la otra. Encabezado X-Tenant no cambia el destino de una reserva; asientos con mismos IDs permanecen independientes.
5. Cliente sin acceso administrativo; empleado puede consultar resumen pero no programar; administrador puede programar.
6. Desactivación de monedero invalida una sesión abierta.
7. Registro inválido rechazado; registro duplicado revierte también el monedero.

Las pruebas retiran sus cuentas ficticias y cierran servidor/pools. No generan usuarios de acceso permanente ni modifican registros históricos. El limite de intentos tiene dos pruebas unitarias; las HTTP elevan su cuota para no bloquear sus fixtures.

La compilación limpia sin Oracle aprobó 36 pruebas. El smoke de consola histórica pasó sin escribir archivos. El JAR web arrancó en 127.0.0.1:8080: raíz, cartelera de ambas cadenas y CSRF devolvieron 200; historial anónimo devolvió 401.

Las primeras pruebas detectaron mapeo de parámetros de rutas y autoridades internas de Spring Security; se corrigieron con nombres explícitos y comparación de roles. La suite final pasó completa.

## Límites

API local sin frontend; sesiones en memoria. No desplegada a Internet, no probada bajo carga y sin recuperación de contraseña/verificación de correo. Personal y administración cubren solo las operaciones descritas; el resto de requisitos sigue en la matriz. CI remoto no ejecutado.

Operación y contrato: [API](../api/README.md). Decisiones: [ADR-004](../architecture/ADR-004-API-IDENTIDAD.md).

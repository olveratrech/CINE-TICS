# H3 — Oracle local inicial

Fecha: 2026-09-28. Infraestructura y consola de compras de demostración en ejecución.

## Implementado

- Motor Docker sobre el perfil Colima existente, sin modificar su asignación de recursos.
- Imagen oficial Oracle Free ARM64 `23.26.3.0-lite-arm64`.
- Proyecto Compose `cine-tics`, puerto `127.0.0.1:1522`, volumen `cine-tics_oracle-data`.
- PDB `CINE_TICS` y `CADENA_DEMO`, abiertas y con estado guardado. La PDB original FREEPDB1 se conserva.
- Usuarios locales CINE_OWNER y CINE_APP con contraseñas distintas en cada PDB. La aplicación no posee privilegios de creación de objetos ni roles administrativos.
- Flyway 13.8.0 con historial y checksums por esquema. Migración V001: branches, products e inventory, con claves y restricciones.
- Seed limitado a cuatro sucursales CINE-TICS y una sucursal ficticia de otra cadena. Productos, clientes y ventas históricas no se importaron.

## Verificado sobre Oracle real

1. Aplicación de V001 y validación en las dos PDB.
2. Reejecución de provision, migrate y seed sin recreación ni duplicados; Flyway reporta esquema actualizado.
3. Conexiones de aplicación a la PDB correcta, con el mismo ID de sucursal y nombres distintos por cadena.
4. Rechazo ORA-01017 al usar las credenciales de CINE_TICS en CADENA_DEMO.
5. Rollback de actualización de sucursal y de inserción de producto/inventario; no quedan registros temporales.
6. Rechazo de cantidad negativa por restricción Oracle.
7. Reinicio del contenedor, recuperación del estado healthy y repetición de las comprobaciones de aislamiento y datos.
8. Credenciales excluidas de Git y archivo .env con permisos 0600.

Flyway 11.8.2 avisó inicialmente de versión Oracle no probada. Se actualizó a 13.8.0: migrate/validate terminaron sin ese aviso y sin migraciones pendientes. El controlador JDBC utilizado por las migraciones conectó correctamente con Java 25.

## Pendiente

Integración de los demás flujos comerciales, pools por cadena, autenticación/autorización de usuarios SaaS, importación conciliada y datos de prueba completos. Probar copia de seguridad/restauración; la persistencia tras reinicio no sustituye un respaldo.

La consola histórica conserva su persistencia TXT. La nueva OracleConsole ejecuta compras ficticias contra Oracle. No se modificaron los archivos históricos durante esta entrega. Los tests de integración Oracle son explícitos (`manage.py verify`), no forman parte todavía del workflow Java de GitHub.

## Operación

Ver [instrucciones y conexiones](../../infra/oracle/README.md). El contenedor queda encendido para el siguiente hito; puede detenerse con Compose sin eliminar el volumen.

## Compras transaccionales — segundo corte

- V002 aplicada y validada en ambas PDB: monederos demo, pedidos, líneas y pagos.
- Puerto Compras y adaptador JDBC: precios obtenidos de Oracle; locks en orden de producto; descuento de inventario y saldo, pedido y pago confirmados juntos.
- Clave única por cliente y huella de solicitud: reintento estable aunque cambien precios; contenido distinto con la misma clave se rechaza.
- Fallos técnicos revierten la transacción. Rechazos comerciales conservan el resultado sin cargo.
- 38 pruebas aprobadas: 30 unitarias/caracterización/consola histórica y ocho contra Oracle real. Cubren aprobación/reintento, saldo insuficiente, producto ausente, concurrencia por última unidad, duplicados concurrentes, fallo después del débito, aislamiento entre PDB y cuenta inactiva.
- Prueba manual de CLI con clave demo-validacion-001: dos invocaciones, un pedido de 130.00 MXN, saldo final 870 y stock 8. Datos exclusivamente ficticios.
- Se normalizaron importes recuperados a dos decimales. Los fixtures reutilizan una conexión para preparación y comprobaciones; cada compra sigue usando una conexión independiente. La primera ejecución produjo un ORA-12516 transitorio; la siguiente pasó al reducir la apertura de conexiones del fixture. No se cambiaron límites de Oracle ni recursos de Colima.
- SQLPlus recibe NLS_LANG=.AL32UTF8 para preservar acentos del seed.

No representa todavía una prueba de carga ni la migración completa de los requisitos. El workflow remoto sigue sin ejecutar Oracle; las pruebas de integración se activan explícitamente con manage.py test.

## Reservas temporales — tercer corte

- V003 aplicada y validada por Flyway en ambas PDB: salas, funciones, reservas y asientos por función.
- Adaptador ReservasOracle y comandos de demo: schedule-demo, shows, seats, hold y cancel-hold.
- 50 pruebas aprobadas: 32 sin Oracle, ocho de compras y diez de reservas/programación en Oracle real.
- Escenarios nuevos: reserva completa/reintento sin extensión, vencimiento y reasignación, cancelación del propietario, dos clientes por un asiento, duplicados concurrentes, disponibilidad independiente por función, horarios contiguos y solapados, cuenta inactiva/función iniciada/asiento inexistente, rollback de escritura, aislamiento entre PDB y programación simultánea de horarios incompatibles.
- Recorrido CLI: función ficticia 20260929 a las 19:00 de México; reserva de asientos 1 y 2, reintento con selección invertida, consulta, cancelación y consulta final. Ningún cargo generado.
- Decisiones, garantías y límites: [ADR-002](../architecture/ADR-002-RESERVAS.md).

La venta confirmada de boletos, el seed académico y la integración web siguen pendientes. El SQL administrativo puede eludir las validaciones del adaptador; aún no se expone programación a usuarios externos.

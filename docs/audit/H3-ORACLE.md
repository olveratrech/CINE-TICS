# H3 — Oracle local inicial

Fecha: 2026-09-28. Infraestructura en ejecución; consola aún no conectada.

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

Transacciones de compra, idempotencia, conexión del código de negocio, pools por cadena, autenticación/autorización de usuarios SaaS, importación conciliada y datos de prueba completos. Probar copia de seguridad/restauración; la persistencia tras reinicio no sustituye un respaldo.

No se ejecutó la aplicación de consola contra Oracle ni se cambió su persistencia TXT. No se modificaron los archivos históricos durante esta entrega. Los tests de integración Oracle son explícitos (`manage.py verify`), no forman parte todavía del workflow Java de GitHub.

## Operación

Ver [instrucciones y conexiones](../../infra/oracle/README.md). El contenedor queda encendido para el siguiente hito; puede detenerse con Compose sin eliminar el volumen.

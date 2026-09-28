# Oracle local — primer corte de persistencia

Esta infraestructura no sustituye todavía los archivos usados por la consola. Crea una CDB Oracle Free y dos PDB de aplicación, usuarios, catálogo y compras de demostración. No importar datos personales ni tarjetas en este corte.

## Requisitos

Docker/Compose con motor Linux ARM64 (Mac Apple Silicon), JDK 25, Python 3 y red para descargar Oracle, Maven y dependencias. La imagen fijada es `23.26.3.0-lite-arm64`; para otra arquitectura hay que seleccionar y validar una imagen equivalente.

En esta máquina Colima ya tiene 4 CPU, 8 GiB de memoria y disco de 100 GiB. Se inició el perfil existente sin alterar su configuración.

## Arranque desde la raíz del repositorio

```sh
colima start
python3 infra/oracle/manage.py secrets
docker compose -f infra/oracle/compose.yaml up -d
python3 infra/oracle/manage.py status
```

Esperar a que Oracle esté healthy. La primera inicialización puede tardar varios minutos.

```sh
python3 infra/oracle/manage.py provision
python3 infra/oracle/manage.py migrate
python3 infra/oracle/manage.py seed
python3 infra/oracle/manage.py verify
```

`secrets` no sobrescribe credenciales existentes. `provision` no borra ni recrea PDB ni cambia contraseñas de usuarios existentes. `migrate` ejecuta y valida Flyway por separado en cada PDB. `seed` añade sucursales ficticias solo si no existen. No es el seed completo del documento académico.

No ejecutar el despliegue de migraciones en paralelo. Si hay un fallo DDL, detenerse y revisar el historial de Flyway y el esquema; Oracle no garantiza rollback de DDL. No usar repair/clean automáticamente.

## Conexiones

| Cadena | Servicio | URL JDBC |
|---|---|---|
| CINE-TICS | CINE_TICS | `jdbc:oracle:thin:@//127.0.0.1:1522/CINE_TICS` |
| Cadena de demostración | CADENA_DEMO | `jdbc:oracle:thin:@//127.0.0.1:1522/CADENA_DEMO` |

- Host: `127.0.0.1`; puerto: `1522` (no 1521 del host).
- Usuario de aplicación: `CINE_APP`, con contraseña distinta por cadena.
- Usuario de migraciones: `CINE_OWNER`, con CREATE SESSION/TABLE/SEQUENCE y cuota limitada.
- Las contraseñas se guardan en `.env` de esta carpeta, modo 0600, excluido de Git. Nunca usar `docker compose config` sin cuidado: puede mostrar variables secretas.
- La aplicación consulta `CINE_OWNER.branches`, `CINE_OWNER.products` y `CINE_OWNER.inventory`.
- No usar SYS, SYSTEM ni PDB_ADMIN en el backend.

## Aislamiento y ciclo de vida

Ambas PDB tienen los mismos nombres de esquema y claves de sucursal; el ID 1 identifica CU en una y Sucursal Demo en la otra. La verificación conecta con cada usuario local y comprueba nombre de PDB, datos distintos, rollback y credenciales de la otra cadena rechazadas. Esto verifica aislamiento de base de datos; la API añade identidad por cadena y autorización HTTP, verificadas en WebOracleIT.

Oracle mantiene su PDB predeterminada FREEPDB1 además de las dos PDB de aplicación. No se elimina ni se usa para almacenar datos del proyecto.

```sh
# Detener sin eliminar datos
docker compose -f infra/oracle/compose.yaml stop
# Volver a arrancar
docker compose -f infra/oracle/compose.yaml start
# Ver estado
python3 infra/oracle/manage.py status
```

Los datos viven en el volumen nombrado del proyecto Compose. Un volumen persistente no es un respaldo. No ejecutar `down -v`, borrar el volumen ni borrar el perfil Colima: eso elimina los datos de desarrollo. Exportación y recuperación verificadas con Oracle quedan pendientes del siguiente corte de operación.

## Alcance de la primera migración

`V001__catalog.sql`: sucursales, productos y existencias por sucursal, claves únicas, referencias y restricciones de importes/stock no negativos. La aplicación puede consultar/modificar estos datos pero no crear tablas. Flyway conserva historial y checksums en cada esquema.

`V002__transactional_purchases.sql`: monederos ficticios, pedidos con clave idempotente por cliente, líneas con precio histórico y pagos simulados. El adaptador JDBC confirma pedido, pago, saldo e inventario en una transacción. Los rechazos se conservan sin descontar saldo o existencias; repetir una clave rechazada recupera ese rechazo.

## Demostración y pruebas

Desde la raíz, con JDK 25 activo y ambas migraciones aplicadas:

```sh
python3 infra/oracle/manage.py demo_seed
python3 infra/oracle/console.py catalog
python3 infra/oracle/console.py buy mi-compra-001 9001 2
python3 infra/oracle/console.py buy mi-compra-001 9001 2
python3 infra/oracle/console.py history
python3 infra/oracle/manage.py test
```

En una instalación nueva, el seed crea el monedero DEMO con 1000 MXN y diez unidades del producto 9001 a 65 MXN. No repone saldo ni stock al reejecutarse. La primera compra descuenta 130 MXN y dos unidades; la segunda devuelve el mismo pedido. Otra compra requiere otra clave. Reutilizar una clave con contenido distinto produce un error.

Las pruebas Oracle generan fixtures ficticias exclusivas y las eliminan al finalizar. El perfil `oracle-it` incluye las pruebas de compras y reservas, además de las pruebas que no necesitan base de datos. Una interrupción del proceso puede dejar fixtures con prefijo IT_; revisar sus identificadores antes de retirarlos. La ejecución habitual `./mvnw verify` no requiere Oracle.

La consola demo fija cliente DEMO y sucursal CU; no implementa autenticación ni factura fiscal. Usa conexiones JDBC por operación. La API web usa pools limitados por cadena y sesiones autenticadas; sigue ligada a localhost. Compra combinada, importación del legado, lealtad y facturación continúan pendientes.

## Referencias

- Imagen oficial: https://container-registry.oracle.com/ (repositorio database/free).
- Scripts y documentación de Oracle: https://github.com/oracle/docker-images/tree/main/OracleDatabase/SingleInstance

## Funciones y reservas temporales

Aplicar `manage.py migrate` para instalar V003 en ambas PDB y ejecutar `manage.py demo_seed` para añadir una sala ficticia de doce asientos. El seed conserva el saldo y stock existentes.

Ejemplo con fecha explícita (elegir una fecha futura en formato AAAAMMDD):

```sh
python3 infra/oracle/console.py schedule-demo 20260929
python3 infra/oracle/console.py shows
python3 infra/oracle/console.py seats 20260929
python3 infra/oracle/console.py hold mi-reserva-001 20260929 1 2
python3 infra/oracle/console.py hold mi-reserva-001 20260929 2 1
python3 infra/oracle/console.py cancel-hold mi-reserva-001
python3 infra/oracle/console.py seats 20260929
```

`schedule-demo` crea la función a las 19:00–21:00 de America/Mexico_City; su ID es la fecha. Repetir una fecha existente produce un error y no la modifica. Los doce asientos son numerados del 1 al 12.

Las reservas duran como máximo diez minutos y nunca superan el inicio de la función. Repetir la misma clave recupera el resultado sin renovar el tiempo. Una reserva vencida o cancelada requiere una nueva clave para reservar nuevamente. El vencimiento libera disponibilidad sin tareas programadas. Consultar asientos no garantiza disponibilidad posterior: reservar vuelve a comprobarla transaccionalmente.

Reservar no genera un cargo. El comando `pay-hold` confirma el pago y emite boletos como se describe abajo. La compra de productos `buy` sigue siendo independiente.

`manage.py test` ejecuta actualmente 71 pruebas: 36 sin Oracle y 35 de integración (incluidas siete HTTP con servidor real). Véase [decisiones y límites de reservas](../../docs/architecture/ADR-002-RESERVAS.md).


## Pagar una reserva y consultar boletos

Aplicar `manage.py migrate` para instalar las migraciones hasta V005 en ambas PDB. Usar una función futura existente y asientos disponibles. Ejemplo con una clave nueva:

```sh
python3 infra/oracle/console.py hold mi-compra-boletos-001 20260929 5 6
python3 infra/oracle/console.py pay-hold mi-compra-boletos-001 5 ADULTO 6 NINO
python3 infra/oracle/console.py pay-hold mi-compra-boletos-001 6 NINO 5 ADULTO
python3 infra/oracle/console.py tickets
python3 infra/oracle/console.py seats 20260929
```

Los precios son 80 MXN adulto y 50 MXN niño; solo se aceptan ADULTO/NINO. Incluir todos los asientos de la reserva. El segundo pago devuelve los mismos boletos sin cargo adicional, independientemente del orden de selección. Una tarifa distinta después del pago se rechaza.

Los asientos pagados permanecen ocupados al vencer la reserva. `cancel-hold` no cancela boletos pagados ni devuelve dinero. Una reserva vencida/cancelada no puede pagarse; se necesita reservar otra vez con una clave nueva. Saldo insuficiente o cuenta inactiva no emiten boletos ni debitan saldo. Un error técnico permite reintentar con la misma reserva: si la operación anterior sí se confirmó, se recupera su resultado.

V004 guarda ventas, pagos simulados y un UUID por boleto, con precio histórico y restricción única de asiento por función. El historial es exclusivamente de la cuenta ficticia DEMO. No incluye factura ni archivo de comprobante exportado. Véase [ADR-003](../../docs/architecture/ADR-003-VENTA-BOLETOS.md).

## Identidad web y API

V005 incorpora usuarios web por cadena con hash de contraseña y roles. La API usa conexiones CINE_APP del pool correspondiente a la identidad autenticada. Arranque: `python3 infra/oracle/web.py serve`. Registro, CSRF, login y endpoints: [guía de la API](../../docs/api/README.md). Las consolas demo siguen siendo herramientas locales con identidad DEMO fija.

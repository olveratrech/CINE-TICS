# CINE-TICS

Modernización de un proyecto académico de programación orientada a objetos: de consola y archivos locales a una aplicación web de gestión de cines con Oracle Multitenant.

**Estado:** base histórica preservada; primera refactorización de carrito y autorización de compra implementada. Oracle local dispone de dos PDB y una consola de demostración con compras transaccionales e idempotentes. La consola histórica aún usa archivos; la interfaz web sigue pendiente. La consola conserva defectos conocidos: no usarla para operaciones reales.

## Empezar

Requisitos: JDK 25, Python 3 para la prueba de arranque y acceso a Internet en la primera compilación. Maven Wrapper descarga Maven 3.9.16; no hace falta instalar Maven por separado.

```sh
cd Cine
./mvnw -B -ntp clean verify
cd ..
python3 scripts/smoke_console.py
```

En Windows usar `mvnw.cmd`. Comprobar `java -version` y `./mvnw -version`: ambos deben utilizar Java 25.

En este Mac, si una terminal antigua sigue usando otro JDK:

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

## Ejecutar la consola existente

Abrir **la carpeta raíz CINE-TICS** en VS Code y elegir `Cine-TICS — consola histórica` con F5. La configuración fija el directorio de trabajo en `Cine`.

También se puede ejecutar desde terminal:

```sh
cd Cine
./mvnw compile exec:exec
```

La prueba de arranque funciona sin datos históricos. Las funciones completas de la consola requieren los archivos locales originales dentro de `Cine`; esos archivos se excluyen de Git. Un clon nuevo puede compilar, ejecutar las pruebas y abrir/salir del menú, La demo Oracle se prepara por separado con los comandos siguientes.

## Probar compras con Oracle

Después de preparar Oracle según [su guía](infra/oracle/README.md):

```sh
python3 infra/oracle/manage.py demo_seed
python3 infra/oracle/console.py catalog
python3 infra/oracle/console.py buy mi-compra-001 9001 2
python3 infra/oracle/console.py history
```

Repetir la misma compra con la misma clave recupera el resultado sin cobrar otra vez. Esta consola usa una cuenta ficticia fija y no requiere datos históricos.

## Guía del proyecto

- [Oracle local: arranque y conexiones](infra/oracle/README.md).
- [Verificación de Oracle y compras](docs/audit/H3-ORACLE.md).
- [Roadmap y criterios de salida](docs/ROADMAP.md).
- [Matriz de requisitos](docs/requirements/MATRIX.md) y [CSV editable](docs/requirements/matrix.csv).
- [Decisiones de producto](docs/requirements/DECISIONS.md).
- [Auditoría y defectos conocidos](docs/audit/BASELINE.md).
- [Primera entrega comercial y límites pendientes](docs/audit/H2-COMMERCE.md).
- [Resultados de verificación](docs/audit/VERIFICATION.md).
- [Inventario de código](docs/audit/INVENTORY.md).
- [Arquitectura objetivo](docs/architecture/ADR-001.md).
- [Convenciones y proceso de cambios](CONTRIBUTING.md).
- [Preservación y recuperación](docs/RECOVERY.md).

## Estructura actual

```text
Cine/                   Aplicación de consola y Maven Wrapper
  src/main/java/        Código histórico preservado
  src/test/java/        Pruebas de caracterización
  .mvn/                 Versión de Maven fijada
docs/                   Requisitos, auditoría, decisiones y roadmap
scripts/                Verificación e inventario
.github/workflows/      Verificación propuesta para GitHub Actions
.local-backups/         Respaldo privado, excluido de Git
```

`infra/oracle` contiene Compose, migraciones y verificación. Los directorios futuros `backend` y `frontend` se crearán al implementar sus componentes. No son funcionalidades disponibles todavía.

## Verificación y límites

Las pruebas actuales cubren reglas puntuales de la consola. No prueban aislamiento multitenant, pagos, compras completas ni concurrencia. El workflow local está preparado para GitHub, pero no hay remoto ni ejecución de CI en la nube todavía.

Nunca incorporar datos personales históricos, tarjetas, contraseñas, respaldos o `.env` al repositorio. La primera versión nueva utilizará pagos y facturación simulados, elegidos por el propietario del proyecto.

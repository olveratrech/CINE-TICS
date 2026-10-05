# CINE-TICS

CINE-TICS nació como un proyecto final de Programación Orientada a Objetos. La
primera versión estaba hecha en Java, funcionaba desde consola y guardaba la
información en archivos de texto. Después decidí retomarlo para corregir varios
problemas de la versión original y convertirlo poco a poco en una aplicación
web más completa.

Actualmente el proyecto conserva la consola original como referencia, pero
también cuenta con una interfaz web para clientes, una API en Spring Boot y una
base de datos Oracle ejecutándose en Docker.

## ¿Qué se puede hacer?

Desde la página web un usuario puede:

- Consultar las funciones disponibles de cada cadena de cine.
- Registrarse e iniciar sesión con su ID de socio.
- Consultar los asientos de una función.
- Reservar hasta diez asientos durante diez minutos.
- Elegir tarifa de adulto o niño.
- Realizar un pago de demostración.
- Consultar los boletos que ha comprado.

También existen operaciones de API para compras de productos, consulta de
ventas por empleados y programación de funciones por administradores.

Los pagos y saldos son simulados. El proyecto no realiza cobros reales.

## Tecnologías utilizadas

- Java 25
- Spring Boot 4.1.1
- Spring Security
- Maven
- React y TypeScript
- Vite y Vitest
- Oracle Database Free
- Flyway para las migraciones de la base de datos
- Docker Compose
- JUnit 5

## Organización del proyecto

```text
Cine/                   Código Java, consola original, API y pruebas
frontend/               Interfaz web hecha con React
infra/oracle/           Docker, migraciones y herramientas para Oracle
docs/                   Requisitos, decisiones y notas de desarrollo
scripts/                Scripts de apoyo y comprobación
.github/workflows/      Configuración de pruebas para GitHub Actions
```

La base de datos utiliza dos PDB para representar dos cadenas independientes:
`CINE_TICS` y `CADENA_DEMO`. Los usuarios, reservas, ventas y boletos de una
cadena permanecen separados de los de la otra.

## Requisitos para ejecutarlo

- JDK 25
- Python 3
- Node.js 22.12 o posterior
- Docker y Docker Compose

El proyecto incluye Maven Wrapper, por lo que no es necesario instalar Maven
por separado.

Para revisar las versiones instaladas:

```sh
java -version
node --version
docker --version
```

En este Mac, si la terminal todavía utiliza otro JDK, se puede seleccionar Java
25 con:

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

## Preparar Oracle por primera vez

Desde la carpeta raíz del proyecto:

```sh
python3 infra/oracle/manage.py secrets
docker compose -f infra/oracle/compose.yaml up -d
python3 infra/oracle/manage.py status
python3 infra/oracle/manage.py provision
python3 infra/oracle/manage.py migrate
python3 infra/oracle/manage.py seed
python3 infra/oracle/manage.py demo_seed
```

El primer arranque de Oracle puede tardar varios minutos. El comando `status`
permite comprobar cuándo el contenedor ya está listo.

## Ejecutar la aplicación web

Con Oracle iniciado:

```sh
python3 infra/oracle/web.py serve
```

Después se puede abrir:

<http://127.0.0.1:8080/web/index.html>

Este comando compila el frontend, lo incluye dentro de la aplicación Spring
Boot e inicia el servidor. Para detenerlo se utiliza `Ctrl+C`; la base de datos
permanece encendida.

## Ejecutar la consola original

La consola que formaba parte del proyecto académico todavía se puede ejecutar
desde VS Code con la configuración `CINE-TICS — consola histórica`, o desde la
terminal:

```sh
cd Cine
./mvnw compile exec:exec
```

En Windows se utiliza `mvnw.cmd`.

## Ejecutar las pruebas

Las pruebas que no necesitan Oracle se ejecutan con:

```sh
cd Cine
./mvnw -B -ntp clean verify
```

Para ejecutar también las pruebas de React y las pruebas de integración con
Oracle:

```sh
python3 infra/oracle/manage.py test
```

Estas pruebas revisan, entre otras cosas, el registro e inicio de sesión, los
permisos, las reservas, los pagos, los reintentos, el aislamiento entre cadenas
y la compra simultánea de asientos.

## Documentación adicional

- [Uso de la interfaz web](frontend/README.md)
- [Configuración de Oracle](infra/oracle/README.md)
- [Endpoints de la API](docs/api/README.md)
- [Roadmap del proyecto](docs/ROADMAP.md)
- [Matriz de requisitos](docs/requirements/MATRIX.md)
- [Decisiones de arquitectura](docs/architecture/ADR-001.md)

Los archivos `.env`, respaldos, datos históricos de clientes y archivos
generados durante la compilación están excluidos del repositorio. Las
credenciales y los datos personales no deben agregarse a GitHub.

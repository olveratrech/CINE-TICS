# Convenciones de desarrollo

## Cambios pequeños y verificables

1. Asociar el cambio a un ID de la matriz de requisitos o de defectos.
2. Añadir una prueba del comportamiento relevante antes de corregirlo.
3. Compilar con `cd Cine && ./mvnw -B -ntp clean verify`.
4. Desde la raíz ejecutar `python3 scripts/smoke_console.py`.
5. Actualizar evidencia y documentación, revisar `git diff --check` y los archivos preparados para commit.

No marcar un requisito como verificado por encontrar una clase o un botón. Registrar el escenario ejecutado y su resultado.

## Java

- JDK 25, UTF-8, cuatro espacios; paquetes nuevos en minúsculas bajo `com.penta.cinetics`.
- Agrupar por capacidad de negocio. Cada módulo separará dominio, aplicación e infraestructura según su necesidad.
- Una responsabilidad por clase; evitar nuevas clases `Utils` o `MetodosEnGeneral` para lógica de negocio.
- `BigDecimal` para dinero con redondeo explícito; fechas con `java.time` y zona de negocio explícita.
- Reglas de negocio sin `Scanner`, `System.out`, rutas a archivos ni llamadas HTTP.
- Persistencia mediante adaptadores; errores tipados y validaciones en límites de entrada y dominio.
- Preferir composición; introducir patrones cuando resuelvan variaciones reales.
- Conservar nombres del código histórico hasta migrar cada módulo con pruebas. No reformatear todo el legado en un cambio funcional.

## Datos y pruebas

- Fixtures ficticias y deterministas. Ninguna prueba depende de los archivos personales del desarrollador.
- Probar rechazo de pagos, repetición de solicitudes, doble reserva, stock concurrente y aislamiento de cadenas al implementar esos flujos.
- Cada bug corregido debe tener una regresión. No escribir pruebas que afirmen como correcto un defecto conocido.
- No usar H2 como prueba suficiente del comportamiento específico de Oracle.
- Los cambios de esquema serán migraciones versionadas y revisables; no depender de creación automática del ORM en producción.

## Repositorio

La etiqueta `baseline-java25` conserva el código anterior a la modernización. Los commits siguientes deben explicar problema y resultado. No se ha elegido una licencia: no añadir una ni publicar el repositorio sin acordarlo con los autores.

La configuración local de JDK está ignorada. Los archivos de lanzamiento compartidos no deben contener rutas personales.

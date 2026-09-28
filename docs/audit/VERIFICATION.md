# Verificación de H0/H1

Fecha: 2026-09-28. Entorno local: macOS ARM64, OpenJDK 25.0.4.1, Maven Wrapper con Maven 3.9.16.

| Comprobación | Resultado |
|---|---|
| Archivo de respaldo frente al manifiesto SHA-256 | 206 archivos originales de Cine verificados |
| Fuentes de copia de trabajo frente a NetBeansProjects al inicio | Coinciden |
| `./mvnw -B -ntp clean verify` | BUILD SUCCESS; 8 pruebas, 0 fallos, 0 errores, 0 omitidas |
| `python3 scripts/smoke_console.py` | Menú y salida correctos; sin archivos creados |
| `./mvnw -B -ntp exec:exec` con opción 4 | BUILD SUCCESS; salida normal |
| Copia temporal solo de archivos versionables, sin target ni datos históricos | Compila con `./mvnw -o -B -ntp clean verify`; las 8 pruebas y el arranque pasan |
| Exclusión de datos y respaldo | `git check-ignore` confirma exclusión de tarjetas, clientes, ventas y respaldo |
| Documentación y configuración | Enlaces locales válidos; XML y JSON válidos |

La compilación de la copia temporal utilizó dependencias previamente descargadas en la caché Maven. No se ha probado aún Windows ni un runner de GitHub. El workflow está preparado, no ejecutado remotamente.

Permanece el aviso de API obsoleta en MetodosEnGeneral. No se ha cambiado código de producción ni corregido los defectos enumerados en BASELINE.md. El alcance de estas pruebas es una línea base, no una certificación de compras completas.

Oracle, web, pagos, multitenancy y pruebas de concurrencia están pendientes de implementación. La matriz documenta criterios futuros, no resultados ya obtenidos.

## Actualización H3 — compras Oracle

La línea base anterior describe H0/H1. El estado posterior está en [H3-ORACLE.md](H3-ORACLE.md): 38 pruebas aprobadas, ocho contra Oracle real, y recorrido CLI de compra/reintento confirmado. El smoke de consola histórica volvió a pasar sin escribir datos. Web, reservas, importación completa y restauración siguen pendientes.

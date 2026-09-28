# Preservación y recuperación

Fecha: 2026-09-28.

- Carpeta de trabajo: copia `Cine` dentro de este repositorio.
- El código fuente coincidía con `/Users/olveratrech/NetBeansProjects/Cine/src` al comenzar.
- La carpeta original de NetBeans no se modifica en esta etapa.
- Copia completa local: `.local-backups/cine-before-modernization.tar.gz`.
- Manifiesto por archivo: `.local-backups/manifest.json`.
- Se verificó el contenido de los 206 archivos de `Cine` incluidos en el respaldo contra sus SHA-256. También se incluyó la configuración de VS Code del espacio de trabajo.
- SHA-256 del archivo de respaldo: `333b3afa517bc3cebe64115a611bcf0817c5d445a31c53cd4a755c9891929c68`.
- Referencia Git de código: etiqueta `baseline-java25` (sin datos locales).

Para inspeccionar el código antiguo sin sobrescribir el actual:

```sh
git show baseline-java25:Cine/pom.xml
git diff baseline-java25 -- Cine/src/main/java
```

Para recuperar el respaldo completo, extraerlo a una carpeta nueva y comparar antes de reemplazar archivos. El archivo contiene datos locales y debe mantenerse privado. Git no recupera datos ignorados: conservar el respaldo fuera de la máquina antes de cualquier futura eliminación o migración destructiva.

Los directorios de datos históricos permanecen en su ubicación. Todavía no se han renombrado ni borrado módulos.

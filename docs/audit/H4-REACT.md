# H4 — Recorrido visual de cliente

Fecha: 2026-09-28.

## Entregado

React 19.3.0, TypeScript 7.0.2 y Vite 8.3.1, versiones fijadas en package-lock.json. Pantallas en español: cartelera por cadena, registro/login, cuenta, asientos, reserva, tarifas adulto/niño, pago simulado e historial. CSS original sin recursos remotos.

El build se empaqueta como recursos estáticos de Spring Boot bajo /web/. La API y sus controles de autenticación, roles y CSRF se conservan. La aplicación integrada se abre en http://127.0.0.1:8080/web/index.html. El servidor Vite es opcional para desarrollo.

Claves de operación conservadas en sessionStorage por propietario/cadena; reintentos recuperan la misma reserva/pago. No se almacenan contraseñas ni tokens. El historial anterior se vacía antes de cargar uno nuevo y al cambiar de sesión.

## Verificación

- Seis pruebas de React/Testing Library aprobadas; TypeScript y build de producción correctos.
- 72 pruebas Java aprobadas, incluidas ocho HTTP y pruebas Oracle. La prueba nueva valida HTML público y que el historial siga exigiendo sesión.
- Compilación limpia Java sin Oracle: 36 pruebas. Consola histórica: arranque/salida sin escribir datos.
- Navegador real: login con cuenta ficticia, selección de asientos 5 y 6, reserva, tarifa adulta/infantil, compra por 130 MXN y dos UUID de boletos; recuperación del historial tras reabrir, saldo 870 y logout.
- Inspección visual en escritorio 1440px y móvil 390px. Formularios, controles de teclado, asientos deshabilitados e historial legibles.
- La cuenta y las operaciones ficticias del navegador se retiraron por sus identificadores; se liberaron únicamente los asientos de esa prueba. Los boletos demo existentes de los asientos 3 y 4 se conservaron.
- Workflow preparado para instalar, probar y compilar React antes de Java; no ejecutado remotamente.

La prueba del historial detectó y corrigió la permanencia de una lista anterior tras un fallo de red. No se afirma cobertura completa de accesibilidad ni pruebas de carga.

## Pendiente

Personal/administración visual, compra de productos desde la web, compra combinada, lealtad, recuperación de cuenta, verificación de correo, seed académico completo y comprobantes exportables. No desplegado públicamente.

Operación: [frontend](../../frontend/README.md). Contrato: [API](../api/README.md).

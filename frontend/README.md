# CINE-TICS · Interfaz React

React + TypeScript + Vite. Pantallas: cartelera por cadena, registro/login, mapa de asientos, reserva temporal, selección de tarifa, pago simulado e historial privado. Diseño adaptable y navegación por teclado.

## Arranque integrado

Desde la raíz, con Oracle iniciado, migraciones aplicadas, JDK 25 y Node 24 (o versión compatible >=22.12):

```sh
python3 infra/oracle/web.py serve
```

Abrir **http://127.0.0.1:8080/web/index.html**. El comando instala las versiones del lockfile, compila React y lo incluye en el JAR de Spring Boot. Ctrl+C detiene la aplicación; Oracle queda encendido. No se necesita un segundo servidor para usar esta versión.

## Desarrollo con recarga

Dejar la API en 8080 y, en otra terminal:

```sh
cd frontend
npm ci
npm run dev
```

Abrir http://127.0.0.1:5173/web/. Vite envía `/api` a 8080. Cookies y CSRF se manejan en el mismo origen para el navegador; no se habilita CORS indiscriminado. No ejecutar dos instancias con el mismo puerto.

## Pruebas y formato

```sh
npm test
npm run build
npm run format
```

Seis pruebas de interfaz: registro/login con ID devuelto, reintento de reserva, reintento de pago sin cambiar selección, vencimiento/liberación, recuperación de pago ambiguo después del plazo e historial que no muestra datos anteriores si falla una consulta.

Desde la raíz, `python3 infra/oracle/manage.py test` instala/valida/compila React y ejecuta las pruebas Java/Oracle/HTTP. La compilación Java habitual sin perfil Oracle permanece disponible.

## Datos, recuperación y límites

- No se guardan contraseñas ni tokens de sesión en almacenamiento web. La cookie de sesión es HttpOnly.
- `sessionStorage` conserva solo la solicitud de reserva/pago pendiente, sus asientos, función y tarifas, bajo una clave específica de cadena/cliente. Reabrir una pestaña nueva no garantiza recuperar esa solicitud; el historial del servidor conserva los boletos confirmados.
- Ante un fallo ambiguo, se conserva la clave y se ofrece reintentar. No se cambia tarifa después del primer intento de pago hasta conocer un rechazo definitivo. Un reintento de pago puede recuperar una venta confirmada aunque haya vencido el temporizador local.
- La hora mostrada es de Ciudad de México. El temporizador ayuda al usuario; Oracle es la autoridad del vencimiento. Los precios mostrados son informativos y el servidor calcula el cobro.
- Las ilustraciones son CSS original; no representan carteles oficiales de películas. La cartelera se obtiene de Oracle, sin funciones inventadas en el navegador.
- La cuenta permite consultar el ID necesario para futuros logins. No hay recuperación de contraseña todavía.
- Este corte cubre clientes. Paneles de personal, dulcería web, compra combinada, lealtad y factura/exportación de boletos siguen pendientes.

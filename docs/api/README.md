# API local de CINE-TICS

Base: `http://127.0.0.1:8080`. Los endpoints devuelven JSON; la interfaz React está en `/web/index.html`. Todo pago y saldo es ficticio.

## Preparar y arrancar

Desde la raíz, con JDK 25 y Oracle healthy:

```sh
python3 infra/oracle/manage.py migrate
python3 infra/oracle/web.py serve
```

El comando instala y compila React (requiere Node), lee las credenciales locales ignoradas por Git y construye el JAR web e inicia el servidor en primer plano. Ctrl+C lo detiene sin detener Oracle. Para otro puerto: `CINE_WEB_PORT=8081 python3 infra/oracle/web.py serve`. El límite local es de cuatro conexiones por cadena; no cambia la configuración de los demás contenedores.

Consultar en el navegador:

- http://127.0.0.1:8080/
- http://127.0.0.1:8080/api/public/CINE_TICS/shows
- http://127.0.0.1:8080/api/public/CADENA_DEMO/shows

La cartelera lista como máximo 100 funciones futuras; un arreglo vacío significa que hay que programar nuevas funciones. Se conserva la herramienta `console.py schedule-demo AAAAMMDD` para crear funciones ficticias.

## Contrato de autenticación

1. GET `/api/auth/csrf`, conservando la cookie `CINE_SESSION`. Respuesta `{ "headerName": "X-CSRF-TOKEN", "token": "..." }`.
2. Enviar ese encabezado y la cookie en cada POST, también registro/login/logout.
3. Registro: POST `/api/public/CINE_TICS/register` con JSON:

```json
{
  "nombre": "Cliente",
  "apellidos": "De demostración",
  "direccion": "Dirección ficticia",
  "telefono": "5555555555",
  "correo": "cliente@example.test",
  "password": "una contraseña elegida por ti"
}
```

Respuesta 201: `memberId`, `username` (`CINE_TICS:U_...`), `role: CLIENTE` y aviso de saldo ficticio. No inicia sesión automáticamente. El correo es único dentro de cada cadena; el login utiliza el ID de socio devuelto.

4. POST `/api/auth/login`, **application/x-www-form-urlencoded**, con `username` y `password`. Responde 200 o 401; conservar la cookie nueva que rota durante el login.
5. Obtener un token CSRF nuevo después de login. GET `/api/me` devuelve el perfil propio, cadena, roles y saldo de demostración.
6. POST `/api/auth/logout` con CSRF invalida la sesión; obtener otro token si se desea iniciar sesión de nuevo.

Nunca enviar `cliente`, `cadena`, `role` o `saldo` en las acciones privadas: el backend usa la identidad autenticada y rechaza campos JSON desconocidos. CINE_TICS y CADENA_DEMO son cuentas independientes, incluso con el mismo correo.

## Endpoints

| Método y ruta | Acceso | Entrada / resultado |
|---|---|---|
| GET `/api/auth/csrf` | Público | Token de sesión |
| POST `/api/public/{cadena}/register` | Público + CSRF | Registro de cliente, 201 |
| POST `/api/auth/login` | Público + CSRF | Formulario username/password |
| POST `/api/auth/logout` | Sesión + CSRF | Cerrar sesión |
| GET `/api/me` | Sesión | Perfil y saldo propios |
| GET `/api/public/{cadena}/shows` | Público | Funciones futuras, sala/sucursal y fechas con offset |
| GET `/api/public/{cadena}/shows/{funcion}/seats` | Público | Asientos numerados y disponibilidad |
| POST `/api/client/holds` | CLIENTE + CSRF | `{ "funcion": 20260929, "clave": "reserva-001", "asientos": [1,2] }` |
| POST `/api/client/holds/{clave}/cancel` | CLIENTE + CSRF | Sin cuerpo; cancela una reserva propia sin pagar |
| POST `/api/client/holds/{clave}/pay` | CLIENTE + CSRF | Selección de tarifas; venta y boletos |
| GET `/api/client/tickets` | CLIENTE | Solo sus boletos |
| POST `/api/client/purchases` | CLIENTE + CSRF | Compra independiente de productos |
| GET `/api/staff/summary` | EMPLEADO o ADMIN | Ventas de boletos y total ficticio de su cadena |
| POST `/api/admin/shows` | ADMIN + CSRF | Programar función y asientos en sala existente |

Pago de reserva:

```json
{"seleccion":[{"asiento":1,"tipo":"ADULTO"},{"asiento":2,"tipo":"NINO"}]}
```

Precios en servidor: adulto 80, niño 50 MXN. Hay que incluir todos los asientos reservados. Repetir el pago con la misma clave/selección devuelve los mismos boletos sin cobrar otra vez. No enviar el importe. Una reserva confirmada no se puede cancelar mediante este endpoint.

Compra de productos:

```json
{"sucursal":1,"clave":"productos-001","items":[{"producto":9001,"cantidad":2}]}
```

Los resultados comerciales del adaptador de productos se devuelven en `estado`, incluidos los rechazos de saldo/stock; comprobarlo además del código HTTP. Esta compra aún es independiente de los boletos.

Programar función (sala existente):

```json
{"id":20260930,"sala":9101,"titulo":"Película de demostración","inicio":"2026-09-30T19:00:00-06:00","fin":"2026-09-30T21:00:00-06:00"}
```

## Provisionar personal localmente

```sh
python3 infra/oracle/web.py create-user CINE_TICS ADMIN
python3 infra/oracle/web.py create-user CINE_TICS EMPLEADO
```

Solicita perfil y contraseña sin mostrarla. Imprime el identificador para login. No sobrescribe usuarios ni crea un administrador predeterminado. El registro HTTP siempre crea CLIENTE.

## Errores y verificación

- 400: cuerpo, ruta o datos inválidos; campos extra también se rechazan.
- 401: faltan credenciales, son incorrectas o la sesión ya no es válida.
- 403: falta CSRF o permiso.
- 404: recurso privado no encontrado para ese usuario.
- 409: conflicto comercial o registro duplicado.
- 429: límite de intentos de acceso; esperar Retry-After.
- 503: base no disponible. En compras, reintentar con la misma clave para recuperar un resultado ya confirmado.

```sh
python3 infra/oracle/manage.py test
```

Ejecuta pruebas existentes y pruebas HTTP/Oracle con servidor temporal y usuarios ficticios que se limpian. Una interrupción puede dejar fixtures; no ejecutar borrados generales. Las pruebas habituales `Cine/mvnw -f Cine/pom.xml clean verify` no necesitan Oracle.

Sesiones en memoria; se pierden al reiniciar. La API local no incluye recuperación de contraseña, comprobantes exportados ni despliegue público. Ver [decisiones y límites de seguridad](../architecture/ADR-004-API-IDENTIDAD.md).

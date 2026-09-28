# ADR-004 — API, identidad y sesiones por cadena

Fecha: 2026-09-28. Primer corte H4; R04/R05/R06/R09 y aislamiento SaaS.

## Decisiones

Spring Boot 4.1.1 sobre Java 25, Spring MVC y Spring Security. Versiones transitivas administradas por el BOM de Boot; JUnit pasa a la versión gestionada. El JAR original y su main de consola permanecen; un segundo JAR con clasificador `web` inicia el servidor.

Dos pools Hikari independientes, cada uno con un usuario CINE_APP y una URL fija. Máximo cuatro conexiones por PDB, mínimo cero inactivas, espera de conexión cinco segundos. Los adaptadores existentes reciben conexiones del pool; su cierre devuelve la conexión. No se cambia de esquema ni de PDB en una conexión reutilizada.

El registro público elige una cadena de la lista cerrada CINE_TICS/CADENA_DEMO y devuelve un ID de socio generado. La identidad de login es `CADENA:ID_SOCIO`, autenticada contra esa PDB. El registro exige nombre, apellidos, dirección, teléfono, correo y contraseña; guarda correo normalizado único por cadena. Contraseña de al menos ocho caracteres y hasta 72 bytes UTF-8, hash BCrypt coste 12 con sal propia. No se importan contraseñas del legado.

Solo se crean clientes mediante HTTP. La herramienta local `web.py create-user` permite al operador provisionar empleados/administradores con contraseña interactiva. No hay credencial de administrador predeterminada. Todos los monederos nuevos reciben 1000 MXN ficticios para la demo; este comportamiento no sirve como crédito monetario de producción.

Sesiones de servidor, cookie HttpOnly/SameSite=Lax, expiración tras 30 minutos de inactividad y rotación de ID al autenticarse. CSRF queda activo incluso para login, registro y logout; el cliente obtiene un token con GET /api/auth/csrf y debe renovarlo tras login/logout. No hay JWT ni token de autenticación en localStorage.

Las acciones privadas extraen cadena y cliente del principal autenticado. No admiten estos campos, rol ni saldo en JSON. Los encabezados de tenant no cambian el contexto. Cartelera y disponibilidad son públicas por cadena; el acceso público a ellas no da acceso a datos privados de esa cadena.

Cada petición autenticada vuelve a comprobar estado de cuenta y roles en Oracle. Una cuenta desactivada o un cambio de rol invalidan la sesión; hace falta autenticarse otra vez. Se conservan las autoridades internas de Spring Security, comparando únicamente roles para esta comprobación.

Treinta intentos de login/registro por minuto y dirección remota, con registro acotado a 1024 orígenes en memoria; 429 y Retry-After al agotarse. No se confía en X-Forwarded-For. Es un límite local por proceso, no distribuido. Las pruebas HTTP elevan el límite para poder crear sus fixtures; el algoritmo tiene pruebas de ventana y capacidad.

## Permisos del corte

- CLIENTE: su perfil, reservas, cancelación, pago, boletos y compras de productos.
- EMPLEADO: resumen agregado de ventas de boletos de su cadena.
- ADMIN: resumen y programación de funciones en salas existentes de su cadena.
- Cualquier ruta no declarada se deniega.

Los roles no implementan todavía todos los módulos de personal o administración del PDF.

## Verificación y límites

Pruebas HTTP con servidor embebido real y ambas PDB: registro/hash, login incorrecto/correcto, rotación/logout, CSRF, campos extra, roles, compra idempotente, aislamiento entre propietarios y cadenas, desactivación y rollback de registro duplicado. Las fixtures se retiran; no son cuentas de demostración persistentes.

Servidor ligado a 127.0.0.1; HTTP para desarrollo local. Para publicación: TLS, CINE_COOKIE_SECURE=true, límites de solicitudes en el proxy, política de sesiones compartidas si hay múltiples instancias, controles operativos y respaldo/restauración. Las sesiones se pierden al reiniciar. No hay recuperación de contraseña, verificación de correo, MFA ni gestión web de usuarios.

El frontend React sigue pendiente; este corte entrega una API. Los adaptadores comerciales aún comunican fallos SQL al borde HTTP; se traducen a respuestas genéricas sin SQL ni credenciales. Los errores de negocio se traducen a 404/409; validación a 400. No se registran cuerpos de solicitudes ni contraseñas.

## Fuentes oficiales consultadas

- [Requisitos de Spring Boot 4.1.1](https://docs.spring.io/spring-boot/system-requirements.html): compatible con Java 25.
- [Login de formulario](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html).
- [CSRF y sesiones](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).

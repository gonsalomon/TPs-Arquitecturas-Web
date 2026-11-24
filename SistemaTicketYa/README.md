# TicketYa – Sistema de venta de entradas

## Descripción

TicketYa es una plataforma de venta de entradas para distintos tipos de eventos, desarrollada mediante una arquitectura de microservicios con Spring Boot.

El sistema permite gestionar usuarios con autenticación JWT, consultar y buscar eventos, administrar el acceso a eventos de alta demanda mediante fila virtual, generar órdenes de compra, gestionar reservas concurrentes sin doble venta, procesar pagos, emitir entradas con código QR, enviar notificaciones y ofrecer recomendaciones personalizadas.

## Requisitos previos

Para ejecutar el proyecto se necesita:

- Java 21.
- Maven.
- Bases de datos relacionales (MySQL) en funcionamiento para los microservicios SQL.
- MongoDB para la persistencia de eventos y lugares.
- Postman para realizar las pruebas de los endpoints (o el cliente web `index.html` apuntando al Gateway).

## Ejecución del proyecto

Cada microservicio es un proyecto Spring Boot independiente y debe iniciarse por separado.

Todos los servicios (incluido el API Gateway) deben estar levantados antes de comenzar a realizar las peticiones.

### Microservicios y puertos

| Microservicio | Puerto | Rol principal |
|---|---:|---|
| **Gateway** | **8080** | Punto de entrada único, enrutamiento, CORS y autenticación JWT |
| Usuario | 8081 | Gestión de usuarios, roles y preferencias |
| Pago | 8082 | Procesamiento de pagos y reembolsos |
| Evento | 8083 | Lugares, eventos, búsqueda y recomendaciones |
| Reserva | 8084 | Bloqueo temporal de asientos/cupos, concurrencia e idempotencia |
| Entrada | 8085 | Emisión de entradas, validación de QR, historial y co-compras |
| Notificación | 8086 | Envío de notificaciones por confirmación o cancelación |
| Orden | 8087 | Orquestación del flujo de compra, pagos y reportes |
| **Fila Virtual** | **8088** | Encolamiento y control de turnos para eventos de alta demanda |

> Todas las peticiones externas deben enviarse a través del **API Gateway (`http://localhost:8080`)**.

---

## Orden de ejecución de los endpoints

Para probar correctamente el circuito completo de la plataforma es necesario respetar el siguiente orden de ejecución.

### 1. Crear usuarios

Primero se deben registrar los usuarios que participarán en las operaciones del sistema.

Se recomienda crear, como mínimo:
- Un usuario con rol `ADMINISTRADOR`.
- Un usuario con rol `ORGANIZADOR`.
- Un usuario con rol `PERSONAL_DE_ACCESO`.
- Uno o dos usuarios con rol `CLIENTE`.

**Endpoint:**

`POST http://localhost:8080/api/usuarios`

Ejemplo de creación de organizador:

```json
{
  "nombre": "Santiago",
  "apellido": "Sosa",
  "email": "organizador@example.com",
  "ciudad": "Tandil",
  "password": "1234",
  "authorities": ["ORGANIZADOR"],
  "artistasFavoritos": []
}
```

Ejemplo de creación de cliente:

```json
{
  "nombre": "Maria",
  "apellido": "Cabrera",
  "email": "cliente@example.com",
  "ciudad": "Tandil",
  "password": "1234",
  "authorities": ["CLIENTE"],
  "artistasFavoritos": ["Shakira", "Maluma"]
}
```

Es importante conservar los IDs generados, ya que se utilizarán posteriormente en los headers (`X-Usuario-Id`) y parámetros de las peticiones.

### 2. Iniciar sesión y obtener el token JWT (Login)

Antes de operar sobre endpoints protegidos, cada usuario debe autenticarse en el Gateway para obtener su token JWT.

**Endpoint:**

`POST http://localhost:8080/api/authenticate`

**Body:**

```json
{
  "username": "cliente@example.com",
  "password": "1234"
}
```

El token devuelto debe enviarse en las peticiones protegidas mediante el header:

`Authorization: Bearer <TOKEN_JWT>`

### 3. Consultar (o crear y publicar) lugares y eventos disponibles

Se pueden consultar los lugares y eventos existentes, o crear nuevos (con token de `ORGANIZADOR`) y publicarlos cambiando su estado a `PUBLICADO`.

**Endpoints de consulta:**

- Listar eventos: `GET http://localhost:8080/api/eventos`
- Listar lugares: `GET http://localhost:8080/api/eventos/lugares`
- Buscar eventos por filtros: `GET http://localhost:8080/api/eventos/buscar?ciudad=tandil`

**Importante:** los identificadores de los eventos almacenados en MongoDB son cadenas de texto (`String`), diferentes de los IDs numéricos utilizados por los microservicios relacionales. Se debe utilizar el identificador exacto devuelto por la API.

### 4. Ingresar a la Fila Virtual *(solo para eventos de Alta Demanda)*

Si el evento seleccionado tiene `altaDemanda: true`, el cliente debe encolarse en la fila virtual antes de poder crear su orden de compra. Para eventos de baja demanda (`altaDemanda: false`), este paso se omite.

**Ingresar a la fila:**

`POST http://localhost:8080/api/fila/ingresar`

**Headers requeridos:**
- `Authorization: Bearer <TOKEN_CLIENTE>`
- `X-Usuario-Id: ID_DEL_CLIENTE`

**Body:**

```json
{
  "eventoId": "ID_DEL_EVENTO_ALTA_DEMANDA",
  "usuarioId": 1
}
```

**Consultar estado y posición en la fila:**

`GET http://localhost:8080/api/fila/eventos/{eventoId}/estado`

Conservar el token de turno devuelto (`tokenAcceso` / `tokenFila`), ya que será requerido al crear la orden.

### 5. Crear una orden de compra

Una vez seleccionado el evento (y con el token de fila si corresponde), el cliente genera una orden indicando el evento, el sector y los asientos (para sectores `NUMERADO`) o la cantidad (para sectores `NO_NUMERADO`).

**Endpoint:**

`POST http://localhost:8080/api/ordenes`

**Headers requeridos:**
- `Authorization: Bearer <TOKEN_CLIENTE>`
- `X-Usuario-Id: ID_DEL_CLIENTE`

Ejemplo de Body (**sector numerado**):

```json
{
  "eventoId": "ID_DEL_EVENTO",
  "sector": "Platea",
  "asientos": [
    { "fila": 1, "asiento": 5 },
    { "fila": 1, "asiento": 6 }
  ]
}
```

Ejemplo de Body (**sector no numerado**):

```json
{
  "eventoId": "ID_DEL_EVENTO",
  "sector": "Campo",
  "cantidad": 3
}
```

> Si el evento es de **alta demanda**, incluir además el campo `"tokenFila": "<TOKEN_DE_FILA>"` en el body.

La creación de la orden reserva los asientos/cupos en el microservicio `Reserva` y deja la orden lista para el pago. Conservar el ID de la orden generada.

### 6. Realizar el pago de la orden

###### Importante: Para el envío efectivo de correos en este paso son necesarias credenciales válidas de email, o funciona en modo best-effort. Se necesita configurar `application.yml` en `Notificacion` con un mail válido que cuente con 2FA y una contraseña de aplicación (Gmail). Sin `MAIL_USERNAME` ni `MAIL_PASSWORD` válidos, se registra un warning y el flujo continúa normalmente.

Una vez creada la orden, se ejecuta el endpoint de pago correspondiente.

**Endpoint:**

`POST http://localhost:8080/api/ordenes/{ordenId}/pago`

**Headers requeridos:**

| Header | Valor |
|---|---|
| `Authorization` | `Bearer <TOKEN_CLIENTE>` |
| `X-Usuario-Id` | ID del cliente |

**Body:**

```json
{
  "medioPago": "TARJETA_CREDITO"
}
```

Al confirmarse el pago, la orden pasa a estado `CONFIRMADA`, se emite la entrada correspondiente con su código QR y se dispara la notificación de confirmación.

**Importante:** no es necesario crear previamente una entrada de manera manual. El procesamiento del pago de la orden orquesta automáticamente la emisión.

### 7. Consultar órdenes, entradas y validación de QR

- **Consultar órdenes del cliente:** `GET http://localhost:8080/api/ordenes` (con `Authorization` y `X-Usuario-Id`).
- **Consultar entrada por ID:** `GET http://localhost:8080/api/entradas/{entradaId}` (con `Authorization`).
- **Validar QR en acceso (`PERSONAL_DE_ACCESO`):** `PATCH http://localhost:8080/api/entradas/validar` enviando `codigoQr` y `eventoId`.

### 8. Preferencias y Sistema de Recomendaciones

Una vez que existen compras registradas en el sistema, el cliente puede configurar sus preferencias y consultar recomendaciones personalizadas y eventos similares por co-compra:

1. **Registrar preferencias del cliente:**
   `PUT http://localhost:8080/api/usuarios/{clienteId}/preferencias`
   ```json
   {
     "ciudad": "Tandil",
     "tiposEvento": ["TEATRO", "RECITAL"],
     "artistasFavoritos": ["Shakira", "Maluma"]
   }
   ```
2. **Consultar eventos similares ("Quienes compraron este evento también compraron..."):**
   `GET http://localhost:8080/api/eventos/{eventoId}/similares?limit=5`
3. **Consultar recomendaciones personalizadas (historial + preferencias + co-compras, excluyendo eventos ya comprados):**
   `GET http://localhost:8080/api/eventos/recomendaciones?limit=10` (requiere `Authorization: Bearer <TOKEN_CLIENTE>` y header `X-Usuario-Id`).

---

## Endpoints disponibles

Además del circuito principal, los microservicios exponen los siguientes endpoints a través del Gateway (`http://localhost:8080`):

| Microservicio | Método | Endpoint | Descripción |
|---|---|---|---|
| Gateway | POST | `/api/authenticate` | Autenticación de usuario y emisión de token JWT. |
| Usuario | POST | `/api/usuarios` | Registrar un nuevo usuario. |
| Usuario | GET | `/api/usuarios` | Obtener todos los usuarios. |
| Usuario | GET | `/api/usuarios/{id}` | Obtener un usuario por ID. |
| Usuario | PUT | `/api/usuarios/{id}` | Modificar un usuario. |
| Usuario | DELETE | `/api/usuarios/{id}` | Eliminar un usuario. |
| Usuario | PUT | `/api/usuarios/{id}/preferencias` | Registrar o actualizar preferencias del cliente. |
| Usuario | GET | `/api/usuarios/{id}/preferencias` | Consultar preferencias del cliente. |
| Evento | GET | `/api/eventos/lugares` | Listar lugares disponibles. |
| Evento | POST | `/api/eventos/lugares` | Crear un lugar con sectores numerados/no numerados (`ORGANIZADOR`). |
| Evento | GET | `/api/eventos` | Listar eventos disponibles. |
| Evento | POST | `/api/eventos?usuarioId={id}` | Crear un evento (`ORGANIZADOR`). |
| Evento | PATCH | `/api/eventos/{id}/estado?usuarioId={id}` | Publicar o cancelar un evento (`ORGANIZADOR` / `ADMINISTRADOR`). |
| Evento | PATCH | `/api/eventos/{id}/fecha?usuarioId={id}` | Modificar la fecha de un evento publicado (`ORGANIZADOR`). |
| Evento | GET | `/api/eventos/buscar` | Buscar eventos por filtros (ej. `ciudad`). |
| Evento | GET | `/api/eventos/{id}/similares?limit={n}` | Obtener eventos similares por co-compra. |
| Evento | GET | `/api/eventos/recomendaciones?limit={n}` | Obtener recomendaciones personalizadas para el cliente. |
| Fila Virtual | POST | `/api/fila/ingresar` | Ingresar a la fila virtual de un evento de alta demanda. |
| Fila Virtual | GET | `/api/fila/eventos/{eventoId}/estado` | Consultar estado y posición del cliente en la fila virtual. |
| Orden | POST | `/api/ordenes` | Crear una orden de compra (numerada, no numerada o alta demanda). |
| Orden | GET | `/api/ordenes` | Obtener las órdenes del cliente autenticado. |
| Orden | POST | `/api/ordenes/{ordenId}/pago` | Procesar el pago de una orden y emitir entradas. |
| Orden | GET | `/api/ordenes/reportes/{eventoId}` | Obtener reporte de ventas de un evento (`ORGANIZADOR`). |
| Orden | PUT | `/api/ordenes/{eventoId}/cancelacion` | Cancelar órdenes asociadas a un evento cancelado. |
| Reserva | POST | `/api/reservas` | Crear una reserva (admite header opcional `Idempotency-Key`). |
| Reserva | GET | `/api/reservas` | Consultar todas las reservas. |
| Reserva | GET | `/api/reservas/{id}` | Consultar una reserva por ID. |
| Reserva | GET | `/api/reservas/cliente/{idCliente}/evento/{idEvento}/confirmadas` | Consultar reservas confirmadas de un cliente en un evento. |
| Reserva | PUT | `/api/reservas/{id}/estado` | Actualizar el estado de una reserva y liberar asientos si vence/cancela. |
| Reserva | GET | `/api/reservas/{id}/qr` | Descargar imagen PNG del código QR de una reserva confirmada. |
| Entrada | POST | `/api/entradas` | Emitir entradas numeradas o no numeradas. |
| Entrada | GET | `/api/entradas/{id}` | Consultar una entrada por ID. |
| Entrada | POST | `/api/entradas/revision` | Revisión de administrador sobre una entrada (`ADMINISTRADOR`). |
| Entrada | PATCH | `/api/entradas/validar` | Validar código QR en el ingreso (`PERSONAL_DE_ACCESO`). |
| Entrada | GET | `/api/entradas/cliente/{id}/eventos` | Obtener historial de eventos comprados por un cliente. |
| Entrada | GET | `/api/entradas/co-compras/evento/{eventoId}?limit={n}` | Obtener IDs de eventos co-comprados junto a un evento dado. |
| Pago | GET | `/api/pagos` | Obtener listado de pagos registrados. |
| Pago | POST | `/api/pagos/reserva/{idReserva}/reembolso` | Procesar el reembolso de un pago aprobado. |
| Notificación | POST | `/api/notificaciones/notificar` | Enviar una notificación (confirmación, cambio de fecha, etc.). |
| Notificación | GET | `/api/notificaciones` | Obtener todas las notificaciones registradas. |
| Notificación | GET | `/api/notificaciones/canceladas` | Obtener notificaciones de eventos cancelados para un usuario. |

---

## Colección de Postman

El repositorio incluye la colección de Postman completa:

`REST API SistemaTicketYa- CRUD, test & variable`

La colección genera automáticamente un `runId` único por ejecución (evitando colisiones de emails, lugares y asientos) y captura automáticamente los IDs y tokens JWT en variables de colección.

Para utilizarla:

1. Abrir Postman.
2. Seleccionar la opción **Import**.
3. Importar el archivo JSON de la colección.
4. Verificar que todos los microservicios (incluidos **Gateway :8080** y **Fila Virtual :8088**) estén levantados.
5. Ejecutar las peticiones en orden secuencial o mediante **Collection Runner**.

**Se recomienda ejecutar las carpetas en su orden completo para cubrir el flujo de principio a fin:**
1. **`01 - Usuario y JWT`** (alta de roles, login JWT y CRUD).
2. **`02 - Evento y Lugares`** (lugares numerados/no numerados, eventos de baja y alta demanda, publicación, cambio de fecha, cancelación y búsqueda).
3. **`03 - Fila Virtual`** (encolamiento y consulta de estado para eventos de alta demanda).
4. **`04 - Orden`** (compra y pago de órdenes numeradas, no numeradas y de alta demanda, reportes y cancelación).
5. **`05 - Reserva`** (creación y consulta de reservas).
6. **`06 - Entrada`** (emisión directa, revisión de administrador y validación de QR).
7. **`07 - Pago`** (consulta de pagos y reembolsos).
8. **`08 - Notificacion`** (envío y consulta de notificaciones y cancelaciones).
9. **`09 - Preferencias y Recomendaciones`** (preferencias del cliente, historial, co-compras, eventos similares y recomendaciones personalizadas).

---

## Documentación técnica

| Documento | Contenido |
|---|---|
| [Diagramas de arquitectura](docs/diagramas.md) | Diagrama de módulos, diagramas entidad-relación por microservicio, relaciones entre servicios y entidades solapadas |
| [Diagrama de secuencia de la compra](openspec/changes/archive/2026-11-06-orden-flujo-compra/design.md#11-diagrama-de-secuencia--compra-de-una-entrada-numerada) | Compra de una entrada numerada, incluyendo pago rechazado y reserva vencida |
| [Arquitectura de microservicios](arquitectura_de_microservicios.md) | Responsabilidad y endpoints de cada microservicio |
| [Orden](Orden/README.md) | Flujo de compra, estados de la orden y cómo correr sus tests |
| [Búsqueda y cache de Evento](Evento/CACHE_Y_BUSQUEDA.md) | Filtros de búsqueda y decisiones de cache |
| Swagger | Con cada microservicio levantado: `http://localhost:<puerto>/swagger-ui.html` |

---

## Tests, Concurrencia e Idempotencia

En cada microservicio: `./mvnw test` (o desde IntelliJ, *Run All Tests* sobre `src/test/java`).

- **Lógica de negocio (unitarios con Mockito):** compra y límite por cliente (Orden), emisión, reintegros y devoluciones (Entrada), eventos, estados y búsqueda (Evento), reservas y capacidad de zonas (Reserva), pagos (Pago), usuarios (Usuario).
- **No doble venta e idempotencia (multi-instancia):**
  - **En `Orden`:** `Orden/src/test/.../OrdenConcurrenciaTest` lanza múltiples compras simultáneas sobre el mismo asiento y verifica que solo una resulta exitosa (requiere la base de Orden levantada).
  - **En `Reserva` (mecanismo persistente en base de datos compartida para soportar varias instancias en paralelo):**
    - **Sectores numerados (`NUMERADO`):** tabla `asiento_bloqueado` con restricción de unicidad (`UNIQUE`) sobre `(id_evento, sector, asiento)`. Los asientos se insertan ordenados para prevenir *deadlocks*; ante solicitudes simultáneas desde una o varias instancias, la base de datos acepta una sola reserva y rechaza el resto con `DataIntegrityViolationException`, traducida a `409 Conflict`. Cuando una reserva vence o se cancela, los registros se eliminan para liberar los asientos.
    - **Sectores no numerados (`NO_NUMERADO`):** tabla `bloqueo_sector` con bloqueo pesimista (`@Lock(LockModeType.PESSIMISTIC_WRITE)` / `SELECT ... FOR UPDATE`) por `(id_evento, sector)` y nivel de aislamiento `Isolation.READ_COMMITTED`, serializando el conteo de capacidad entre múltiples instancias concurrentes.
    - **Idempotencia:** soporte para el header `Idempotency-Key` respaldado por la columna única `idempotency_key` en la tabla `reserva`, devolviendo la reserva ya creada ante reintentos o dobles envíos simultáneos.
    - **Validación de titularidad:** verificación contra `Usuario` de que el cliente exista y contra `Orden` (cuando se envía `idOrden`) de que el cliente coincida con el titular de la orden.
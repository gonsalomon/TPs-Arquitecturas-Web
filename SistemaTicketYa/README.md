# TicketYa – Sistema de venta de entradas

## Descripción

TicketYa es una plataforma de venta de entradas para distintos tipos de eventos, desarrollada mediante una arquitectura de microservicios con Spring Boot.

El sistema permite gestionar usuarios, consultar eventos disponibles, generar órdenes de compra, procesar pagos y emitir entradas.

## Requisitos previos

Para ejecutar el proyecto se necesita:

- Java 21.
- Maven.
- Las bases de datos correspondientes a cada microservicio en funcionamiento.
- MongoDB para la persistencia de eventos.
- Postman para realizar las pruebas de los endpoints.

## Ejecución del proyecto

Cada microservicio es un proyecto Spring Boot independiente y debe iniciarse por separado.

Los servicios deben estar levantados antes de comenzar a realizar las peticiones.

### Microservicios y puertos

| Microservicio | Puerto |
|---|---:|
| Usuario | 8081 |
| Pago | 8082 |
| Evento | 8083 |
| Reserva | 8084 |
| Entrada | 8085 |
| Notificación | 8086 |
| Orden | 8087 |

## Orden de ejecución de los endpoints

Para probar correctamente el circuito de compra de entradas es necesario respetar el siguiente orden de ejecución.

### 1. Crear usuarios

Primero se deben registrar los usuarios que participarán en las operaciones del sistema.

Se recomienda crear, como mínimo:
- Un usuario con rol `ORGANIZADOR`.
- Un usuario con rol `CLIENTE`.

**Endpoint:**

`POST http://localhost:8081/api/usuarios`

Ejemplo de creación de organizador:

```json
{
  "nombre": "Santiago",
  "apellido": "Sosa",
  "email": "organizador@example.com",
  "rol": "ORGANIZADOR"
}
```

Ejemplo de creación de cliente:

```json
{
  "nombre": "Maria",
  "apellido": "Cabrera",
  "email": "cliente@example.com",
  "rol": "CLIENTE"
}
```

Es importante conservar los IDs generados, ya que se utilizarán posteriormente para identificar al cliente y al organizador.

### 2. Consultar eventos disponibles

Los eventos ya cuentan con datos precargados en MongoDB, por lo que no es necesario crearlos para realizar el circuito de compra.

**Endpoint:**

`GET http://localhost:8083/api/eventos`

También se pueden consultar los lugares disponibles mediante:

`GET http://localhost:8083/api/lugares`

Para realizar una compra se debe seleccionar un evento existente y utilizar su ID.

**Importante:** los identificadores de los eventos almacenados en MongoDB son diferentes de los IDs numéricos utilizados por otros microservicios. Se debe utilizar el identificador correspondiente al evento devuelto por la API.

### 3. Crear una orden de compra

Una vez seleccionado el evento, el cliente puede generar una orden indicando el evento, el sector y los asientos que desea comprar.

**Endpoint:**

`POST http://localhost:8087/api/ordenes`

**Header requerido:**

`X-Usuario-Id: ID_DEL_CLIENTE`

Ejemplo de Body:

```json
{
  "eventoId": "ID_DEL_EVENTO",
  "sector": "platea",
  "asientos": [
    {
      "fila": 1,
      "asiento": 5
    },
    {
      "fila": 1,
      "asiento": 6
    }
  ]
}
```

La creación de la orden inicia el proceso de compra y permite continuar con el pago.

Es necesario conservar el ID de la orden generada, ya que se utilizará en el siguiente paso.

### 4. Realizar el pago de la orden

Una vez creada la orden, se debe ejecutar el endpoint de pago correspondiente.

**Endpoint:**

`POST http://localhost:8087/api/ordenes/{ordenId}/pago`

Ejemplo:

`POST http://localhost:8087/api/ordenes/1/pago`

**Headers requeridos:**

| Header | Valor |
|---|---|
| X-Usuario-Id | ID del cliente |
| clienteId | ID del cliente |

**Body:**

```json
{
  "medioPago": "TARJETA_CREDITO"
}
```

Esta operación procesa el pago de la orden. Cuando el pago se confirma, se completa el proceso de compra y se valida/genera la entrada correspondiente.

**Importante:** no es necesario crear previamente una entrada de manera manual. El procesamiento del pago de la orden se encarga de completar este flujo.

### 5. Consultar las entradas generadas

Finalmente, se pueden consultar las entradas para verificar que la compra se haya completado correctamente.

**Endpoint:**

`GET http://localhost:8085/api/entradas/{clienteId}`

Reemplazar `{clienteId}` por el identificador del cliente correspondiente.

## Otros endpoints disponibles

Además del circuito principal de compra, la colección de Postman incluye operaciones adicionales para probar las funcionalidades de los microservicios.

| Microservicio | Método | Endpoint | Descripción |
|---|---|---|---|
| Usuario | GET | `/api/usuarios` | Obtener usuarios. |
| Usuario | PUT | `/api/usuarios/{id}` | Modificar un usuario. |
| Usuario | DELETE | `/api/usuarios/{id}` | Eliminar un usuario. |
| Evento | POST | `/api/lugares` | Crear un lugar. |
| Evento | POST | `/api/eventos?usuarioId={id}` | Crear un evento. |
| Evento | PATCH | `/api/eventos/{id}/estado?usuarioId={id}` | Modificar un evento o su estado. |
| Evento | GET | `/api/eventos/buscar` | Buscar eventos mediante filtros. |
| Orden | GET | `/api/ordenes` | Obtener órdenes. |
| Pago | GET | `/api/pagos` | Obtener pagos. |
| Reserva | GET | `/api/reservas` | Consultar reservas. |
| Entrada | GET | `/api/entradas/{id}` | Consultar entradas. |
| Notificación | POST | `/api/notificaciones/notificar` | Enviar una notificación. |

Los endpoints de reservas, revisiones de entradas y reembolsos también se encuentran disponibles en la colección de Postman para probar operaciones adicionales.

## Colección de Postman

El repositorio incluye la colección de Postman:

`REST API SistemaTicketYa: CRUD, test & variable`

Para utilizarla:

1. Abrir Postman.
2. Seleccionar la opción **Import**.
3. Importar el archivo JSON de la colección.
4. Verificar que todos los microservicios estén levantados.
5. Ejecutar las peticiones respetando el orden indicado en este README.

**Se recomienda comenzar por la carpeta Usuario y continuar con las operaciones de Evento, Orden, Pago y Entrada.**


## Documentación técnica

| Documento | Contenido |
|---|---|
| [Diagramas de arquitectura](docs/diagramas.md) | Diagrama de módulos, diagramas entidad-relación por microservicio, relaciones entre servicios y entidades solapadas |
| [Diagrama de secuencia de la compra](openspec/changes/archive/2026-11-06-orden-flujo-compra/design.md#11-diagrama-de-secuencia--compra-de-una-entrada-numerada) | Compra de una entrada numerada, incluyendo pago rechazado y reserva vencida |
| [Arquitectura de microservicios](arquitectura_de_microservicios.md) | Responsabilidad y endpoints de cada microservicio |
| [Orden](Orden/README.md) | Flujo de compra, estados de la orden y cómo correr sus tests |
| [Búsqueda y cache de Evento](Evento/CACHE_Y_BUSQUEDA.md) | Filtros de búsqueda y decisiones de cache |
| Swagger | Con cada microservicio levantado: `http://localhost:<puerto>/swagger-ui.html` |

## Tests

En cada microservicio: `./mvnw test` (o desde IntelliJ, *Run All Tests* sobre `src/test/java`).

- **Lógica de negocio (unitarios con Mockito):** compra y límite por cliente (Orden), emisión, reintegros y devoluciones (Entrada), eventos, estados y búsqueda (Evento), reservas y capacidad de zonas (Reserva), pagos (Pago), usuarios (Usuario).
- **No doble venta:** `Orden/src/test/.../OrdenConcurrenciaTest` lanza muchas compras simultáneas del mismo asiento y verifica que solo una resulta exitosa (requiere la base de Orden levantada).

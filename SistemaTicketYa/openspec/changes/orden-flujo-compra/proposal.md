# Proposal

## Why

La Entrega 1 del TPE TicketYa exige el circuito de compra con reserva temporal, emisión de entradas con QR y sin doble venta (requisitos 4 y 5 del enunciado). Ningún microservicio coordina hoy una compra de punta a punta: Reserva bloquea lugares, Pago cobra y Entrada emite, pero falta quien los llame en orden, registre cada intento de compra y decida qué hacer cuando un paso falla. Ese es el rol del microservicio Orden, que hoy es solo un esqueleto.

## What Changes

- Nuevo flujo de compra en dos pasos expuesto por Orden (puerto 8087, ruta `/api/ordenes`, ya enrutada por el Gateway):
  1. **Crear orden** (`POST /api/ordenes`): valida el evento con Evento (existe y está `PUBLICADO`), toma de Evento el precio del sector y la fecha del evento, valida el límite de 6 entradas por cliente y evento, solicita la reserva temporal a Reserva y deja la orden en `PENDIENTE_PAGO` con fecha de vencimiento.
  2. **Pagar orden** (`POST /api/ordenes/{id}/pago`): si la reserva venció, la libera y la orden queda `VENCIDA` sin cobrar; si no, cobra con Pago. Aprobado → solicita a Entrada la emisión de entradas con QR y la orden queda `CONFIRMADA`. Rechazado → libera la reserva y la orden queda `RECHAZADA`.
- Reintento de pago idempotente: pagar de nuevo una orden ya procesada devuelve el mismo resultado sin cobrar ni emitir dos veces.
- Prevención de doble venta de asientos numerados en Orden mediante restricción única en base de datos, válida con varias instancias del servicio.
- Vencimiento automático de órdenes pendientes cuya reserva expiró, liberando los lugares.
- Registro del detalle de la compra (items, precio unitario, total) y del medio de pago.
- Consultas: `GET /api/ordenes/{id}` y listado de órdenes del cliente.
- Identificación del cliente por header `X-Usuario-Id` (Entrega 1 sin autenticación).
- Ajustes de configuración de Orden: datasource a `localhost:3309` (hoy apunta por error a 3306, la base de Reserva) y eliminación de la dependencia `spring-cloud-starter-gateway-mvc`, que no corresponde a un servicio que no es el gateway.

**Fuera de alcance:** devoluciones y cancelación de eventos (change aparte `orden-devoluciones`); JWT y roles, saga completa con resiliencia (reintentos, timeouts, circuit breaker, reembolso automático), notificaciones, log centralizado y fila virtual (Entrega 2).

## Capabilities

### New Capabilities
- `orden/compra-entradas`: ciclo de vida de una orden de compra de entradas — creación con validaciones, reserva temporal, pago, emisión, rechazo, vencimiento, idempotencia de reintentos, no doble venta y consulta de órdenes.

### Modified Capabilities
- (ninguna; no existen specs previas en el proyecto)

## Impact

- **Código:** microservicio `Orden` (entidades, repositorios, servicio, controller, clientes Feign, manejo de errores, tests), `Orden/pom.xml` y `Orden/src/main/resources/application.yml`.
- **APIs consumidas:**
  - Evento (`GET /api/eventos/{id}`, `GET /api/lugares/{id}`) — existente en la rama `ms-evento`.
  - Entrada (emisión idempotente por `ordenId`) — existente en la rama `entrada-crud`; **depende** del cambio acordado por el grupo: `eventoId` como `String` y sector identificado por nombre.
  - Reserva (`POST /api/reservas`, `PUT /api/reservas/{id}/estado`) y Pago (`POST /api/pagos`, mock de pasarela) — existentes en la rama `ms-pago`; Orden se adapta a sus endpoints y los mockea en los tests.
  - **Decisión del grupo:** Orden coordina la compra. Pago y Reserva requieren cambios mínimos para alinearse (dejar las validaciones de compra a Orden, pago idempotente, rechazo en el mock, estado para liberar reservas, 409 sin lugar); el detalle está en `design.md`, decisión 9.
- **API expuesta:** `/api/ordenes` (documentada con OpenAPI/Swagger).
- **Base de datos:** esquema `ordenes` en el MySQL propio de Orden (contenedor `Orden-mysql`, puerto 3309).
- **Documentación del TP:** aporta el diagrama de secuencia del flujo de compra de una entrada numerada (pago rechazado y reserva vencida) y el modelo de datos de Orden.

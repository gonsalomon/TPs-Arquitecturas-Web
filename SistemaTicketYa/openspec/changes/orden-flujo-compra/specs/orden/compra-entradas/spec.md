# Spec Delta

## Purpose

Coordinar la compra de entradas de un evento de punta a punta: registrar cada intento de compra como una orden, reservar los lugares, cobrar, emitir las entradas y deshacer los pasos realizados cuando el pago se rechaza o la reserva vence, garantizando que un mismo lugar nunca se venda dos veces.

## ADDED Requirements

### Requirement: Identificación del cliente
Toda operación sobre órdenes DEBE identificar al cliente mediante el header `X-Usuario-Id` (numérico). Sin ese header la solicitud DEBE rechazarse. Una orden solo DEBE poder consultarse o pagarse por el cliente que la creó.

#### Scenario: Solicitud sin cliente
- **WHEN** se invoca cualquier endpoint de órdenes sin el header `X-Usuario-Id`
- **THEN** la respuesta es 400 y no se crea ni modifica ninguna orden

#### Scenario: Orden de otro cliente
- **WHEN** un cliente consulta o paga una orden creada por otro cliente
- **THEN** la respuesta es 403 y la orden no se modifica

### Requirement: Crear una orden de compra
El cliente DEBE poder crear una orden indicando el evento (`eventoId`, texto), el sector (por nombre) y, si el sector es numerado, la lista de asientos (fila y asiento); si es no numerado, la cantidad de lugares. Una orden creada con éxito DEBE quedar en estado `PENDIENTE_PAGO` con una fecha de vencimiento.

#### Scenario: Orden creada para asientos numerados
- **WHEN** un cliente crea una orden para un evento publicado, un sector numerado existente y asientos disponibles
- **THEN** la respuesta es 201 con la orden en estado `PENDIENTE_PAGO`, sus items, el precio unitario, el total y la fecha de vencimiento

#### Scenario: Orden creada para sector no numerado
- **WHEN** un cliente crea una orden indicando una cantidad de lugares en un sector no numerado con capacidad disponible
- **THEN** la respuesta es 201 con la orden en estado `PENDIENTE_PAGO` y tantos items como la cantidad pedida

#### Scenario: Datos inconsistentes con el tipo de sector
- **WHEN** se piden asientos en un sector no numerado, o una cantidad sin asientos en un sector numerado, o un asiento fuera del rango de filas y asientos del sector, o asientos repetidos en el mismo pedido
- **THEN** la respuesta es 400 y no se crea la orden

### Requirement: Validar el evento antes de vender
Antes de reservar, la orden DEBE verificar con el servicio de eventos que el evento exista, que esté en estado `PUBLICADO` y que el sector pedido exista en su lugar con precio definido. Un evento en cualquier otro estado (borrador, agotado, cancelado, finalizado) NO DEBE admitir ventas.

#### Scenario: Evento inexistente
- **WHEN** se crea una orden para un `eventoId` que no existe
- **THEN** la respuesta es 404 y no se solicita ninguna reserva

#### Scenario: Evento no publicado
- **WHEN** se crea una orden para un evento que no está en estado `PUBLICADO`
- **THEN** la respuesta es 409 y no se solicita ninguna reserva

#### Scenario: Sector inexistente o sin precio
- **WHEN** se crea una orden para un sector que no existe en el lugar del evento o que no tiene precio definido para ese evento
- **THEN** la respuesta es 400 y no se solicita ninguna reserva

### Requirement: Precio definido por el evento
El precio unitario de cada item DEBE tomarse del precio que el organizador definió para ese sector en el evento, nunca de los datos enviados por el cliente. El total de la orden DEBE ser la suma de los precios de sus items.

#### Scenario: Precio ignorado del cliente
- **WHEN** un cliente crea una orden de 2 asientos en un sector cuyo precio en el evento es 15000
- **THEN** cada item tiene precio unitario 15000 y el total es 30000, independientemente de cualquier precio enviado en la solicitud

### Requirement: Límite de entradas por cliente y evento
Un cliente NO DEBE superar 6 entradas para un mismo evento. Cuentan los items de sus órdenes en estado `PENDIENTE_PAGO`, `PAGO_APROBADO` o `CONFIRMADA` para ese evento. La validación DEBE hacerse antes de solicitar la reserva.

#### Scenario: Pedido dentro del límite
- **WHEN** un cliente con 4 entradas activas para un evento crea una orden de 2 entradas para ese evento
- **THEN** la orden se crea

#### Scenario: Pedido que excede el límite
- **WHEN** un cliente con 5 entradas activas para un evento crea una orden de 2 entradas para ese evento
- **THEN** la respuesta es 422, no se crea la orden y no se solicita ninguna reserva

#### Scenario: Órdenes rechazadas o vencidas no cuentan
- **WHEN** un cliente tiene una orden `RECHAZADA` de 6 entradas para un evento y crea una nueva orden de 6 entradas para ese evento
- **THEN** la orden se crea

### Requirement: Reserva temporal de los lugares
Al crear la orden, los lugares DEBEN reservarse temporalmente en el servicio de reservas. La fecha de vencimiento de la orden DEBE coincidir con la de la reserva. Si los lugares no están disponibles, la orden NO DEBE crearse.

#### Scenario: Lugares no disponibles
- **WHEN** el servicio de reservas informa que alguno de los lugares pedidos no está disponible
- **THEN** la respuesta es 409 y no queda ninguna orden activa para esos lugares

#### Scenario: Servicio de reservas no disponible
- **WHEN** el servicio de reservas no responde al crear la orden
- **THEN** la respuesta es 503 y no queda ninguna orden activa para esos lugares

### Requirement: No doble venta de asientos numerados
Un mismo asiento numerado de un evento NO DEBE pertenecer a más de una orden activa (`PENDIENTE_PAGO`, `PAGO_APROBADO` o `CONFIRMADA`) al mismo tiempo, aun con solicitudes simultáneas y varias instancias del servicio ejecutándose. Al pasar una orden a `RECHAZADA` o `VENCIDA`, sus asientos DEBEN quedar libres para otra orden.

#### Scenario: Compras simultáneas del mismo asiento
- **WHEN** muchos clientes crean al mismo tiempo órdenes para el mismo asiento de un evento
- **THEN** exactamente una orden queda activa y todas las demás reciben 409

#### Scenario: Asiento liberado se puede volver a comprar
- **WHEN** una orden que tenía un asiento pasa a `RECHAZADA` o `VENCIDA` y luego otro cliente crea una orden para ese asiento
- **THEN** la nueva orden se crea

### Requirement: Pago aprobado y emisión de entradas
Al pagar una orden `PENDIENTE_PAGO` no vencida, la orden DEBE registrar el medio de pago y solicitar el cobro del total a la pasarela. Si el pago se aprueba, DEBE confirmar la reserva, solicitar la emisión de las entradas con código QR y quedar `CONFIRMADA`, devolviendo las entradas emitidas.

#### Scenario: Pago aprobado
- **WHEN** el cliente paga una orden pendiente no vencida y la pasarela aprueba el pago
- **THEN** la respuesta es 200 con la orden en estado `CONFIRMADA`, el medio de pago, la referencia del pago y las entradas emitidas con su código QR

#### Scenario: Medio de pago inválido
- **WHEN** el cliente paga sin indicar un medio de pago válido
- **THEN** la respuesta es 400 y no se solicita ningún cobro

### Requirement: Pago rechazado libera los lugares
Si la pasarela rechaza el pago, la orden DEBE solicitar la liberación de la reserva y quedar `RECHAZADA`, sin emitir entradas.

#### Scenario: Pago rechazado
- **WHEN** el cliente paga una orden pendiente y la pasarela rechaza el pago
- **THEN** la respuesta es 200 con la orden en estado `RECHAZADA`, la reserva se libera y no se emiten entradas

### Requirement: Vencimiento de la reserva
Una orden `PENDIENTE_PAGO` cuya fecha de vencimiento pasó NO DEBE cobrarse: DEBE quedar `VENCIDA` y solicitar la liberación de la reserva. Esto DEBE ocurrir tanto al intentar pagarla como de forma automática para órdenes abandonadas.

#### Scenario: Pago de una orden vencida
- **WHEN** el cliente intenta pagar una orden cuya fecha de vencimiento ya pasó
- **THEN** la respuesta es 409, la orden queda `VENCIDA`, la reserva se libera y no se solicita ningún cobro

#### Scenario: Orden abandonada
- **WHEN** una orden queda en `PENDIENTE_PAGO` sin que el cliente la pague y pasa su fecha de vencimiento
- **THEN** en un plazo breve la orden pasa a `VENCIDA` y sus lugares quedan libres

### Requirement: Reintento de pago idempotente
Pagar nuevamente una orden ya procesada NO DEBE generar un segundo cobro ni una segunda emisión de entradas, y DEBE devolver el mismo resultado que el primer intento. Si un intento anterior quedó incompleto (pago aprobado pero entradas no emitidas), el reintento DEBE completar la emisión sin volver a cobrar.

#### Scenario: Reintento de una orden confirmada
- **WHEN** el cliente vuelve a pagar una orden `CONFIRMADA`
- **THEN** la respuesta es 200 con la misma orden y las mismas entradas, sin nuevo cobro ni nueva emisión

#### Scenario: Reintento de una orden rechazada
- **WHEN** el cliente vuelve a pagar una orden `RECHAZADA`
- **THEN** la respuesta es 200 con la orden `RECHAZADA`, sin nuevo cobro

#### Scenario: Reintento tras fallo en la emisión
- **WHEN** un pago fue aprobado pero la emisión de entradas falló, y el cliente reintenta el pago
- **THEN** no se realiza un nuevo cobro, se emiten las entradas y la orden queda `CONFIRMADA`

#### Scenario: Pasarela no disponible
- **WHEN** la pasarela de pagos no responde al pagar una orden pendiente
- **THEN** la respuesta es 503, la orden sigue `PENDIENTE_PAGO` y el cliente puede reintentar sin riesgo de doble cobro

### Requirement: Consultar órdenes
El cliente DEBE poder consultar una orden propia por su identificador y listar sus órdenes, opcionalmente filtradas por evento. Cada orden DEBE mostrar estado, evento, sector, items con precio, total, medio de pago, referencia del pago, fechas de creación y vencimiento.

#### Scenario: Consulta de una orden
- **WHEN** un cliente consulta una orden propia existente
- **THEN** la respuesta es 200 con el detalle completo de la orden

#### Scenario: Orden inexistente
- **WHEN** se consulta o paga una orden que no existe
- **THEN** la respuesta es 404

#### Scenario: Listado de órdenes del cliente
- **WHEN** un cliente lista sus órdenes indicando un evento
- **THEN** la respuesta es 200 con solo sus órdenes de ese evento

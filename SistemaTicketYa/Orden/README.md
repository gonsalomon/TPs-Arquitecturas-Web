# Orden

Microservicio que coordina la compra de entradas del TPE TicketYa: valida el evento, reserva
los lugares, cobra con Pago, emite las entradas con Entrada y deshace lo hecho si el pago se
rechaza o la reserva vence. Ver el diseño completo en
[`design.md`](../openspec/changes/archive/2026-11-06-orden-flujo-compra/design.md).

## Levantar la base

```bash
cd Orden
docker compose up -d
```

Esto levanta MySQL en el contenedor `Orden-mysql`, publicado en el puerto **3309** (host) →
3306 (contenedor), con la base `ordenes` ya creada.

## Levantar el servicio

```bash
cd Orden
.\mvnw spring-boot:run
```

La aplicación arranca en el puerto **8087**. Requiere `Orden-mysql` levantado.

## Correr los tests

```bash
cd Orden
.\mvnw test
```

Los tests que tocan base de datos (`OrdenApplicationTests.contextLoads`, los `@DataJpaTest` de
`OrdenRepositoryTest` y el test de concurrencia `OrdenConcurrenciaTest`) requieren
`Orden-mysql` levantado y usan el esquema `ordenes_test` (se crea solo). Los tests unitarios de
`service`, `mapper`, `utils` y `exception` no necesitan la base ni ningún otro microservicio
levantado (mockean los clientes Feign).

## Configuración de otros servicios

Las URLs de los microservicios con los que integra Orden están en `application.yml`:

```yaml
servicios:
  evento:
    url: http://localhost:8083/api
  reserva:
    url: http://localhost:8084/api
  pago:
    url: http://localhost:8082/api
  entrada:
    url: http://localhost:8085
```

Ver la [decisión 9 del `design.md`](../openspec/changes/archive/2026-11-06-orden-flujo-compra/design.md#9-cambios-pendientes-en-otros-servicios)
para el detalle de los cambios pendientes en cada servicio.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/ordenes` | Crea una orden: valida el evento, reserva los lugares, queda `PENDIENTE_PAGO` |
| POST | `/api/ordenes/{id}/pago` | Paga una orden pendiente; reintentar sobre una orden ya procesada no cobra ni emite de nuevo |
| GET | `/api/ordenes/{id}` | Consulta una orden propia |
| GET | `/api/ordenes?eventoId=` | Lista las órdenes del cliente, opcionalmente filtradas por evento |

Todos requieren el header `X-Usuario-Id` (numérico, sin autenticación en esta entrega). Swagger
UI disponible en `http://localhost:8087/swagger-ui.html` con la app levantada.

## Colección Postman

- Colección del microservicio: [`Orden.postman_collection.json`](Orden.postman_collection.json)
- Colección completa del sistema (todos los microservicios): [`REST API SistemaTicketYa - CRUD, test & variable`](../REST%20API%20SistemaTicketYa-%20CRUD,%20test%20%26%20variable.postman_collection.json)

Importar en Postman con **Import → File**. La colección de Orden apunta a `http://localhost:8087` y usa el header `X-Usuario-Id`; la global cubre los puertos 8081–8087.

## Estados de la orden

```
PENDIENTE_PAGO ──pago aprobado──▶ PAGO_APROBADO ──entradas emitidas──▶ CONFIRMADA
      │  │
      │  └──pago rechazado──▶ RECHAZADA
      └──vencimiento───────▶ VENCIDA
```

`PENDIENTE_PAGO`, `PAGO_APROBADO` y `CONFIRMADA` son estados activos (ocupan asiento y cuentan
para el límite de 6 entradas por cliente y evento). `RECHAZADA` y `VENCIDA` son terminales y
liberan los asientos.

## Diagrama de secuencia — compra de una entrada numerada

Ver la sección ["11. Diagrama de secuencia"](../openspec/changes/archive/2026-11-06-orden-flujo-compra/design.md#diagrama-de-secuencia) del `design.md`.

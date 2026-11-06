# Tasks

> Los tests que tocan la base (`contextLoads`, `@DataJpaTest`, concurrencia) requieren el contenedor `Orden-mysql` levantado: `docker compose up -d` en `Orden/`. Usan el esquema `ordenes_test`.

## 1. Configuración del proyecto Orden

- [x] 1.1 Quitar `spring-cloud-starter-gateway-mvc` del `Orden/pom.xml`; verificar con `.\mvnw -q dependency:tree` que ya no aparece y que el proyecto compila
- [x] 1.2 Corregir `Orden/src/main/resources/application.yml`: datasource a `jdbc:mysql://localhost:3309/ordenes?createDatabaseIfNotExist=true`, y agregar `servicios.{evento,reserva,pago,entrada}.url`, `orden.limite-entradas-por-evento: 6`, `orden.minutos-reserva: 10`, intervalo del job de vencimiento y timeouts de Feign (connect 2 s, read 5 s); verificar que con `Orden-mysql` levantado la aplicación arranca en el 8087
- [x] 1.3 Crear `Orden/src/test/resources/application.yml` apuntando a `jdbc:mysql://localhost:3309/ordenes_test?createDatabaseIfNotExist=true` con `ddl-auto: create-drop`, y habilitar `@EnableScheduling`; verificar que `OrdenApplicationTests.contextLoads` pasa con `.\mvnw test` y que se crea el esquema `ordenes_test`

## 2. Modelo de datos

- [x] 2.1 Crear enums `EstadoOrden` (`PENDIENTE_PAGO`, `PAGO_APROBADO`, `CONFIRMADA`, `RECHAZADA`, `VENCIDA`, con método `esActiva()`) y `MedioPago` en `utils`; verificar con un test unitario que solo los tres primeros son activos
- [x] 2.2 Crear entidades `Orden` (con `@Version`, fechas, `idReserva`, `idPago`, total `BigDecimal`) e `ItemOrden` (con `claveAsientoActivo` UNIQUE sincronizada en `@PrePersist/@PreUpdate` según el estado de la orden) según el modelo de `design.md`; verificar en `ordenes_test` que la tabla `item_orden` tiene la restricción única y que `orden` tiene los índices
- [x] 2.3 Crear `OrdenRepository` con la suma de items activos por cliente/evento, búsqueda de pendientes vencidas y listado por cliente (y evento opcional); verificar con un `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` sobre MySQL que la suma ignora órdenes `RECHAZADA`/`VENCIDA`, que dos items activos con la misma clave lanzan `DataIntegrityViolationException` y que dos claves `NULL` no

## 3. Clientes de otros servicios

- [x] 3.1 Crear DTOs de integración y clientes Feign según los contratos reales de `design.md` (decisión 8): `EventoClient` (evento y lugar), `ReservaClient` (`POST /api/reservas`, `PUT /api/reservas/{id}/estado` para `CONFIRMADA`/`LIBERADA`), `PagoClient` (`POST /api/pagos` con `idReserva`, `idCliente`, `metodoPago`) y `EntradaClient` (emitir), con URLs desde configuración; verificar que el contexto levanta con `.\mvnw test`
- [x] 3.2 Implementar el `ErrorDecoder` que traduce 404/409 remotos a excepciones de negocio y errores de conexión o 5xx a `ServicioNoDisponibleException`; verificar con un test unitario del decoder para 404, 409 y 503
- [x] 3.3 Implementar la conversión de fila/asiento al número de asiento de Reserva (`(fila - 1) × asientosPorFila + asiento`); verificar con un test unitario (ej. 20 por fila: fila 1 asiento 1 = 1, fila 3 asiento 5 = 45, fila 2 asiento 20 = 40)

## 4. Crear orden

- [x] 4.1 Crear DTOs `CrearOrdenRequestDTO` (eventoId, sector, asientos o cantidad, con Bean Validation), `OrdenResponseDTO`/`ItemOrdenResponseDTO` y `OrdenMapper`; verificar con un test unitario del mapper
- [x] 4.2 Implementar en `OrdenService.crear` la validación del evento (existe, `PUBLICADO`), del sector y sus asientos contra el lugar (tipo, rango, repetidos, asientos vs cantidad) y el precio tomado de Evento; verificar con tests unitarios (Mockito) de los escenarios de "Crear una orden", "Validar el evento antes de vender" y "Precio definido por el evento" del spec
- [x] 4.3 Implementar la validación del límite configurable de 6 antes de reservar; verificar con tests unitarios de los tres escenarios de "Límite de entradas por cliente y evento"
- [x] 4.4 Implementar el insert de la orden con `saveAndFlush` (409 ante `DataIntegrityViolationException`), la llamada a Reserva, el guardado de `idReserva`/`fechaVencimiento` y el paso a `RECHAZADA` con claves liberadas si Reserva responde 409 o falla (503); verificar con tests unitarios de "Reserva temporal de los lugares"

## 5. Pagar orden

- [x] 5.1 Crear `PagarOrdenRequestDTO` (medioPago obligatorio: `TARJETA_CREDITO`, `TARJETA_DEBITO`, `BILLETERA_VIRTUAL`) e implementar `OrdenService.pagar` para `PENDIENTE_PAGO`: chequeo de vencimiento (→ `VENCIDA`, liberar reserva con `nuevoEstado=LIBERADA`, 409, sin llamar a Pago), cobro con `idReserva`, `ACEPTADO` → `PAGO_APROBADO` → confirmar reserva (`nuevoEstado=CONFIRMADA`) → emitir en Entrada → `CONFIRMADA` con entradas en la respuesta, `RECHAZADO` → liberar reserva → `RECHAZADA`; verificar con tests unitarios de "Pago aprobado y emisión de entradas", "Pago rechazado libera los lugares" y "Pago de una orden vencida"
- [x] 5.2 Implementar el comportamiento idempotente según el estado (`RECHAZADA` devuelve sin llamadas externas; `CONFIRMADA` devuelve reconsultando la emisión en Entrada sin llamar a Pago, ver decisión 5; `VENCIDA` → 409; `PAGO_APROBADO` solo reintenta la emisión; pasarela caída, `PENDIENTE` o 409 "ya pagada" → 503 sin nuevo cobro y la orden sigue pendiente) y el manejo de `OptimisticLockingFailureException` releyendo la orden; verificar con tests unitarios de cada escenario de "Reintento de pago idempotente", comprobando con Mockito que Pago se invoca como máximo una vez
- [x] 5.3 Implementar el job `@Scheduled` que pasa a `VENCIDA` las órdenes `PENDIENTE_PAGO` vencidas, libera sus claves y la reserva (`nuevoEstado=LIBERADA`); verificar con un test que invoca el método del job sobre una orden vencida y comprueba estado, clave en `NULL` y llamada a liberar

## 6. API REST y errores

- [x] 6.1 Crear `OrdenController` (`POST /api/ordenes`, `POST /api/ordenes/{id}/pago`, `GET /api/ordenes/{id}`, `GET /api/ordenes?eventoId=`) con header obligatorio `X-Usuario-Id`, control de propietario (403) y anotaciones Swagger con descripciones, ejemplos y códigos de error de `design.md`; verificar que `/swagger-ui.html` muestra los 4 endpoints documentados
- [x] 6.2 Crear las excepciones de negocio y el `GlobalExceptionHandler` (formato de ms-usuario más 400 de validación/header faltante, 403, 404, 409, 422, 503); verificar con tests `@WebMvcTest` del controller para header faltante (400), orden ajena (403), orden inexistente (404), límite (422) y servicio caído (503)

## 7. No doble venta

- [x] 7.1 Escribir el test de concurrencia (`@SpringBootTest` sobre `ordenes_test` en MySQL, Reserva mockeada OK): 50 hilos con `CountDownLatch` crean la orden del mismo asiento; verificar que exactamente 1 orden queda activa y 49 reciben conflicto, y que tras pasar esa orden a `RECHAZADA` una nueva orden para el asiento se crea
- [x] 7.2 Ejecutar la suite completa con `.\mvnw test` en `Orden/` (con `Orden-mysql` levantado) y verificar que todos los tests pasan

## 8. Documentación y pruebas manuales

- [x] 8.1 Crear la colección Postman `Orden/Orden.postman_collection.json` con crear orden, pagar (aprobado/rechazado), reintento, consultas y casos de error; verificar ejecutándola contra Orden con Evento levantado y Reserva/Pago/Entrada disponibles o simulados
- [x] 8.2 Crear `Orden/README.md` con cómo levantar la base (puerto 3309) y el servicio, cómo correr los tests (requieren el contenedor), configuración de URLs de otros servicios, endpoints, estados de la orden y el diagrama de secuencia de `design.md`; verificar que los comandos documentados funcionan tal como están escritos
- [ ] 8.3 Pasar al grupo la tabla "Cambios pendientes en otros servicios" de `design.md` (decisión 9: Pago, Reserva y Entrada) junto con la fórmula del número de asiento; verificar que cada cambio queda aplicado o registrar en el design, con `/opsx:update`, lo que se acuerde distinto

## Workflow follow-up

- Archivar el change con `/opsx:archive` una vez implementado, revisado y mergeado a `develop`.
- Siguiente change sugerido: `orden-devoluciones` (reintegros por devolución aprobada y cancelación de evento).

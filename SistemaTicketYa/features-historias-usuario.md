# Features e Historias de Usuario

## F01 — Gestión de eventos, lugares y sectores

### US-01.1 — Crear y publicar eventos

**Como** organizador, **quiero** crear un evento en borrador, completar sus datos y publicarlo, **para** ofrecerlo a la venta.

**Criterios de aceptación:**

- Dado un organizador que crea un evento, cuando lo guarda como borrador, entonces el evento incluye ID, nombre, descripción, tipo, organizador, participantes, fecha y hora, lugar y estado.
- Dado un evento configurado, cuando el organizador lo publica, entonces su estado pasa a publicado.
- Los datos y estados enumerados en el documento deben poder representarse. Las condiciones adicionales para publicar están por definir.

### US-01.2 — Configurar lugar, sectores y precios

**Como** organizador, **quiero** configurar los sectores y sus precios para un evento, **para** definir qué entradas se ofrecen.

**Criterios de aceptación:**

- Dado un lugar, cuando se registra, entonces se identifican su nombre, dirección, ubicación geográfica y sectores.
- Dado un sector numerado, cuando se configura, entonces se identifican sus filas y asientos.
- Dado un sector no numerado, cuando se configura, entonces se define su capacidad máxima.
- Dado un evento con sectores, cuando el organizador establece precios, entonces el precio queda definido por sector para ese evento.

### US-01.3 — Suspender o cancelar eventos

**Como** administrador, **quiero** suspender o cancelar cualquier evento, **para** intervenir ante una necesidad operativa.

**Criterios de aceptación:**

- Dado cualquier evento, cuando un administrador lo suspende, entonces queda en estado suspendido y no se permiten nuevas ventas.
- Dado cualquier evento, cuando un administrador lo cancela, entonces se inicia el flujo de cancelación y reintegros de F05.

**Observaciones:**

- La visibilidad del evento suspendido y el tratamiento de reservas o pagos que ya estaban en curso quedan pendientes.

---

## F02 — Búsqueda y ordenamiento de eventos

### US-02.1 — Buscar eventos

**Como** cliente, **quiero** buscar eventos con varios filtros y ordenarlos, **para** encontrar opciones relevantes.

**Criterios de aceptación:**

- Dado que hay eventos disponibles, cuando el cliente busca, entonces puede filtrar por tipo, organizador, participante, ciudad y rango de fechas.
- Dado un conjunto de resultados, cuando el cliente lo ordena, entonces puede elegir fecha, precio o relevancia.

**Observaciones:**

- Qué ciudad se usa para filtrar y cómo se calcula el precio y la relevancia de un evento requieren definición.

---

## F03 — Reserva, compra y emisión de entradas

### US-03.1 — Reservar lugares temporalmente

**Como** cliente, **quiero** elegir un sector y, si corresponde, asientos específicos, **para** reservar disponibilidad mientras pago.

**Criterios de aceptación:**

- Dado un asiento numerado disponible, cuando el cliente lo selecciona, entonces queda reservado temporalmente para esa compra.
- Dado un sector no numerado con capacidad disponible, cuando se reserva un lugar, entonces la reserva no excede la capacidad.
- Dado un asiento o el último lugar disponible, cuando dos clientes intentan reservarlo en simultáneo, entonces no se asigna a más de una reserva activa.

**Observaciones:**

- La duración exacta de la reserva está pendiente: el documento menciona diez minutos como ejemplo.

### US-03.2 — Pagar y recibir entradas

**Como** cliente, **quiero** completar el pago y recibir mis entradas, **para** acceder al evento.

**Criterios de aceptación:**

- Dado un pago aprobado, cuando termina el procesamiento, entonces se emiten entradas con códigos QR únicos.
- Dada una compra, cuando se procesa, entonces se registra el detalle de la compra y el medio de pago.
- Dado un cliente identificado por idUsuario, cuando ya tiene una entrada para ese evento, entonces no puede comprar otra para el mismo evento.

**Regla de negocio:**

- Límite confirmado: una entrada por cliente y evento.

### US-03.3 — Liberar disponibilidad y evitar ventas duplicadas

**Como** cliente, **quiero** que un rechazo, vencimiento o reintento no cause inconsistencias, **para** que las entradas y los pagos queden correctamente registrados.

**Criterios de aceptación:**

- Dado un pago rechazado o una reserva vencida, cuando se procesa el resultado, entonces se libera el lugar reservado.
- Dado un reintento de pago por un problema de conexión, cuando se vuelve a procesar, entonces no se vende dos veces la misma entrada.
- Dado varios clientes que compiten por un mismo asiento o el último lugar de una zona, cuando compran simultáneamente, entonces la entrada no se vende más de una vez.

---

## F04 — Fila virtual para eventos de alta demanda

### US-04.1 — Participar en la fila de compra

**Como** cliente, **quiero** conocer mi posición en la fila y recibir acceso a la compra por turnos, **para** participar en la venta de un evento de alta demanda.

**Criterios de aceptación:**

- Dado un evento marcado como de alta demanda, cuando se abre la venta, entonces el cliente ingresa a una fila virtual y recibe su posición.
- Dada una fila activa, cuando se habilita la compra, entonces los clientes avanzan en orden y por tandas.

**Observaciones:**

- El criterio técnico de orden, el tamaño de las tandas y las reglas ante abandono o expiración están por definir.

---

## F05 — Devoluciones y cancelaciones

### US-05.1 — Solicitar y resolver una devolución

**Como** cliente, **quiero** solicitar la devolución de una entrada dentro del plazo permitido, **para** pedir el reintegro correspondiente.

**Criterios de aceptación:**

- Dado un evento que comienza dentro de más de 72 horas, cuando el cliente solicita una devolución, entonces la solicitud puede ser presentada.
- Dada una solicitud, cuando un administrador la aprueba o rechaza, entonces queda registrada la decisión.
- Dada una solicitud aprobada, cuando se procesa, entonces el lugar se libera y se solicita el reintegro a la pasarela de pagos.

**Observaciones:**

- La interpretación exacta del límite de 72 horas y el estado de la entrada tras un rechazo requieren definición.

### US-05.2 — Gestionar la cancelación de un evento

**Como** administrador, **quiero** que la cancelación de un evento contemple todas sus entradas vendidas, **para** iniciar el reintegro y avisar a los compradores.

**Criterios de aceptación:**

- Dado un evento cancelado con entradas vendidas, cuando se procesa la cancelación, entonces todas esas entradas pasan al flujo de reintegro.
- Dado que existen compradores afectados, cuando se procesa la cancelación, entonces se los notifica.

**Observaciones:**

- No se especifica cómo se manejan los reintegros fallidos ni si la solicitud a la pasarela se reintenta automáticamente.

---

## F06 — Validación de entradas en el acceso

### US-06.1 — Validar y consumir un QR

**Como** personal de acceso, **quiero** escanear el QR de una entrada, **para** validar el ingreso al evento.

**Criterios de aceptación:**

- Dado un QR existente, correspondiente al evento y todavía no utilizado, cuando se escanea, entonces la entrada se valida y se marca como utilizada.
- Dado un QR inexistente, de otro evento o ya utilizado, cuando se escanea, entonces no se habilita un nuevo ingreso.
- Dado que dos lectores intentan validar el mismo QR, cuando las validaciones ocurren concurrentemente, entonces no se permite usarlo dos veces.

---

## F07 — Preferencias y recomendaciones

### US-07.1 — Registrar preferencias del cliente

**Como** cliente, **quiero** registrar mis preferencias de tipos de eventos, artistas favoritos y ciudad, **para** recibir una oferta afín a mis intereses.

**Criterios de aceptación:**

- Dado un cliente, cuando registra preferencias, entonces el sistema conserva esos tipos, artistas y ciudad.

### US-07.2 — Recibir recomendaciones

**Como** cliente, **quiero** recibir sugerencias basadas en mis preferencias e historial, **para** descubrir eventos de mi interés.

**Criterios de aceptación:**

- Dado un historial de compras, cuando se generan recomendaciones, entonces estas consideran ese historial.

**Observaciones:**

- El documento menciona como referencia «quienes compraron para este evento también compraron para…», pero no define algoritmo, ponderaciones ni comportamiento para clientes sin historial.

---

## F08 — Notificaciones

### US-08.1 — Recibir confirmaciones y avisos de cambios

**Como** cliente comprador, **quiero** recibir notificaciones sobre mis compras y eventos, **para** estar informado.

**Criterios de aceptación:**

- Dado un pago confirmado, cuando se emite la entrada, entonces se envía una notificación de compra confirmada.
- Dado un evento cancelado o modificado, cuando se registra el cambio, entonces se notifica a los compradores afectados.

**Observaciones:**

- El documento contempla canales email o push; no define preferencias de canal, contenido ni reglas de reintento.

### US-08.2 — Recibir avisos de artistas favoritos

**Como** cliente, **quiero** que me avisen cuando un artista favorito publique un evento en mi ciudad, **para** enterarme de esa oportunidad.

**Criterios de aceptación:**

- Dado que un cliente tiene un artista favorito y una ciudad registrada, cuando ese artista publica un evento en esa ciudad, entonces se envía una notificación.

---

## F09 — Reportes de eventos

### US-09.1 — Consultar resultados del evento

**Como** organizador, **quiero** consultar reportes de mis eventos, **para** conocer ventas, devoluciones, ocupación y recaudación.

**Criterios de aceptación:**

- Dado un evento, cuando el organizador consulta su reporte, entonces puede consultar entradas vendidas y devueltas por sector, porcentaje de ocupación y recaudación.

# Trazabilidad de Requisitos

| N.º | Requisito original | Feature / Historias que lo cubren |
|---:|---|---|
| 1 | Datos y estados del evento | F01 / US-01.1, US-01.3 |
| 2 | Lugares, sectores, capacidad y precios | F01 / US-01.2 |
| 3 | Creación, publicación, suspensión y cancelación | F01 / US-01.1, US-01.3 |
| 4 | Selección, reserva, compra, emisión, detalle, medio de pago y límite | F03 / US-03.1, US-03.2, US-03.3 |
| 5 | No vender dos veces; concurrencia y reintentos | F03 / US-03.1, US-03.3 |
| 6 | Fila virtual para alta demanda | F04 / US-04.1 |
| 7 | Devoluciones, reintegros, liberación y cancelaciones | F05 / US-05.1, US-05.2 |
| 8 | Validación y uso único del QR | F06 / US-06.1 |
| 9 | Búsqueda, filtros y ordenamiento | F02 / US-02.1 |
| 10 | Preferencias y recomendaciones basadas en historial | F07 / US-07.1, US-07.2 |
| 11 | Notificaciones de compra, cambios y artistas favoritos | F08 / US-08.1, US-08.2 |
| 12 | Reportes por sector | F09 / US-09.1 |
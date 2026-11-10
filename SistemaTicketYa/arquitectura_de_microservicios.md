# Arquitectura de Microservicios — TicketYa

El presente documento describe la arquitectura de microservicios propuesta para la plataforma **TicketYa**, detallando las responsabilidades de cada componente y los patrones de integración utilizados para cumplir con los requerimientos funcionales y no funcionales del sistema.

---

## 1. API Gateway
* **Punto de Entrada Único:** Actúa como la única interfaz de acceso expuesta hacia clientes externos (App Web/Móvil, Portal de Organizadores, Backoffice Administrativo y Lectores QR de Acceso).
* **Enrutamiento y Seguridad:** Enruta el tráfico hacia los microservicios correspondientes y centraliza la autenticación mediante la validación de tokens JWT y roles de usuario (Cliente, Organizador, Administrador, Personal de Acceso).

---

## 2. Microservicio de Usuario (`Usuarios`)
* **Gestión de Perfiles y Roles:** Administra el registro y la actualización de datos de los distintos roles del sistema.
* **Preferencias:** Almacena la información de preferencias de los clientes (tipos de evento preferidos, artistas favoritos y ciudad de residencia) para alimentar el sistema de notificaciones y recomendaciones.

---

## 3. Microservicio de Evento (`Eventos`)
* **Catálogo de Eventos:** Gestiona el ciclo de vida de los eventos (creación en borrador, publicación, suspensión, cancelación o finalización) y sus metadatos (nombre, descripción, tipo, fecha/hora, organizador y participantes).
* **Gestión de Lugares y Sectores:** Administra estadios, teatros o recintos, definiendo sectores numerados (filas y asientos) o no numerados (zonas con capacidad máxima como "Campo") y la configuración de precios por sector.
* **Búsquedas y Reportes:** Permite realizar búsquedas avanzadas por múltiples criterios (categoría, artista, ciudad, rango de fechas) y compila datos de recaudación, porcentaje de ocupación y ventas por sector para los organizadores.

---

## 4. Microservicio de Fila Virtual (`Fila Virtual`)
* **Gestión de Alta Demanda:** Absorbe el volumen masivo de tráfico al abrirse la venta de eventos populares o convocantes.
* **Control de Turnos:** Asigna posiciones en la fila virtual, notifica el estado del turno al usuario y autoriza el acceso a la compra en tandas de manera ordenada.

---

## 5. Microservicio de Reserva (`Reservas`)
* **Bloqueos Temporales:** Controla el bloqueo temporal (ej. 10 minutos) de asientos numerados específicos o cupos en zonas generales durante el proceso de compra.
* **Prevención de Doble Venta:** Implementa mecanismos de concurrencia e idempotencia para asegurar que un mismo lugar jamás se venda dos veces.
* **Liberación Automática:** Libera los lugares asignados de forma inmediata si la reserva expira o el pago es rechazado.

---

## 6. Microservicio de Órdenes (`Ordenes`)
* **Orquestación de Compras:** Actúa como el coordinador transaccional principal de la compra (ejecutando patrones de consistencia entre Reserva, Pago y Entrada).
* **Validaciones de Negocio:** Verifica y aplica los límites máximos de entradas permitidas por cliente para un mismo evento.
* **Registro de Transacciones:** Garantiza el registro detallado y el estado del ciclo de vida de cada intento de compra.

---

## 7. Microservicio de Pago (`Pagos`)
* **Integración con Pasarela de Pagos:** Procesa las transacciones económicas comunicándose con el proveedor externo (o su mock correspondiente).
* **Gestión de Reintegros:** Procesa devoluciones y solicitudes de reembolso ante devoluciones aprobadas o cancelaciones totales del evento.

---

## 8. Microservicio de Entrada (`Entradas`)
* **Emisión de Tickets:** Genera las entradas definitivas y emite el código QR único tras la confirmación exitosa del pago.
* **Control de Acceso:** Valida la validez de las entradas mediante escaneo QR por el personal de acceso (comprobando existencia, evento correcto y estado) y las marca como utilizadas para evitar reutilización.
* **Solicitudes de Devolución:** Gestiona las solicitudes de devolución hechas por clientes (hasta 72 horas antes) y procesa las aprobaciones/rechazos administrativos.

---

## 9. Microservicio de Notificaciones (`Notificaciones`)
* **Comunicación Asíncrona:** Escucha eventos del sistema (mensajería / *fire-and-forget*) para enviar alertas sin impactar los tiempos de respuesta del flujo principal.
* **Canales de Alerta:** Envía correos electrónicos o notificaciones *push* para confirmaciones de compra, cancelaciones o reprogramaciones de eventos, y alertas sobre publicaciones de eventos de artistas favoritos en la ciudad del usuario.
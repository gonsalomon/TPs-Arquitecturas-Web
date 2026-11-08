FUNCIONALIDAD
# Tareas
4. Un cliente elige un evento, un sector y, si es numerado, asientos específicos. El sistema
   reserva temporalmente esos lugares (por ej. por 10 minutos) mientras el cliente paga. Si
   el pago es aprobado, se emiten las entradas con un código QR único; si el pago es
   rechazado o la reserva vence, los lugares se liberan. Se debe registrar el detalle de la
   compra y el medio de pago, y limitar la cantidad de entradas por cliente para un mismo
   evento.
5. Una entrada nunca puede venderse dos veces, aun cuando muchos clientes intenten
   comprar el mismo asiento (o el último lugar de una zona) al mismo tiempo, o cuando un
   cliente reintente el pago por un problema de conexión.
7. Un cliente puede solicitar la devolución de una entrada hasta 72 horas antes del evento.
   Un administrador aprueba o rechaza la solicitud; si la aprueba, el asiento o lugar en la zona
   se libera y se solicita el reintegro del dinero a la pasarela de pagos. Si un evento se cancela,
   todas sus entradas vendidas pasan a reintegro y se notifica a los compradores.

# Done 
- 1. Cada evento tiene: ID, nombre, descripción, tipo (recital, teatro, deporte, conferencia,
     etc.), organizador, participantes (artistas, equipos, disertantes), fecha y hora, lugar, y
     estado (borrador, publicado, agotado, cancelado, finalizado).
- 2. Cada lugar (estadio, teatro, sala) tiene: ID, nombre, dirección, ubicación (latitud/longitud)
      y un conjunto de sectores. Un sector puede ser numerado (filas y asientos identificados)
      o no numerado (zona con una capacidad máxima, p. ej. “Campo”). Para cada evento, el
      organizador define el precio de cada sector.
- 3. Los organizadores crean sus eventos en estado borrador, configuran sectores y precios, y
   luego los publican. Los administradores pueden suspender o cancelar cualquier evento.
- 9. El sistema debe permitir búsquedas por múltiples criterios: tipo de evento, organizador,
   participante, ciudad y rango de fechas, con ordenamiento por fecha, precio o relevancia

tareas micro servicio usuario
- CRUD usuario 

tareas micro servicio evento
- Entidad lugar y CRUD (establecer nombre como unico para la busqueda)
- Entidad evento y CRUD (Este depende del lugar, primero debo crear el lugar)
estado (borrador, publicado, agotado, cancelado, finalizado) ENUM

tareas micro servicio pago
- creacion del pago, devolver un mock de pago (en request recibir id usuario y para ciertos id rechazar pagos)
- devolucion pago

tareas micro servicio notificacio
- creacion notificacion, devolver mock de que fue notificado

tareas micro servicio entrada
- CRUD Entrada

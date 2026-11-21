FUNCIONALIDAD
# Tareas
6. Para eventos marcados como de alta demanda, al abrirse la venta los clientes ingresan a
   una fila virtual: el sistema les informa su posición y los habilita a comprar en orden, por
   tandas.

10. El sistema debe registrar las preferencias de los clientes (tipos de evento, artistas
    favoritos, ciudad) y sugerir eventos en función de su historial (similar a “quienes
    compraron entradas para este evento también compraron para…”).
11. Enviar notificaciones(email o push) al confirmar una compra, ante la cancelación o cambio
    de un evento, y cuando un artista favorito de un cliente publica un evento en su ciudad.
12. Los organizadores deben poder consultar reportes de sus eventos: entradas vendidas y
    devueltas por sector, porcentaje de ocupación y recaudación.
# Done
8. En el ingreso al evento, el personal de acceso escanea el QR. El sistema valida que la
   entrada exista, corresponda a ese evento y no haya sido usada, y la marca como utilizada
   (un QR no puede ingresar dos veces).
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

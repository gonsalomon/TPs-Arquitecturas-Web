package org.example.orden.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Traduce las respuestas HTTP de error de los servicios remotos (Evento, Reserva, Pago, Entrada)
 * a las excepciones que entiende Orden, siguiendo la convención del equipo:
 * 400 -> IllegalArgumentException, 404 -> ResponseStatusException, 409 -> IllegalStateException.
 * Cualquier otro código (5xx, etc.) se trata como servicio no disponible; los errores de
 * conexión (sin respuesta HTTP) no pasan por acá, los maneja quien llama al cliente Feign.
 */
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        String mensaje = "Error al llamar a " + methodKey + " (status " + response.status() + ")";

        return switch (response.status()) {
            case 400 -> new IllegalArgumentException(mensaje);
            case 404 -> new ResponseStatusException(HttpStatus.NOT_FOUND, mensaje);
            case 409 -> new IllegalStateException(mensaje);
            default -> new ServicioNoDisponibleException(mensaje);
        };
    }
}

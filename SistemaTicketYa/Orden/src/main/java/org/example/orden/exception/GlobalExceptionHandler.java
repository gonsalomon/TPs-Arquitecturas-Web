package org.example.orden.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MissingRequestHeaderException.class, MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleSolicitudInvalida(Exception ex) {
        return cuerpo(HttpStatus.BAD_REQUEST, "Solicitud inválida", mensajeDe(ex));
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, Object>> handleAccesoDenegado(AccesoDenegadoException ex) {
        return cuerpo(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler({EventoNoDisponibleException.class, AsientoNoDisponibleException.class,
            OrdenVencidaException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> handleConflicto(RuntimeException ex) {
        return cuerpo(HttpStatus.CONFLICT, "Conflicto en la solicitud", ex.getMessage());
    }

    @ExceptionHandler(LimiteEntradasExcedidoException.class)
    public ResponseEntity<Map<String, Object>> handleLimiteExcedido(LimiteEntradasExcedidoException ex) {
        return cuerpo(HttpStatus.UNPROCESSABLE_ENTITY, "Límite excedido", ex.getMessage());
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> handleServicioNoDisponible(ServicioNoDisponibleException ex) {
        return cuerpo(HttpStatus.SERVICE_UNAVAILABLE, "Servicio no disponible", ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus estado = HttpStatus.valueOf(ex.getStatusCode().value());
        return cuerpo(estado, describir(estado), ex.getReason());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return cuerpo(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", ex.getMessage());
    }

    private String mensajeDe(Exception ex) {
        if (ex instanceof MissingRequestHeaderException e) {
            return "Falta el header obligatorio " + e.getHeaderName();
        }
        if (ex instanceof MethodArgumentNotValidException e) {
            return e.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(f -> f.getField() + ": " + f.getDefaultMessage())
                    .orElse("Solicitud inválida");
        }
        return ex.getMessage();
    }

    private String describir(HttpStatus estado) {
        return switch (estado) {
            case NOT_FOUND -> "No existe la entidad solicitada";
            case BAD_REQUEST -> "Solicitud inválida";
            case CONFLICT -> "Conflicto en la solicitud";
            case FORBIDDEN -> "Acceso denegado";
            default -> estado.getReasonPhrase();
        };
    }

    private ResponseEntity<Map<String, Object>> cuerpo(HttpStatus estado, String error, String mensaje) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", error);
        body.put("message", mensaje);
        return ResponseEntity.status(estado).body(body);
    }
}

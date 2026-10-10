package org.example.ejercicioIntegrador3.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Maneja errores genéricos -> devuelve 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", "Error interno del servidor");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // Maneja errores de datos inválidos (IllegalArgumentException) -> 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", "Solicitud inválida");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Maneja errores de duplicado (IllegalStateException, por ejemplo) -> Para duplicados 409 Conflict
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", "Conflicto en la solicitud");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // Maneja violación en integración de datos -> 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", "Conflicto en la solicitud (duplicado o violación de integridad de datos)");
        body.put("message", "No se pudo completar la operación. Es posible que el registro ya exista o que esté violando una restricción de integridad en la base de datos.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /* Respeta el estado que trae la excepción en vez de responder siempre 404.
     * Hoy todas las ResponseStatusException del proyecto son NOT_FOUND, pero si
     * mañana alguien lanza una con 400 o 409, el cliente recibe ese código y no
     * un 404 equivocado. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus estado = HttpStatus.valueOf(ex.getStatusCode().value());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", describir(estado));
        body.put("message", ex.getReason());
        return ResponseEntity.status(estado).body(body);
    }

    // Texto del campo "error" según el estado, para no perder los mensajes en castellano.
    private String describir(HttpStatus estado) {
        switch (estado) {
            case NOT_FOUND:
                return "No existe la entidad solicitada";
            case BAD_REQUEST:
                return "Solicitud inválida";
            case CONFLICT:
                return "Conflicto en la solicitud";
            default:
                return estado.getReasonPhrase();
        }
    }

}

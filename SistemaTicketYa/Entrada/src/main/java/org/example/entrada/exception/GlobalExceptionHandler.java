package org.example.entrada.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    // 500: log the real cause, never send it to the client
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", "Ocurrió un error inesperado");
    }

    // ---- 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", detalle);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", "Cuerpo de la solicitud inválido");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", "Parámetro inválido: " + ex.getName());
    }

    // ---- 409
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return build(HttpStatus.CONFLICT, "Conflicto en la solicitud", ex.getMessage());
    }

    @ExceptionHandler(AsientoNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> handleAsiento(AsientoNoDisponibleException ex) {
        return build(HttpStatus.CONFLICT, "Asiento no disponible", ex.getMessage());
    }

    // backstop for the unique constraints when the violation isn't caught in the service
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return build(HttpStatus.CONFLICT, "Conflicto en la solicitud (duplicado o violación de integridad de datos)",
                "No se pudo completar la operación. Es posible que el registro ya exista.");
    }

    // two concurrent changes on the same ticket (@Version)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        log.warn("Optimistic lock failure", ex);
        return build(HttpStatus.CONFLICT, "Conflicto en la solicitud", "La entrada fue modificada por otra operación, reintente");
    }

    // ---- 422: valid request, a business rule rejects it
    @ExceptionHandler(LimiteEntradasExcedidoException.class)
    public ResponseEntity<Map<String, Object>> handleLimite(LimiteEntradasExcedidoException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Límite de entradas excedido", ex.getMessage());
    }

    @ExceptionHandler(DevolucionFueraDePlazoException.class)
    public ResponseEntity<Map<String, Object>> handlePlazo(DevolucionFueraDePlazoException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Devolución fuera de plazo", ex.getMessage());
    }

    // keeps the status carried by the exception
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus estado = HttpStatus.valueOf(ex.getStatusCode().value());
        return build(estado, describir(estado), ex.getReason());
    }

    private String describir(HttpStatus estado) {
        switch (estado) {
            case NOT_FOUND:   return "No existe la entidad solicitada";
            case BAD_REQUEST: return "Solicitud inválida";
            case CONFLICT:    return "Conflicto en la solicitud";
            default:          return estado.getReasonPhrase();
        }
    }
}
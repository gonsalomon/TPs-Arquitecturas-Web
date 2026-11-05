package org.example.pago.controller;
import org.example.pago.dto.request.PagoRequestDTO;
import org.example.pago.dto.response.PagoResponseDTO;
import org.example.pago.service.PagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    public ResponseEntity<List<PagoResponseDTO>> obtenerTodasLasReservas() {
        List<PagoResponseDTO> pagos = pagoService.obtenerTodosLosPagos();
        return ResponseEntity.ok(pagos);
    }

    @PostMapping
    public ResponseEntity<?> realizarPago(@RequestBody PagoRequestDTO request) {
        try {
            PagoResponseDTO response = pagoService.realizarPago(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            // Captura errores de estado (ej. reserva vencida o límite excedido en concurrencia)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Captura errores de datos (ej. reserva no encontrada)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el pago");
        }
    }

    @PostMapping("/reserva/{idReserva}/reembolso")
    public ResponseEntity<PagoResponseDTO> reembolsarPago(@PathVariable Long idReserva) {
        PagoResponseDTO response = pagoService.procesarReembolso(idReserva);
        return ResponseEntity.ok(response);
    }
}
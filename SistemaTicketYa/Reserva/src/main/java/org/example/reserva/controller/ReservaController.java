package org.example.reserva.controller;

import org.example.reserva.dto.request.ReservaRequestDTO;
import org.example.reserva.dto.response.ReservaResponseDTO;
import org.example.reserva.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> obtenerTodasLasReservas() {
        List<ReservaResponseDTO> reservas = reservaService.obtenerTodasLasReservas();
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> obtenerReservaPorId(@PathVariable Long id) {
        // Si tu ID es String, cambia Long por String en los parámetros
        ReservaResponseDTO reserva = reservaService.obtenerReservaPorId(id);
        return ResponseEntity.ok(reserva);
    }

    @PostMapping
    public ResponseEntity<?> crearReserva(@RequestBody ReservaRequestDTO request) {
        try {
            ReservaResponseDTO response = reservaService.crearReserva(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno: " + e.getMessage());
        }
    }

    @GetMapping("/cliente/{idCliente}/evento/{idEvento}/confirmadas")
    public ResponseEntity<List<ReservaResponseDTO>> obtenerReservasConfirmadas(
            @PathVariable Long idCliente,
            @PathVariable String idEvento) {
        List<ReservaResponseDTO> reservas = reservaService.obtenerReservasConfirmadas(idCliente, idEvento);
        return ResponseEntity.ok(reservas);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> actualizarEstadoReserva(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {

        reservaService.actualizarEstado(id, nuevoEstado);
        return ResponseEntity.ok().build();
    }
}

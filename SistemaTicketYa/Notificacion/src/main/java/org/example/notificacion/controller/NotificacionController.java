package org.example.notificacion.controller;

import lombok.AllArgsConstructor;
import org.example.notificacion.dto.NotificacionRequestDTO;
import org.example.notificacion.dto.NotificacionResponseDTO;
import org.example.notificacion.dto.UsuarioDTO;
import org.example.notificacion.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {
    private final NotificacionService notificacionService;

    @PostMapping("/notificar")
    public ResponseEntity<NotificacionResponseDTO> notificar(@RequestBody NotificacionRequestDTO notificacionDTO) {
        NotificacionResponseDTO notificacion = notificacionService.notificar(notificacionDTO);
        return ResponseEntity.ok(notificacion);
    }
    @GetMapping("")
    public ResponseEntity<List<NotificacionResponseDTO>> getAll(){
        List<NotificacionResponseDTO> notificaciones = notificacionService.findAll();
        return ResponseEntity.ok(notificaciones);
    }
    @GetMapping("/canceladas")
    public ResponseEntity<List<UsuarioDTO>> getAllCanceladas() {
        List<UsuarioDTO> notificaciones = notificacionService.findAllCanceladas();
        return ResponseEntity.ok(notificaciones);
    }
}

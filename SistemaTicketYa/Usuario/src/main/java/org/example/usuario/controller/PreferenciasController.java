package org.example.usuario.controller;

import lombok.RequiredArgsConstructor;
import org.example.usuario.dto.response.PreferenciasDTO;
import org.example.usuario.service.PreferenciasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class PreferenciasController {

    private final PreferenciasService preferenciasService;

    @PutMapping("/{usuarioId}/preferencias")
    public ResponseEntity<PreferenciasDTO> guardarPreferencias(
            @PathVariable Long usuarioId,
            @RequestBody PreferenciasDTO dto) {
        return ResponseEntity.ok(preferenciasService.guardarOActualizar(usuarioId, dto));
    }

    @GetMapping("/{usuarioId}/preferencias")
    public ResponseEntity<PreferenciasDTO> obtenerPreferencias(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(preferenciasService.obtenerPorUsuario(usuarioId));
    }
}

package org.example.evento.client;

import org.example.evento.client.usuario.dto.response.NotificacionArtistaDTO;
import org.example.evento.client.usuario.dto.response.UsuarioResponseDTO;
import org.example.evento.dto.request.PreferenciasExternasDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@FeignClient(name = "usuario-service", url = "http://localhost:8081/api/usuarios")
public interface UsuarioFeignClient {

    @GetMapping ("/{id}")
    ResponseEntity<UsuarioResponseDTO> getById(@PathVariable("id") Long id);

    @GetMapping("/notificaciones/artistas")
    List<NotificacionArtistaDTO> buscarDestinatarios(
            @RequestParam Set<String> artistas,
            @RequestParam String ciudad
    );

    @GetMapping("/api/usuarios/{usuarioId}/preferencias")
    PreferenciasExternasDTO obtenerPreferencias(@PathVariable("usuarioId") Long usuarioId);
}

package org.example.evento.client;

import org.example.evento.client.usuario.dto.response.UsuarioResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "usuario-service", url = "http://localhost:8081/api/usuarios")
public interface UsuarioFeingClient {

    @GetMapping ("/{id}")
    ResponseEntity<UsuarioResponseDTO> getById(@PathVariable("id") Long id);
}

package org.example.entrada.client;

import org.example.entrada.client.usuario.response.dto.UsuarioResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "usuario-service", url = "http://localhost:8081/api/usuarios")
public interface UsuarioFeignClient {
    @GetMapping("/{id}")
    ResponseEntity<UsuarioResponseDTO> getById(@PathVariable("id") Long id);
}

package org.example.gateway.client;

import org.example.gateway.client.dto.user.UsuarioTokenResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "usuario-client", url = "${USUARIO_URL:http://localhost:8081}/api/auth")
public interface UsuarioClient {

    @GetMapping("/{email}")
    Optional<UsuarioTokenResponseDTO> findOneWithAuthoritiesByEmailIgnoreCase(@PathVariable String email);
}
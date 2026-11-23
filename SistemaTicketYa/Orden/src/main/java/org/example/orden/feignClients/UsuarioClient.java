package org.example.orden.feignClients;

import org.example.orden.dto.response.UsuarioResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name ="usuario-client", url = "${USUARIO_URL:http://localhost:8081}/api/usuarios")
public interface UsuarioClient {
    @GetMapping("/{idUsuario}")
    UsuarioResponseDTO getById(@PathVariable Long idUsuario);
}

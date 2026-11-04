package org.example.reserva.feignClients;
import org.example.reserva.dto.request.UsuarioExternoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Cliente para el microservicio de Usuarios
@FeignClient(name = "usuario-client", url = "http://localhost:8081/api/usuarios", primary = false)
public interface UsuarioClient {
    @GetMapping("/{id}")
    UsuarioExternoDTO obtenerUsuario(@PathVariable("id") Long id);
}

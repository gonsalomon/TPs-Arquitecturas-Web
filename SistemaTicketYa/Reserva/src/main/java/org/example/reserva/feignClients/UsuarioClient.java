package org.example.reserva.feignClients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "usuario-service", url = "http://localhost:8081/api/usuarios")
public interface UsuarioClient {

    @GetMapping("/{id}")
    ResponseEntity<Object> obtenerUsuarioPorId(@PathVariable("id") Long id);
}

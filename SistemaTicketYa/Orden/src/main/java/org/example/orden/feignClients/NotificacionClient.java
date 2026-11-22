package org.example.orden.feignClients;

import org.example.orden.dto.request.NotificacionRequestDTO;
import org.example.orden.dto.response.NotificacionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notificacion-client", url = "http://localhost:8086/api/notificaciones")
public interface NotificacionClient {
    @PostMapping("/notificar")
    NotificacionResponseDTO notificar(@RequestBody NotificacionRequestDTO notificacionDTO);
}

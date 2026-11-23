package org.example.evento.client;

import org.example.evento.client.notificacion.dto.NotificacionRequestDTO;
import org.example.evento.client.notificacion.dto.NotificacionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notificacion-client", url = "${NOTIFICACION_URL:http://localhost:8086}/api/notificaciones")
public interface NotificacionFeignClient {
    @PostMapping("/notificar")
    NotificacionResponseDTO notificar(@RequestBody NotificacionRequestDTO notificacionDTO);
}

package org.example.notificacion.service;

import lombok.AllArgsConstructor;
import org.example.notificacion.dto.NotificacionRequestDTO;
import org.example.notificacion.dto.NotificacionResponseDTO;
import org.example.notificacion.entity.Notificacion;
import org.example.notificacion.repository.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class NotificacionService {
    private final NotificacionRepository notificacionRepository;

    @Transactional
    public NotificacionResponseDTO notificar(NotificacionRequestDTO notificacionDTO) {
        Notificacion notificacion = new Notificacion(
                null,
                notificacionDTO.getTipo(),
                notificacionDTO.getCanal(),
                notificacionDTO.getEmail(),
                "ENVIADO"
        );
        Notificacion guardada = notificacionRepository.save(notificacion);
        return new NotificacionResponseDTO(
                guardada.getId(),
                guardada.getTipo(),
                guardada.getCanal(),
                guardada.getEmail(),
                guardada.getEstado()
        );
    }
}
/* private NotificacionTipo tipo;
    private NotificacionCanal canal;
    private String email;
    private Long clienteId;
    private Long eventoId;
    private String ciudad;

    private Long id;
    private NotificacionTipo tipo;
    private NotificacionCanal canal;
    private String email;
    private String mensaje;
    private String estado;*/

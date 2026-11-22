package org.example.notificacion.service;

import lombok.AllArgsConstructor;
import org.example.notificacion.dto.NotificacionRequestDTO;
import org.example.notificacion.dto.NotificacionResponseDTO;
import org.example.notificacion.dto.UsuarioDTO;
import org.example.notificacion.entity.Notificacion;
import org.example.notificacion.repository.NotificacionRepository;
import org.example.notificacion.utils.NotificacionTipo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class NotificacionService {
    private final NotificacionRepository notificacionRepository;
    private final EmailService emailService;

    @Transactional
    public NotificacionResponseDTO notificar(NotificacionRequestDTO notificacionDTO) {
        if (notificacionDTO.getTipo() == NotificacionTipo.MODIFICACION_EVENTO) {
            emailService.enviarModificacion(
                    notificacionDTO.getEmail(),
                    notificacionDTO.getFechaAnterior(),
                    notificacionDTO.getFechaNueva()
            );
        }else if (notificacionDTO.getTipo() == NotificacionTipo.CANCELACION_EVENTO) {
            emailService.enviarCancelacion(notificacionDTO.getEmail());
        }else if (notificacionDTO.getTipo() == NotificacionTipo.CONFIRMACION_COMPRA) {
            emailService.enviarConfirmacionOrden(notificacionDTO.getEmail());
        }
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

    @Transactional
    public List<NotificacionResponseDTO> findAll(){
        return notificacionRepository.findAll()
                .stream()
                .map(notificacion -> new NotificacionResponseDTO(
                        notificacion.getId(),
                        notificacion.getTipo(),
                        notificacion.getCanal(),
                        notificacion.getEmail(),
                        notificacion.getEstado()
                ))
                .toList();
    }

    @Transactional
    public List<UsuarioDTO> findAllCanceladas() {
        return notificacionRepository.findByTipo(NotificacionTipo.CANCELACION_EVENTO)
                .stream()
                .map(notificacion ->  new UsuarioDTO(
                    notificacion.getEmail()
                ))
                .toList();
    }
}
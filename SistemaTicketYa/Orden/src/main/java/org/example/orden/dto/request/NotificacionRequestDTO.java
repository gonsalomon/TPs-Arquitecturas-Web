package org.example.orden.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionRequestDTO {
    private String tipo;
    private String canal;
    private String email;
    private LocalDateTime fechaAnterior;
    private LocalDateTime fechaNueva;
}


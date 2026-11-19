package org.example.orden.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionRequestDTO {
    private String tipo;
    private String canal;
    private String email;
}


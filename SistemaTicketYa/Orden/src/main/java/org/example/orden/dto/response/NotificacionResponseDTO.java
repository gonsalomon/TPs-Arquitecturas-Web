package org.example.orden.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionResponseDTO {
    private Long id;
    private String tipo;
    private String canal;
    private String email;
    private String estado;

}
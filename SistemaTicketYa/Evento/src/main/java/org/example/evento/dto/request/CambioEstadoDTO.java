package org.example.evento.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.evento.utils.EstadoEvento;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CambioEstadoDTO {
    @NotNull(message = "El estado es obligatorio")
    private EstadoEvento estado;
}

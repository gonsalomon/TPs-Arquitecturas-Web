package org.example.evento.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.evento.utils.TipoEvento;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventoRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    private String descripcion;
    @NotNull(message = "El tipo es obligatorio")
    private TipoEvento tipo;
    @NotNull(message = "El organizador es obligatorio")
    private Long idOrganizador;
    private List<String> participantes;
    @NotNull(message = "La fecha y hora son obligatorias")
    @Future(message = "La fecha debe ser futura")
    private LocalDateTime fechaHora;
    @NotBlank(message = "El lugar es obligatorio")
    private String idLugar;
    private List<@Valid PrecioSectorDTO> precios;
}

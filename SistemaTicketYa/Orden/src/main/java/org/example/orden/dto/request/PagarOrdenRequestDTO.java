package org.example.orden.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.orden.utils.MedioPago;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagarOrdenRequestDTO {

    @NotNull(message = "El medio de pago es obligatorio")
    private MedioPago medioPago;
}

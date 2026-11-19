package org.example.fila_virtual.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidacionAccesoDTO {
    private boolean habilitado;
    private String motivo;
}

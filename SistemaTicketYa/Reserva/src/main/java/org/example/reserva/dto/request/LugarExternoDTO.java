package org.example.reserva.dto.request;

import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LugarExternoDTO {
    private String id;
    private List<SectorExternoDTO> sectores;
}

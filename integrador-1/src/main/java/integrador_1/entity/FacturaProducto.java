package integrador_1.entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class FacturaProducto {
    private Long idFactura;
    private Long idProducto;
    private Long cantidad;      
}
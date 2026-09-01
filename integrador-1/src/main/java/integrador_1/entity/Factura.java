package integrador_1.entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Factura {
    private Long idFactura;
    private Long idCliente;    
}
package integrador_1.entity;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Producto {
    private Long idProducto;
    private String nombre;
    private Float valor; 
}

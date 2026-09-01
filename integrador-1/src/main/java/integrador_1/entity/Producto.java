package integrador_1.entity;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Producto {
    private int idProducto;
    private String nombre;
    private double valor;
}

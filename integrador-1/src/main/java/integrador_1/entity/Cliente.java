package integrador_1.entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Cliente {
    private int idCliente;
    private String nombre;
    private String email;
}

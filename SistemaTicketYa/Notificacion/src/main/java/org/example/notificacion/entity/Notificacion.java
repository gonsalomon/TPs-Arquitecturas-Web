package org.example.notificacion.entity;

import jakarta.persistence.*;
import lombok.*;

import org.example.notificacion.utils.NotificacionTipo;
import org.example.notificacion.utils.NotificacionCanal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) //anotación de JPA/Hibernate que sirve para indicar cómo se debe guardar un enum en la base de datos.
    private NotificacionTipo tipo;

    @Enumerated(EnumType.STRING)
    private NotificacionCanal canal;

    private String email;

    private String estado;
}
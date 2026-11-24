package org.example.reserva.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "bloqueo_sector",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bloqueo_evento_sector",
                columnNames = {"id_evento", "sector"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BloqueoSector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_evento", nullable = false, length = 100)
    private String idEvento;

    @Column(name = "sector", nullable = false, length = 100)
    private String sector;
}

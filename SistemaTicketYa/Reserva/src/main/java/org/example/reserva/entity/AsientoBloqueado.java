package org.example.reserva.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "asiento_bloqueado",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_evento_sector_asiento",
                columnNames = {"id_evento", "sector", "asiento"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsientoBloqueado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_evento", nullable = false, length = 100)
    private String idEvento;

    @Column(name = "sector", nullable = false, length = 100)
    private String sector;

    @Column(name = "asiento", nullable = false)
    private Long asiento;

    @Column(name = "id_reserva", nullable = false)
    private Long idReserva;
}

package cl.duoc.esports.rankingservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "rankings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"torneoId", "participanteId"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ranking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long torneoId;

    @Column(nullable = false)
    private Long participanteId;

    @Column(nullable = false)
    private Integer puntos;

    @Column(nullable = false)
    private Integer victorias;

    @Column(nullable = false)
    private Integer derrotas;

    @Column(nullable = false)
    private Integer diferencia;

    @Column(nullable = false)
    private Integer posicion;
}
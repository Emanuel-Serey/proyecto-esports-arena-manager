package cl.duoc.esports.prizeservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "premios",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"torneoId", "participanteId", "posicion"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Premio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long torneoId;

    @Column(nullable = false)
    private Long participanteId;

    @Column(nullable = false)
    private Integer posicion;

    @Column(nullable = false)
    private String tipoPremio;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String estadoEntrega;

    @Column(nullable = false)
    private LocalDate fechaAsignacion;
}
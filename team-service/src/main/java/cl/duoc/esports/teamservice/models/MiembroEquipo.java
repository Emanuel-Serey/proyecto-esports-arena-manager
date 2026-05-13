package cl.duoc.esports.teamservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "miembros_equipo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiembroEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private String rolDentroEquipo;

    @ManyToOne
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;
}
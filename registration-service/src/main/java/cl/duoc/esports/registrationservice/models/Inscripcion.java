package cl.duoc.esports.registrationservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "inscripciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long torneoId;

    private Long equipoId;

    private Long jugadorId;

    @Column(nullable = false)
    private String tipoParticipante;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private LocalDate fechaInscripcion;
}
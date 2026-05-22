package cl.duoc.esports.resultservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "resultados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long partidaId;

    @Column(nullable = false)
    private Long ganadorId;

    @Column(nullable = false)
    private Integer puntajeA;

    @Column(nullable = false)
    private Integer puntajeB;

    @Column(nullable = false)
    private String estadoValidacion;

    @Column(nullable = false)
    private LocalDate fechaRegistro;
}
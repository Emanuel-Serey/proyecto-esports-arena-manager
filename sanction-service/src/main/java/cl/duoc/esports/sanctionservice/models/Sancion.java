package cl.duoc.esports.sanctionservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "sanciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sancion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usuarioId;

    private Long equipoId;

    @Column(nullable = false)
    private String motivo;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String severidad;
}

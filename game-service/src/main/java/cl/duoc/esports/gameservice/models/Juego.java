package cl.duoc.esports.gameservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name= "juegos")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Juego {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String genero;

    @Column(nullable = false)
    private String modalidad;

    @Column(nullable = false)
    private Integer jugadoresPorEquipo;

    @Column(nullable = false)
    private Boolean estado;
}

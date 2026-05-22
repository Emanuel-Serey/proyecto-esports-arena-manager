package cl.duoc.esports.resultservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoDTO {

    private Long id;

    @NotNull(message = "El ID de la partida es obligatorio")
    private Long partidaId;

    @NotNull(message = "El ID del ganador es obligatorio")
    private Long ganadorId;

    @NotNull(message = "El puntaje del participante A es obligatorio")
    @Min(value = 0, message = "El puntaje A no puede ser negativo")
    private Integer puntajeA;

    @NotNull(message = "El puntaje del participante B es obligatorio")
    @Min(value = 0, message = "El puntaje B no puede ser negativo")
    private Integer puntajeB;

    private String estadoValidacion;

    private LocalDate fechaRegistro;
}
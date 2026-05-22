package cl.duoc.esports.rankingservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingDTO {

    private Long id;

    @NotNull(message = "El ID del torneo es obligatorio")
    private Long torneoId;

    @NotNull(message = "El ID del participante es obligatorio")
    private Long participanteId;

    @NotNull(message = "Los puntos son obligatorios")
    @Min(value = 0, message = "Los puntos no pueden ser negativos")
    private Integer puntos;

    @NotNull(message = "Las victorias son obligatorias")
    @Min(value = 0, message = "Las victorias no pueden ser negativas")
    private Integer victorias;

    @NotNull(message = "Las derrotas son obligatorias")
    @Min(value = 0, message = "Las derrotas no pueden ser negativas")
    private Integer derrotas;

    private Integer diferencia;

    private Integer posicion;
}
package cl.duoc.esports.prizeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingDTO {

    private Long id;
    private Long torneoId;
    private Long participanteId;
    private Integer puntos;
    private Integer victorias;
    private Integer derrotas;
    private Integer diferencia;
    private Integer posicion;
}

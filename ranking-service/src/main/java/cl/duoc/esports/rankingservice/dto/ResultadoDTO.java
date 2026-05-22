package cl.duoc.esports.rankingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoDTO {

    private Long id;
    private Long partidaId;
    private Long ganadorId;
    private Integer puntajeA;
    private Integer puntajeB;
    private String estadoValidacion;
    private LocalDate fechaRegistro;
}
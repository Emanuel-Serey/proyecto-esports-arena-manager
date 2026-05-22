package cl.duoc.esports.resultservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartidaDTO {

    private Long id;
    private Long torneoId;
    private Long participanteAId;
    private Long participanteBId;
    private String ronda;
    private LocalDateTime fechaHora;
    private String estado;
}
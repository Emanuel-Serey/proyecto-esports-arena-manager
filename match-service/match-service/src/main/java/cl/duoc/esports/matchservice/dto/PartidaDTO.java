package cl.duoc.esports.matchservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartidaDTO {

    private Long id;

    @NotNull(message = "El ID del torneo es obligatorio")
    private Long torneoId;

    @NotNull(message = "El participante A es obligatorio")
    private Long participanteAId;

    @NotNull(message = "El participante B es obligatorio")
    private Long participanteBId;

    @NotBlank(message = "La ronda es obligatoria")
    private String ronda;

    @NotNull(message = "La fecha y hora de la partida es obligatoria")
    private LocalDateTime fechaHora;

    private String estado;
}
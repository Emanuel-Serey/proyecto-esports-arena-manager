package cl.duoc.esports.registrationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionDTO {

    private Long id;

    @NotNull(message = "El ID del torneo es obligatorio")
    private Long torneoId;

    private Long equipoId;

    private Long jugadorId;

    @NotBlank(message = "El tipo de participante es obligatorio")
    private String tipoParticipante;

    private String estado;

    private LocalDate fechaInscripcion;
}
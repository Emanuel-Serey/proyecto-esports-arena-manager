package cl.duoc.esports.sanctionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class SancionDTO {

    private Long id;

    private Long usuarioId;

    private Long equipoId;

    @NotBlank(message = "El motivo de la sanción es obligatorio")
    private String motivo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    private String estado;

    @NotBlank(message = "La severidad de la sanción es obligatoria")
    private String severidad;
}

package cl.duoc.esports.prizeservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PremioDTO {

    private Long id;

    @NotNull(message = "El ID del torneo es obligatorio")
    private Long torneoId;

    @NotNull(message = "El ID del participante es obligatorio")
    private Long participanteId;

    @NotNull(message = "La posición es obligatoria")
    @Min(value = 1, message = "La posición debe ser mayor o igual a 1")
    private Integer posicion;

    @NotBlank(message = "El tipo de premio es obligatorio")
    private String tipoPremio;

    @NotBlank(message = "La descripción del premio es obligatoria")
    private String descripcion;

    private String estadoEntrega;

    private LocalDate fechaAsignacion;
}
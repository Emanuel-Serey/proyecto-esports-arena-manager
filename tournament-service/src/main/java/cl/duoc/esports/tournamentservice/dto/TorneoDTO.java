package cl.duoc.esports.tournamentservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
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
public class TorneoDTO {

    private Long id;

    @NotBlank(message = "El nombre del torneo es obligatorio")
    private String nombre;

    @NotNull(message = "El ID del juego es obligatorio")
    private Long juegoId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser actual o futura")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotNull(message = "El cupo máximo es obligatorio")
    @Min(value = 1, message = "El cupo máximo debe ser mayor a cero")
    private Integer cupoMaximo;

    private String estado;

    @NotBlank(message = "La modalidad del torneo es obligatoria")
    private String modalidad;
}
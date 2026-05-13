package cl.duoc.esports.gameservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JuegoDTO {

    private Long id;

    @NotBlank(message = "El nombre del juego es obligatorio")
    private String nombre;

    @NotBlank(message = "El género del juego es obligatorio")
    private String genero;

    @NotBlank(message = "La modalidad del juego es obligatoria")
    private String modalidad;

    @NotNull(message = "La cantidad de jugadores por equipo es obligatoria")
    @Min(value = 1, message = "Debe existir al menos 1 jugador por equipo")
    private Integer jugadoresPorEquipo;

    private Boolean estado;
}
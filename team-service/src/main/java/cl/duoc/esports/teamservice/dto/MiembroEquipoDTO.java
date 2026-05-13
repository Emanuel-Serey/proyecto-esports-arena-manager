package cl.duoc.esports.teamservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiembroEquipoDTO {

    private Long id;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @NotBlank(message = "El rol dentro del equipo es obligatorio")
    private String rolDentroEquipo;
}
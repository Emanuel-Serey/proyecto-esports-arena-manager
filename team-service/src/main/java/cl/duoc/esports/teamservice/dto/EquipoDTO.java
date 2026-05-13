package cl.duoc.esports.teamservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoDTO {

    private Long id;

    @NotBlank(message = "El nombre del equipo es obligatorio")
    private String nombre;

    @NotNull(message = "El ID del capitán es obligatorio")
    private Long capitanId;

    @NotNull(message = "El ID del juego principal es obligatorio")
    private Long juegoPrincipalId;

    private String estado;

    @Valid
    @NotEmpty(message = "El equipo debe tener al menos un miembro")
    private List<MiembroEquipoDTO> miembros;
}
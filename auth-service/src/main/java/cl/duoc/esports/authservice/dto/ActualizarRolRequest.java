package cl.duoc.esports.authservice.dto;

import cl.duoc.esports.authservice.models.Rol;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarRolRequest {

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;
}
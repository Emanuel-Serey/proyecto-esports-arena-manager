package cl.duoc.esports.authservice.dto;

import cl.duoc.esports.authservice.models.EstadoCuenta;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarEstadoRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoCuenta estado;
}
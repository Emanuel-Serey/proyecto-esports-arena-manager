package cl.duoc.esports.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarPerfilDTO {

    @NotBlank(message = "El apodo es obligatorio")
    @Pattern(
            regexp = "\\S+",
            message = "El apodo no puede contener espacios"
    )
    @Size(
            min = 3,
            max = 15,
            message = "El apodo debe tener entre 3 y 15 caracteres"
    )
    private String nickname;
}
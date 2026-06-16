package cl.duoc.esports.tournamentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta estándar para errores de la API")
public class ErrorResponseDTO {

    @Schema(description = "Mensaje descriptivo del error", example = "Mensaje de error")
    private String error;

    public ErrorResponseDTO() {
    }

    public ErrorResponseDTO(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
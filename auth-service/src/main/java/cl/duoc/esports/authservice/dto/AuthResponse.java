package cl.duoc.esports.authservice.dto;

import cl.duoc.esports.authservice.models.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tipo;
    private String email;
    private Rol rol;
}
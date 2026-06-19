package cl.duoc.esports.authservice.dto;

import lombok.Data;

@Data
public class UsuarioAuthResponse {

    private Long id;
    private String nombre;
    private String nickname;
    private String email;
    private String rol;
    private String estado;
}
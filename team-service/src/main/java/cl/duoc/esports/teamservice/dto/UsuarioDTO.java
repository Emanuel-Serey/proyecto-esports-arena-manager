package cl.duoc.esports.teamservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String nickname;
    private String email;
    private String rol;
    private String estado;
    private LocalDate fechaRegistro;
}
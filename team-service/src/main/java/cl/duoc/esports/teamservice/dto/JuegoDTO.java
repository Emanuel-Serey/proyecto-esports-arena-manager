package cl.duoc.esports.teamservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JuegoDTO {

    private Long id;
    private String nombre;
    private String genero;
    private String modalidad;
    private Integer jugadoresPorEquipo;
    private Boolean estado;
}

package cl.duoc.esports.registrationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoDTO {

    private Long id;
    private String nombre;
    private Long capitanId;
    private Long juegoPrincipalId;
    private String estado;
}
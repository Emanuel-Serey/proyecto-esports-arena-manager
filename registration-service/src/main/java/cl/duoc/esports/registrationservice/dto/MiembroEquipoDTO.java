package cl.duoc.esports.registrationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiembroEquipoDTO {

    private Long id;
    private Long usuarioId;
    private String rolDentroEquipo;
}
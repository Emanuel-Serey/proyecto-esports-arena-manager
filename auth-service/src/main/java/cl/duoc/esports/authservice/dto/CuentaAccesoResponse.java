package cl.duoc.esports.authservice.dto;

import cl.duoc.esports.authservice.models.EstadoCuenta;
import cl.duoc.esports.authservice.models.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaAccesoResponse {

    private Long id;
    private String email;
    private Rol rol;
    private EstadoCuenta estado;
    private LocalDate fechaCreacion;
}
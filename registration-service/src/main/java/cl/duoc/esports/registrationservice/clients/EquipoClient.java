package cl.duoc.esports.registrationservice.clients;

import cl.duoc.esports.registrationservice.dto.EquipoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "team-service", url = "http://localhost:8085/api/equipos")
public interface EquipoClient {

    @GetMapping("/{id}")
    EquipoDTO buscarEquipoPorId(@PathVariable Long id);
}
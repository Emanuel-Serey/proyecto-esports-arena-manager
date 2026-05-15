package cl.duoc.esports.registrationservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "sanction-service", url = "http://localhost:8088/api/sanciones")
public interface SancionClient {

    @GetMapping("/usuario/{usuarioId}/activa")
    Boolean existeSancionActivaUsuario(@PathVariable Long usuarioId);

    @GetMapping("/equipo/{equipoId}/activa")
    Boolean existeSancionActivaEquipo(@PathVariable Long equipoId);
}
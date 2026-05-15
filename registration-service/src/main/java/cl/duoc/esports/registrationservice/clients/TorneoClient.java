package cl.duoc.esports.registrationservice.clients;

import cl.duoc.esports.registrationservice.dto.TorneoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tournament-service", url = "http://localhost:8084/api/torneos")
public interface TorneoClient {

    @GetMapping("/{id}")
    TorneoDTO buscarTorneoPorId(@PathVariable Long id);
}
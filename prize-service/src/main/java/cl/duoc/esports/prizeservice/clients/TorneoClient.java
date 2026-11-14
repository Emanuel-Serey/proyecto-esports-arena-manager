package cl.duoc.esports.prizeservice.clients;

import cl.duoc.esports.prizeservice.dto.TorneoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "tournament-service",
        url = "${TOURNAMENT_SERVICE_URL:http://localhost:8084}",
        path = "/api/torneos"
)
public interface TorneoClient {

    @GetMapping("/{id}")
    TorneoDTO buscarTorneoPorId(@PathVariable Long id);
}
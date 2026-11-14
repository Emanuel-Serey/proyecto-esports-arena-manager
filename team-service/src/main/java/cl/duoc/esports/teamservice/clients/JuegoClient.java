package cl.duoc.esports.teamservice.clients;

import cl.duoc.esports.teamservice.dto.JuegoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "game-service",
        url = "${GAME_SERVICE_URL:http://localhost:8083}",
        path = "/api/juegos"
)
public interface JuegoClient {

    @GetMapping("/{id}")
    JuegoDTO buscarJuegoPorId(@PathVariable Long id);
}
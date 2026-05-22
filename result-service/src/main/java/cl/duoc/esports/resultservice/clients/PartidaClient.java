package cl.duoc.esports.resultservice.clients;

import cl.duoc.esports.resultservice.dto.PartidaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "match-service", url = "http://localhost:8089/api/partidas")
public interface PartidaClient {

    @GetMapping("/{id}")
    PartidaDTO buscarPartidaPorId(@PathVariable Long id);
}
package cl.duoc.esports.rankingservice.clients;

import cl.duoc.esports.rankingservice.dto.ResultadoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "result-service",
        url = "${RESULT_SERVICE_URL:http://localhost:8090}",
        path = "/api/resultados"
)
public interface ResultadoClient {

    @GetMapping("/{id}")
    ResultadoDTO buscarResultadoPorId(@PathVariable Long id);
}
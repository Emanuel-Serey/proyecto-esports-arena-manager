package cl.duoc.esports.rankingservice.clients;

import cl.duoc.esports.rankingservice.dto.InscripcionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "registration-service", url = "http://localhost:8086/api/inscripciones")
public interface InscripcionClient {

    @GetMapping("/{id}")
    InscripcionDTO buscarInscripcionPorId(@PathVariable Long id);
}
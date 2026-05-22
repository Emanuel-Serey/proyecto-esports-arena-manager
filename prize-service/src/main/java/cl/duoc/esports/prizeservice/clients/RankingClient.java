package cl.duoc.esports.prizeservice.clients;

import cl.duoc.esports.prizeservice.dto.RankingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ranking-service", url = "http://localhost:8091/api/rankings")
public interface RankingClient {

    @GetMapping("/torneo/{torneoId}/participante/{participanteId}")
    RankingDTO buscarRankingPorParticipante(
            @PathVariable Long torneoId,
            @PathVariable Long participanteId
    );
}
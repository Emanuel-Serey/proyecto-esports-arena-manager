package cl.duoc.esports.rankingservice.controllers;

import cl.duoc.esports.rankingservice.dto.RankingDTO;
import cl.duoc.esports.rankingservice.services.RankingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @PostMapping
    public ResponseEntity<RankingDTO> crearRegistroRanking(@Valid @RequestBody RankingDTO rankingDTO) {
        RankingDTO nuevoRanking = rankingService.crearRegistroRanking(rankingDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoRanking);
    }

    @GetMapping
    public ResponseEntity<List<RankingDTO>> listarRankings() {
        return ResponseEntity.ok(rankingService.listarRankings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RankingDTO> buscarRankingPorId(@PathVariable Long id) {
        return ResponseEntity.ok(rankingService.buscarRankingPorId(id));
    }

    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<RankingDTO>> listarRankingPorTorneo(@PathVariable Long torneoId) {
        return ResponseEntity.ok(rankingService.listarRankingPorTorneo(torneoId));
    }

    @GetMapping("/torneo/{torneoId}/participante/{participanteId}")
    public ResponseEntity<RankingDTO> buscarPosicionPorParticipante(
            @PathVariable Long torneoId,
            @PathVariable Long participanteId) {

        return ResponseEntity.ok(
                rankingService.buscarPosicionPorParticipante(torneoId, participanteId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<RankingDTO> actualizarRanking(
            @PathVariable Long id,
            @Valid @RequestBody RankingDTO rankingDTO) {

        RankingDTO rankingActualizado = rankingService.actualizarRanking(id, rankingDTO);
        return ResponseEntity.ok(rankingActualizado);
    }

    @PutMapping("/resultado/{resultadoId}")
    public ResponseEntity<RankingDTO> actualizarRankingPorResultado(
            @PathVariable Long resultadoId,
            @RequestParam Long torneoId,
            @RequestParam Long participanteAId,
            @RequestParam Long participanteBId) {

        RankingDTO rankingActualizado = rankingService.actualizarRankingPorResultado(
                resultadoId,
                torneoId,
                participanteAId,
                participanteBId
        );

        return ResponseEntity.ok(rankingActualizado);
    }

    @DeleteMapping("/torneo/{torneoId}/reiniciar")
    public ResponseEntity<Void> reiniciarRankingPorTorneo(@PathVariable Long torneoId) {
        rankingService.reiniciarRankingPorTorneo(torneoId);
        return ResponseEntity.noContent().build();
    }
}
package cl.duoc.esports.rankingservice.services;

import cl.duoc.esports.rankingservice.dto.RankingDTO;

import java.util.List;

public interface RankingService {

    RankingDTO crearRegistroRanking(RankingDTO rankingDTO);

    List<RankingDTO> listarRankings();

    RankingDTO buscarRankingPorId(Long id);

    List<RankingDTO> listarRankingPorTorneo(Long torneoId);

    RankingDTO buscarPosicionPorParticipante(Long torneoId, Long participanteId);

    RankingDTO actualizarRanking(Long id, RankingDTO rankingDTO);

    void reiniciarRankingPorTorneo(Long torneoId);

    RankingDTO actualizarRankingPorResultado(Long resultadoId, Long torneoId, Long participanteAId, Long participanteBId);
}
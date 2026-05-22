package cl.duoc.esports.rankingservice.repositories;

import cl.duoc.esports.rankingservice.models.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RankingRepository extends JpaRepository<Ranking, Long> {

    List<Ranking> findByTorneoIdOrderByPosicionAsc(Long torneoId);

    Optional<Ranking> findByTorneoIdAndParticipanteId(Long torneoId, Long participanteId);

    boolean existsByTorneoIdAndParticipanteId(Long torneoId, Long participanteId);

    List<Ranking> findByTorneoIdOrderByPuntosDescDiferenciaDescVictoriasDesc(Long torneoId);
}
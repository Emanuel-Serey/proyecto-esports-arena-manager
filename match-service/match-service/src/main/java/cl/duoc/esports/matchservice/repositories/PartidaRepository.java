package cl.duoc.esports.matchservice.repositories;

import cl.duoc.esports.matchservice.models.Partida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    List<Partida> findByTorneoId(Long torneoId);

    List<Partida> findByRonda(String ronda);

    List<Partida> findByEstado(String estado);

    boolean existsByTorneoIdAndRondaAndParticipanteAIdAndParticipanteBId(
            Long torneoId,
            String ronda,
            Long participanteAId,
            Long participanteBId
    );

    boolean existsByTorneoIdAndRondaAndParticipanteBIdAndParticipanteAId(
            Long torneoId,
            String ronda,
            Long participanteBId,
            Long participanteAId
    );
}
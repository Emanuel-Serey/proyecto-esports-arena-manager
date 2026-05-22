package cl.duoc.esports.prizeservice.repositories;

import cl.duoc.esports.prizeservice.models.Premio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PremioRepository extends JpaRepository<Premio, Long> {

    List<Premio> findByTorneoId(Long torneoId);

    List<Premio> findByParticipanteId(Long participanteId);

    List<Premio> findByEstadoEntrega(String estadoEntrega);

    boolean existsByTorneoIdAndParticipanteIdAndPosicion(
            Long torneoId,
            Long participanteId,
            Integer posicion
    );
}

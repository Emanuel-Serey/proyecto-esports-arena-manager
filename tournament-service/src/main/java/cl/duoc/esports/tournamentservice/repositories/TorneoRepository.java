package cl.duoc.esports.tournamentservice.repositories;

import cl.duoc.esports.tournamentservice.models.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TorneoRepository extends JpaRepository<Torneo, Long> {

    List<Torneo> findByJuegoId(Long juegoId);

    List<Torneo> findByEstado(String estado);

    List<Torneo> findByFechaInicio(LocalDate fechaInicio);
}
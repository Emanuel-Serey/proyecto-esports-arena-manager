package cl.duoc.esports.teamservice.repositories;

import cl.duoc.esports.teamservice.models.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    boolean existsByNombre(String nombre);

    List<Equipo> findByEstado(String estado);

    List<Equipo> findByJuegoPrincipalId(Long juegoPrincipalId);

    List<Equipo> findByCapitanId(Long capitanId);
}
package cl.duoc.esports.sanctionservice.repositories;

import cl.duoc.esports.sanctionservice.models.Sancion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SancionRepository extends JpaRepository<Sancion, Long> {

    List<Sancion> findByUsuarioId(Long usuarioId);

    List<Sancion> findByEquipoId(Long equipoId);

    List<Sancion> findByEstado(String estado);

    boolean existsByUsuarioIdAndEstado(Long usuarioId, String estado);

    boolean existsByEquipoIdAndEstado(Long equipoId, String estado);
}
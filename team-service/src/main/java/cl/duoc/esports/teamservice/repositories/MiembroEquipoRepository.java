package cl.duoc.esports.teamservice.repositories;

import cl.duoc.esports.teamservice.models.MiembroEquipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MiembroEquipoRepository extends JpaRepository<MiembroEquipo, Long> {

    List<MiembroEquipo> findByEquipo_Id(Long equipoId);

    boolean existsByEquipo_IdAndUsuarioId(Long equipoId, Long usuarioId);

    boolean existsByUsuarioIdAndEquipo_Estado(Long usuarioId, String estado);
}
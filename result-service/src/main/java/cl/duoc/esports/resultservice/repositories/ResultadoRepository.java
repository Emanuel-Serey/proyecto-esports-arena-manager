package cl.duoc.esports.resultservice.repositories;

import cl.duoc.esports.resultservice.models.Resultado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultadoRepository extends JpaRepository<Resultado, Long> {

    List<Resultado> findByPartidaId(Long partidaId);

    boolean existsByPartidaId(Long partidaId);
}
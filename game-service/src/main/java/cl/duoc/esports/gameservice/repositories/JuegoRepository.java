package cl.duoc.esports.gameservice.repositories;

import cl.duoc.esports.gameservice.models.Juego;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JuegoRepository extends JpaRepository<Juego, Long> {

    boolean existsByNombre(String nombre);

    List<Juego> findByEstadoTrue();
}
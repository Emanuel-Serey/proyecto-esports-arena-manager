package cl.duoc.esports.authservice.repositories;

import cl.duoc.esports.authservice.models.CuentaAcceso;
import cl.duoc.esports.authservice.models.EstadoCuenta;
import cl.duoc.esports.authservice.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuentaAccesoRepository extends JpaRepository<CuentaAcceso, Long> {

    Optional<CuentaAcceso> findByEmail(String email);

    boolean existsByEmail(String email);

    List<CuentaAcceso> findByRol(Rol rol);

    List<CuentaAcceso> findByEstado(EstadoCuenta estado);
}
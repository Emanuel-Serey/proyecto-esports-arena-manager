package cl.duoc.esports.userservice.repositories;

import cl.duoc.esports.userservice.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    List<Usuario> findByRol(String rol);

    List<Usuario> findByEstado(String estado);

    List<Usuario> findByNicknameContainingIgnoreCase(String nickname);
}
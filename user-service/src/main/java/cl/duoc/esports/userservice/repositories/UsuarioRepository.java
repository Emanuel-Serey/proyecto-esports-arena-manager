package cl.duoc.esports.userservice.repositories;

import cl.duoc.esports.userservice.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    List<Usuario> findByRol(String rol);

    List<Usuario> findByEstado(String estado);

    List<Usuario> findByNicknameContainingIgnoreCase(String nickname);
}
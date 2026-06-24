package cl.duoc.esports.userservice.config;

import cl.duoc.esports.userservice.models.Usuario;
import cl.duoc.esports.userservice.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Optional;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initUsuarios(UsuarioRepository usuarioRepository) {
        return args -> {

            cargarUsuario(
                    usuarioRepository,
                    "Administrador Sistema",
                    "adminsys",
                    "admin@gmail.com",
                    "ADMIN"
            );

            cargarUsuario(
                    usuarioRepository,
                    "Organizador Sistema",
                    "organizadorsys",
                    "organizador@gmail.com",
                    "ORGANIZADOR"
            );

            cargarUsuario(
                    usuarioRepository,
                    "Jugador Sistema",
                    "jugadorsys",
                    "jugador@gmail.com",
                    "JUGADOR"
            );

            System.out.println("Usuarios iniciales cargados en user-service");
        };
    }

    private void cargarUsuario(
            UsuarioRepository usuarioRepository,
            String nombre,
            String nickname,
            String email,
            String rol
    ) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(email);

        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();

            usuario.setNombre(nombre);
            usuario.setNickname(nickname);
            usuario.setRol(rol);
            usuario.setEstado("ACTIVO");

            usuarioRepository.save(usuario);

            System.out.println("Usuario inicial actualizado: " + email);
        } else {
            Usuario usuario = new Usuario();

            usuario.setNombre(nombre);
            usuario.setNickname(nickname);
            usuario.setEmail(email);
            usuario.setRol(rol);
            usuario.setEstado("ACTIVO");
            usuario.setFechaRegistro(LocalDate.now());

            usuarioRepository.save(usuario);

            System.out.println("Usuario inicial creado: " + email);
        }
    }
}
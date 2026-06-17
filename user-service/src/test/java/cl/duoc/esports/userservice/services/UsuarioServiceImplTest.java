package cl.duoc.esports.userservice.services;

import cl.duoc.esports.userservice.dto.UsuarioDTO;
import cl.duoc.esports.userservice.exceptions.UsuarioException;
import cl.duoc.esports.userservice.models.Usuario;
import cl.duoc.esports.userservice.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void crearUsuario_deberiaCrearUsuarioCorrectamente() {
        // Given
        UsuarioDTO usuarioDTO = crearUsuarioDTO(null, "Juan Perez", "juanp", "juan@gmail.com", "jugador", null);

        when(usuarioRepository.existsByNickname("juanp")).thenReturn(false);
        when(usuarioRepository.existsByEmail("juan@gmail.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        // When
        UsuarioDTO resultado = usuarioService.crearUsuario(usuarioDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan Perez", resultado.getNombre());
        assertEquals("juanp", resultado.getNickname());
        assertEquals("juan@gmail.com", resultado.getEmail());
        assertEquals("JUGADOR", resultado.getRol());
        assertEquals("ACTIVO", resultado.getEstado());
        assertNotNull(resultado.getFechaRegistro());

        verify(usuarioRepository).existsByNickname("juanp");
        verify(usuarioRepository).existsByEmail("juan@gmail.com");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void crearUsuario_deberiaLanzarErrorSiNicknameYaExiste() {
        // Given
        UsuarioDTO usuarioDTO = crearUsuarioDTO(null, "Juan Perez", "juanp", "juan@gmail.com", "jugador", null);

        when(usuarioRepository.existsByNickname("juanp")).thenReturn(true);

        // When
        UsuarioException exception = assertThrows(
                UsuarioException.class,
                () -> usuarioService.crearUsuario(usuarioDTO)
        );

        // Then
        assertEquals("Ya existe un usuario con ese nickname", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(usuarioRepository).existsByNickname("juanp");
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void crearUsuario_deberiaLanzarErrorSiEmailYaExiste() {
        // Given
        UsuarioDTO usuarioDTO = crearUsuarioDTO(null, "Juan Perez", "juanp", "juan@gmail.com", "jugador", null);

        when(usuarioRepository.existsByNickname("juanp")).thenReturn(false);
        when(usuarioRepository.existsByEmail("juan@gmail.com")).thenReturn(true);

        // When
        UsuarioException exception = assertThrows(
                UsuarioException.class,
                () -> usuarioService.crearUsuario(usuarioDTO)
        );

        // Then
        assertEquals("Ya existe un usuario con ese email", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(usuarioRepository).existsByNickname("juanp");
        verify(usuarioRepository).existsByEmail("juan@gmail.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void listarUsuarios_deberiaRetornarListaDeUsuarios() {
        // Given
        Usuario usuario1 = crearUsuario(1L, "Juan Perez", "juanp", "juan@gmail.com", "JUGADOR", "ACTIVO");
        Usuario usuario2 = crearUsuario(2L, "Pedro Soto", "pedros", "pedro@gmail.com", "ADMIN", "ACTIVO");

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario1, usuario2));

        // When
        List<UsuarioDTO> resultado = usuarioService.listarUsuarios();

        // Then
        assertEquals(2, resultado.size());
        assertEquals("juanp", resultado.get(0).getNickname());
        assertEquals("pedros", resultado.get(1).getNickname());

        verify(usuarioRepository).findAll();
    }

    @Test
    void buscarUsuarioPorId_deberiaRetornarUsuarioSiExiste() {
        // Given
        Usuario usuario = crearUsuario(1L, "Juan Perez", "juanp", "juan@gmail.com", "JUGADOR", "ACTIVO");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // When
        UsuarioDTO resultado = usuarioService.buscarUsuarioPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan Perez", resultado.getNombre());
        assertEquals("juanp", resultado.getNickname());
        assertEquals("ACTIVO", resultado.getEstado());

        verify(usuarioRepository).findById(1L);
    }

    @Test
    void buscarUsuarioPorId_deberiaLanzarErrorSiNoExiste() {
        // Given
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        UsuarioException exception = assertThrows(
                UsuarioException.class,
                () -> usuarioService.buscarUsuarioPorId(99L)
        );

        // Then
        assertEquals("Usuario no encontrado", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(usuarioRepository).findById(99L);
    }

    @Test
    void actualizarUsuario_deberiaActualizarUsuarioCorrectamente() {
        // Given
        Usuario usuarioExistente = crearUsuario(1L, "Juan Perez", "juanp", "juan@gmail.com", "JUGADOR", "ACTIVO");
        UsuarioDTO usuarioDTO = crearUsuarioDTO(null, "Juan Actualizado", "juanpro", "juanpro@gmail.com", "admin", null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.existsByNickname("juanpro")).thenReturn(false);
        when(usuarioRepository.existsByEmail("juanpro@gmail.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UsuarioDTO resultado = usuarioService.actualizarUsuario(1L, usuarioDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan Actualizado", resultado.getNombre());
        assertEquals("juanpro", resultado.getNickname());
        assertEquals("juanpro@gmail.com", resultado.getEmail());
        assertEquals("ADMIN", resultado.getRol());

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).existsByNickname("juanpro");
        verify(usuarioRepository).existsByEmail("juanpro@gmail.com");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void actualizarUsuario_deberiaLanzarErrorSiNicknameYaExiste() {
        // Given
        Usuario usuarioExistente = crearUsuario(1L, "Juan Perez", "juanp", "juan@gmail.com", "JUGADOR", "ACTIVO");
        UsuarioDTO usuarioDTO = crearUsuarioDTO(null, "Juan Perez", "otroNick", "juan@gmail.com", "jugador", null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.existsByNickname("otroNick")).thenReturn(true);

        // When
        UsuarioException exception = assertThrows(
                UsuarioException.class,
                () -> usuarioService.actualizarUsuario(1L, usuarioDTO)
        );

        // Then
        assertEquals("Ya existe un usuario con ese nickname", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).existsByNickname("otroNick");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarUsuario_deberiaLanzarErrorSiEmailYaExiste() {
        // Given
        Usuario usuarioExistente = crearUsuario(1L, "Juan Perez", "juanp", "juan@gmail.com", "JUGADOR", "ACTIVO");
        UsuarioDTO usuarioDTO = crearUsuarioDTO(null, "Juan Perez", "juanp", "otro@gmail.com", "jugador", null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.existsByEmail("otro@gmail.com")).thenReturn(true);

        // When
        UsuarioException exception = assertThrows(
                UsuarioException.class,
                () -> usuarioService.actualizarUsuario(1L, usuarioDTO)
        );

        // Then
        assertEquals("Ya existe un usuario con ese email", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).existsByEmail("otro@gmail.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void desactivarUsuario_deberiaCambiarEstadoAInactivo() {
        // Given
        Usuario usuario = crearUsuario(1L, "Juan Perez", "juanp", "juan@gmail.com", "JUGADOR", "ACTIVO");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        usuarioService.desactivarUsuario(1L);

        // Then
        assertEquals("INACTIVO", usuario.getEstado());

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void listarPorRol_deberiaRetornarUsuariosPorRol() {
        // Given
        Usuario usuario = crearUsuario(1L, "Juan Perez", "juanp", "juan@gmail.com", "JUGADOR", "ACTIVO");

        when(usuarioRepository.findByRol("JUGADOR")).thenReturn(List.of(usuario));

        // When
        List<UsuarioDTO> resultado = usuarioService.listarPorRol("jugador");

        // Then
        assertEquals(1, resultado.size());
        assertEquals("JUGADOR", resultado.get(0).getRol());

        verify(usuarioRepository).findByRol("JUGADOR");
    }

    @Test
    void listarPorEstado_deberiaRetornarUsuariosPorEstado() {
        // Given
        Usuario usuario = crearUsuario(1L, "Juan Perez", "juanp", "juan@gmail.com", "JUGADOR", "ACTIVO");

        when(usuarioRepository.findByEstado("ACTIVO")).thenReturn(List.of(usuario));

        // When
        List<UsuarioDTO> resultado = usuarioService.listarPorEstado("activo");

        // Then
        assertEquals(1, resultado.size());
        assertEquals("ACTIVO", resultado.get(0).getEstado());

        verify(usuarioRepository).findByEstado("ACTIVO");
    }

    @Test
    void buscarPorNickname_deberiaRetornarUsuariosCoincidentes() {
        // Given
        Usuario usuario = crearUsuario(1L, "Juan Perez", "juanp", "juan@gmail.com", "JUGADOR", "ACTIVO");

        when(usuarioRepository.findByNicknameContainingIgnoreCase("juan")).thenReturn(List.of(usuario));

        // When
        List<UsuarioDTO> resultado = usuarioService.buscarPorNickname("juan");

        // Then
        assertEquals(1, resultado.size());
        assertEquals("juanp", resultado.get(0).getNickname());

        verify(usuarioRepository).findByNicknameContainingIgnoreCase("juan");
    }

    private Usuario crearUsuario(Long id, String nombre, String nickname, String email, String rol, String estado) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setNickname(nickname);
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setEstado(estado);
        usuario.setFechaRegistro(LocalDate.now());
        return usuario;
    }

    private UsuarioDTO crearUsuarioDTO(Long id, String nombre, String nickname, String email, String rol, String estado) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(id);
        usuarioDTO.setNombre(nombre);
        usuarioDTO.setNickname(nickname);
        usuarioDTO.setEmail(email);
        usuarioDTO.setRol(rol);
        usuarioDTO.setEstado(estado);
        usuarioDTO.setFechaRegistro(LocalDate.now());
        return usuarioDTO;
    }
}
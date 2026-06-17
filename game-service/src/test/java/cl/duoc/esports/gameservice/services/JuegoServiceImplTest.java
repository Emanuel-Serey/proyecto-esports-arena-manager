package cl.duoc.esports.gameservice.services;

import cl.duoc.esports.gameservice.dto.JuegoDTO;
import cl.duoc.esports.gameservice.exceptions.JuegoException;
import cl.duoc.esports.gameservice.models.Juego;
import cl.duoc.esports.gameservice.repositories.JuegoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JuegoServiceImplTest {

    @Mock
    private JuegoRepository juegoRepository;

    @InjectMocks
    private JuegoServiceImpl juegoService;

    @Test
    void crearJuego_deberiaCrearJuegoCorrectamente() {
        // Given
        JuegoDTO juegoDTO = crearJuegoDTO(null, "League of Legends", "MOBA", "EQUIPO", 5, null);

        when(juegoRepository.existsByNombre("League of Legends")).thenReturn(false);
        when(juegoRepository.save(any(Juego.class))).thenAnswer(invocation -> {
            Juego juego = invocation.getArgument(0);
            juego.setId(1L);
            return juego;
        });

        // When
        JuegoDTO resultado = juegoService.crearJuego(juegoDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("League of Legends", resultado.getNombre());
        assertEquals("MOBA", resultado.getGenero());
        assertEquals("EQUIPO", resultado.getModalidad());
        assertEquals(5, resultado.getJugadoresPorEquipo());
        assertTrue(resultado.getEstado());

        verify(juegoRepository).existsByNombre("League of Legends");
        verify(juegoRepository).save(any(Juego.class));
    }

    @Test
    void crearJuego_deberiaLanzarErrorSiNombreYaExiste() {
        // Given
        JuegoDTO juegoDTO = crearJuegoDTO(null, "League of Legends", "MOBA", "EQUIPO", 5, null);

        when(juegoRepository.existsByNombre("League of Legends")).thenReturn(true);

        // When
        JuegoException exception = assertThrows(
                JuegoException.class,
                () -> juegoService.crearJuego(juegoDTO)
        );

        // Then
        assertEquals("Ya existe un juego con ese nombre", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(juegoRepository).existsByNombre("League of Legends");
        verify(juegoRepository, never()).save(any(Juego.class));
    }

    @Test
    void listarJuegos_deberiaRetornarListaDeJuegos() {
        // Given
        Juego juego1 = crearJuego(1L, "League of Legends", "MOBA", "EQUIPO", 5, true);
        Juego juego2 = crearJuego(2L, "Valorant", "Shooter", "EQUIPO", 5, true);

        when(juegoRepository.findAll()).thenReturn(List.of(juego1, juego2));

        // When
        List<JuegoDTO> resultado = juegoService.listarJuegos();

        // Then
        assertEquals(2, resultado.size());
        assertEquals("League of Legends", resultado.get(0).getNombre());
        assertEquals("Valorant", resultado.get(1).getNombre());

        verify(juegoRepository).findAll();
    }

    @Test
    void listarJuegosActivos_deberiaRetornarListaDeJuegosActivos() {
        // Given
        Juego juego = crearJuego(1L, "League of Legends", "MOBA", "EQUIPO", 5, true);

        when(juegoRepository.findByEstadoTrue()).thenReturn(List.of(juego));

        // When
        List<JuegoDTO> resultado = juegoService.listarJuegosActivos();

        // Then
        assertEquals(1, resultado.size());
        assertEquals("League of Legends", resultado.get(0).getNombre());
        assertTrue(resultado.get(0).getEstado());

        verify(juegoRepository).findByEstadoTrue();
    }

    @Test
    void buscarJuegoPorId_deberiaRetornarJuegoSiExiste() {
        // Given
        Juego juego = crearJuego(1L, "League of Legends", "MOBA", "EQUIPO", 5, true);

        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));

        // When
        JuegoDTO resultado = juegoService.buscarJuegoPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("League of Legends", resultado.getNombre());
        assertEquals("MOBA", resultado.getGenero());
        assertEquals("EQUIPO", resultado.getModalidad());
        assertEquals(5, resultado.getJugadoresPorEquipo());
        assertTrue(resultado.getEstado());

        verify(juegoRepository).findById(1L);
    }

    @Test
    void buscarJuegoPorId_deberiaLanzarErrorSiJuegoNoExiste() {
        // Given
        when(juegoRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        JuegoException exception = assertThrows(
                JuegoException.class,
                () -> juegoService.buscarJuegoPorId(99L)
        );

        // Then
        assertEquals("Juego no encontrado", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(juegoRepository).findById(99L);
    }

    @Test
    void desactivarJuego_deberiaCambiarEstadoAFalse() {
        // Given
        Juego juego = crearJuego(1L, "League of Legends", "MOBA", "EQUIPO", 5, true);

        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(juegoRepository.save(any(Juego.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        juegoService.desactivarJuego(1L);

        // Then
        assertFalse(juego.getEstado());

        verify(juegoRepository).findById(1L);
        verify(juegoRepository).save(juego);
    }

    @Test
    void desactivarJuego_deberiaLanzarErrorSiJuegoNoExiste() {
        // Given
        when(juegoRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        JuegoException exception = assertThrows(
                JuegoException.class,
                () -> juegoService.desactivarJuego(99L)
        );

        // Then
        assertEquals("Juego no encontrado", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(juegoRepository).findById(99L);
        verify(juegoRepository, never()).save(any(Juego.class));
    }

    private Juego crearJuego(Long id, String nombre, String genero, String modalidad, Integer jugadoresPorEquipo, Boolean estado) {
        Juego juego = new Juego();
        juego.setId(id);
        juego.setNombre(nombre);
        juego.setGenero(genero);
        juego.setModalidad(modalidad);
        juego.setJugadoresPorEquipo(jugadoresPorEquipo);
        juego.setEstado(estado);
        return juego;
    }

    private JuegoDTO crearJuegoDTO(Long id, String nombre, String genero, String modalidad, Integer jugadoresPorEquipo, Boolean estado) {
        JuegoDTO juegoDTO = new JuegoDTO();
        juegoDTO.setId(id);
        juegoDTO.setNombre(nombre);
        juegoDTO.setGenero(genero);
        juegoDTO.setModalidad(modalidad);
        juegoDTO.setJugadoresPorEquipo(jugadoresPorEquipo);
        juegoDTO.setEstado(estado);
        return juegoDTO;
    }
}
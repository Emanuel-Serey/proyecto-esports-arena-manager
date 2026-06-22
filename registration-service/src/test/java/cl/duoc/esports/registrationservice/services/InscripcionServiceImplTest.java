package cl.duoc.esports.registrationservice.services;

import cl.duoc.esports.registrationservice.services.InscripcionServiceImpl;
import cl.duoc.esports.registrationservice.clients.EquipoClient;
import cl.duoc.esports.registrationservice.clients.SancionClient;
import cl.duoc.esports.registrationservice.clients.TorneoClient;
import cl.duoc.esports.registrationservice.clients.UsuarioClient;
import cl.duoc.esports.registrationservice.dto.EquipoDTO;
import cl.duoc.esports.registrationservice.dto.InscripcionDTO;
import cl.duoc.esports.registrationservice.dto.TorneoDTO;
import cl.duoc.esports.registrationservice.dto.UsuarioDTO;
import cl.duoc.esports.registrationservice.exceptions.InscripcionException;
import cl.duoc.esports.registrationservice.models.Inscripcion;
import cl.duoc.esports.registrationservice.repositories.InscripcionRepository;
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
class InscripcionServiceImplTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private TorneoClient torneoClient;

    @Mock
    private EquipoClient equipoClient;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private SancionClient sancionClient;

    @InjectMocks
    private InscripcionServiceImpl inscripcionService;

    @Test
    void crearInscripcion_deberiaCrearInscripcionDeEquipoCorrectamente() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, 2L, null, "EQUIPO", null, null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO", 10));
        when(inscripcionRepository.findByTorneoId(1L)).thenReturn(List.of());
        when(equipoClient.buscarEquipoPorId(2L)).thenReturn(crearEquipoDTO(2L, "ACTIVO"));
        when(sancionClient.existeSancionActivaEquipo(2L)).thenReturn(false);
        when(inscripcionRepository.existsByTorneoIdAndEquipoId(1L, 2L)).thenReturn(false);

        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> {
            Inscripcion inscripcion = invocation.getArgument(0);
            inscripcion.setId(1L);
            return inscripcion;
        });

        // When
        InscripcionDTO resultado = inscripcionService.crearInscripcion(inscripcionDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getTorneoId());
        assertEquals(2L, resultado.getEquipoId());
        assertNull(resultado.getJugadorId());
        assertEquals("EQUIPO", resultado.getTipoParticipante());
        assertEquals("PENDIENTE", resultado.getEstado());
        assertNotNull(resultado.getFechaInscripcion());

        verify(torneoClient).buscarTorneoPorId(1L);
        verify(equipoClient).buscarEquipoPorId(2L);
        verify(sancionClient).existeSancionActivaEquipo(2L);
        verify(inscripcionRepository).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaCrearInscripcionDeJugadorCorrectamente() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, null, 3L, "JUGADOR", null, null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO", 10));
        when(inscripcionRepository.findByTorneoId(1L)).thenReturn(List.of());
        when(usuarioClient.buscarUsuarioPorId(3L)).thenReturn(crearUsuarioDTO(3L, "ACTIVO"));
        when(sancionClient.existeSancionActivaUsuario(3L)).thenReturn(false);
        when(inscripcionRepository.existsByTorneoIdAndJugadorId(1L, 3L)).thenReturn(false);

        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> {
            Inscripcion inscripcion = invocation.getArgument(0);
            inscripcion.setId(1L);
            return inscripcion;
        });

        // When
        InscripcionDTO resultado = inscripcionService.crearInscripcion(inscripcionDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getTorneoId());
        assertNull(resultado.getEquipoId());
        assertEquals(3L, resultado.getJugadorId());
        assertEquals("JUGADOR", resultado.getTipoParticipante());
        assertEquals("PENDIENTE", resultado.getEstado());

        verify(torneoClient).buscarTorneoPorId(1L);
        verify(usuarioClient).buscarUsuarioPorId(3L);
        verify(sancionClient).existeSancionActivaUsuario(3L);
        verify(inscripcionRepository).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiTipoEquipoNoTieneEquipoId() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, null, null, "EQUIPO", null, null
        );

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("Si el tipo de participante es EQUIPO, debe indicar equipoId", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiTipoEquipoIncluyeJugadorId() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, 2L, 3L, "EQUIPO", null, null
        );

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("Una inscripción de tipo EQUIPO no debe incluir jugadorId", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiTipoJugadorNoTieneJugadorId() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, null, null, "JUGADOR", null, null
        );

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("Si el tipo de participante es JUGADOR, debe indicar jugadorId", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiTipoJugadorIncluyeEquipoId() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, 2L, 3L, "JUGADOR", null, null
        );

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("Una inscripción de tipo JUGADOR no debe incluir equipoId", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiTipoParticipanteEsInvalido() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, null, null, "INVITADO", null, null
        );

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("El tipo de participante debe ser EQUIPO o JUGADOR", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiTorneoEstaCancelado() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, 2L, null, "EQUIPO", null, null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("CANCELADO", 10));

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("No se puede inscribir en un torneo cancelado", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiTorneoEstaCerrado() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, 2L, null, "EQUIPO", null, null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("CERRADO", 10));

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("No se puede inscribir en un torneo cerrado", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiNoHayCuposDisponibles() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, 2L, null, "EQUIPO", null, null
        );

        Inscripcion inscripcionExistente = crearInscripcion(
                1L, 1L, 99L, null, "EQUIPO", "PENDIENTE"
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO", 1));
        when(inscripcionRepository.findByTorneoId(1L)).thenReturn(List.of(inscripcionExistente));

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("No existen cupos disponibles para este torneo", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiEquipoNoEstaActivo() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, 2L, null, "EQUIPO", null, null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO", 10));
        when(inscripcionRepository.findByTorneoId(1L)).thenReturn(List.of());
        when(equipoClient.buscarEquipoPorId(2L)).thenReturn(crearEquipoDTO(2L, "INACTIVO"));

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("El equipo no está activo", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiJugadorNoEstaActivo() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, null, 3L, "JUGADOR", null, null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO", 10));
        when(inscripcionRepository.findByTorneoId(1L)).thenReturn(List.of());
        when(usuarioClient.buscarUsuarioPorId(3L)).thenReturn(crearUsuarioDTO(3L, "INACTIVO"));

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("El jugador no está activo", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiEquipoTieneSancionActiva() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, 2L, null, "EQUIPO", null, null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO", 10));
        when(inscripcionRepository.findByTorneoId(1L)).thenReturn(List.of());
        when(equipoClient.buscarEquipoPorId(2L)).thenReturn(crearEquipoDTO(2L, "ACTIVO"));
        when(sancionClient.existeSancionActivaEquipo(2L)).thenReturn(true);

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("No se puede inscribir un equipo con sanción activa", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiJugadorTieneSancionActiva() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, null, 3L, "JUGADOR", null, null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO", 10));
        when(inscripcionRepository.findByTorneoId(1L)).thenReturn(List.of());
        when(sancionClient.existeSancionActivaUsuario(3L)).thenReturn(true);

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("No se puede inscribir un jugador con sanción activa", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiEquipoYaEstaInscrito() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, 2L, null, "EQUIPO", null, null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO", 10));
        when(inscripcionRepository.findByTorneoId(1L)).thenReturn(List.of());
        when(equipoClient.buscarEquipoPorId(2L)).thenReturn(crearEquipoDTO(2L, "ACTIVO"));
        when(sancionClient.existeSancionActivaEquipo(2L)).thenReturn(false);
        when(inscripcionRepository.existsByTorneoIdAndEquipoId(1L, 2L)).thenReturn(true);

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("El equipo ya está inscrito en este torneo", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void crearInscripcion_deberiaLanzarErrorSiJugadorYaEstaInscrito() {
        // Given
        InscripcionDTO inscripcionDTO = crearInscripcionDTO(
                null, 1L, null, 3L, "JUGADOR", null, null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO", 10));
        when(inscripcionRepository.findByTorneoId(1L)).thenReturn(List.of());
        when(usuarioClient.buscarUsuarioPorId(3L)).thenReturn(crearUsuarioDTO(3L, "ACTIVO"));
        when(sancionClient.existeSancionActivaUsuario(3L)).thenReturn(false);
        when(inscripcionRepository.existsByTorneoIdAndJugadorId(1L, 3L)).thenReturn(true);

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.crearInscripcion(inscripcionDTO)
        );

        // Then
        assertEquals("El jugador ya está inscrito en este torneo", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(inscripcionRepository, never()).save(any(Inscripcion.class));
    }

    @Test
    void listarInscripciones_deberiaRetornarListaDeInscripciones() {
        // Given
        Inscripcion inscripcion1 = crearInscripcion(1L, 1L, 2L, null, "EQUIPO", "PENDIENTE");
        Inscripcion inscripcion2 = crearInscripcion(2L, 1L, null, 3L, "JUGADOR", "PENDIENTE");

        when(inscripcionRepository.findAll()).thenReturn(List.of(inscripcion1, inscripcion2));

        // When
        List<InscripcionDTO> resultado = inscripcionService.listarInscripciones();

        // Then
        assertEquals(2, resultado.size());
        assertEquals("EQUIPO", resultado.get(0).getTipoParticipante());
        assertEquals("JUGADOR", resultado.get(1).getTipoParticipante());

        verify(inscripcionRepository).findAll();
    }

    @Test
    void buscarInscripcionPorId_deberiaRetornarInscripcionSiExiste() {
        // Given
        Inscripcion inscripcion = crearInscripcion(1L, 1L, 2L, null, "EQUIPO", "PENDIENTE");

        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));

        // When
        InscripcionDTO resultado = inscripcionService.buscarInscripcionPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getTorneoId());
        assertEquals(2L, resultado.getEquipoId());
        assertEquals("PENDIENTE", resultado.getEstado());

        verify(inscripcionRepository).findById(1L);
    }

    @Test
    void buscarInscripcionPorId_deberiaLanzarErrorSiNoExiste() {
        // Given
        when(inscripcionRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        InscripcionException exception = assertThrows(
                InscripcionException.class,
                () -> inscripcionService.buscarInscripcionPorId(99L)
        );

        // Then
        assertEquals("Inscripción no encontrada", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(inscripcionRepository).findById(99L);
    }

    @Test
    void actualizarEstado_deberiaActualizarEstadoCorrectamente() {
        // Given
        Inscripcion inscripcion = crearInscripcion(1L, 1L, 2L, null, "EQUIPO", "PENDIENTE");

        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        InscripcionDTO resultado = inscripcionService.actualizarEstado(1L, "aprobada");

        // Then
        assertEquals("APROBADA", resultado.getEstado());

        verify(inscripcionRepository).findById(1L);
        verify(inscripcionRepository).save(inscripcion);
    }

    @Test
    void cancelarInscripcion_deberiaCambiarEstadoACancelada() {
        // Given
        Inscripcion inscripcion = crearInscripcion(1L, 1L, 2L, null, "EQUIPO", "PENDIENTE");

        when(inscripcionRepository.findById(1L)).thenReturn(Optional.of(inscripcion));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        inscripcionService.cancelarInscripcion(1L);

        // Then
        assertEquals("CANCELADA", inscripcion.getEstado());

        verify(inscripcionRepository).findById(1L);
        verify(inscripcionRepository).save(inscripcion);
    }

    @Test
    void listarPorTorneo_deberiaRetornarInscripcionesPorTorneo() {
        // Given
        Inscripcion inscripcion = crearInscripcion(1L, 1L, 2L, null, "EQUIPO", "PENDIENTE");

        when(inscripcionRepository.findByTorneoId(1L)).thenReturn(List.of(inscripcion));

        // When
        List<InscripcionDTO> resultado = inscripcionService.listarPorTorneo(1L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getTorneoId());

        verify(inscripcionRepository).findByTorneoId(1L);
    }

    @Test
    void listarPorEquipo_deberiaRetornarInscripcionesPorEquipo() {
        // Given
        Inscripcion inscripcion = crearInscripcion(1L, 1L, 2L, null, "EQUIPO", "PENDIENTE");

        when(inscripcionRepository.findByEquipoId(2L)).thenReturn(List.of(inscripcion));

        // When
        List<InscripcionDTO> resultado = inscripcionService.listarPorEquipo(2L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).getEquipoId());

        verify(inscripcionRepository).findByEquipoId(2L);
    }

    @Test
    void listarPorJugador_deberiaRetornarInscripcionesPorJugador() {
        // Given
        Inscripcion inscripcion = crearInscripcion(1L, 1L, null, 3L, "JUGADOR", "PENDIENTE");

        when(inscripcionRepository.findByJugadorId(3L)).thenReturn(List.of(inscripcion));

        // When
        List<InscripcionDTO> resultado = inscripcionService.listarPorJugador(3L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(3L, resultado.get(0).getJugadorId());

        verify(inscripcionRepository).findByJugadorId(3L);
    }

    private Inscripcion crearInscripcion(
            Long id,
            Long torneoId,
            Long equipoId,
            Long jugadorId,
            String tipoParticipante,
            String estado) {

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setId(id);
        inscripcion.setTorneoId(torneoId);
        inscripcion.setEquipoId(equipoId);
        inscripcion.setJugadorId(jugadorId);
        inscripcion.setTipoParticipante(tipoParticipante);
        inscripcion.setEstado(estado);
        inscripcion.setFechaInscripcion(LocalDate.now());
        return inscripcion;
    }

    private InscripcionDTO crearInscripcionDTO(
            Long id,
            Long torneoId,
            Long equipoId,
            Long jugadorId,
            String tipoParticipante,
            String estado,
            LocalDate fechaInscripcion) {

        InscripcionDTO inscripcionDTO = new InscripcionDTO();
        inscripcionDTO.setId(id);
        inscripcionDTO.setTorneoId(torneoId);
        inscripcionDTO.setEquipoId(equipoId);
        inscripcionDTO.setJugadorId(jugadorId);
        inscripcionDTO.setTipoParticipante(tipoParticipante);
        inscripcionDTO.setEstado(estado);
        inscripcionDTO.setFechaInscripcion(fechaInscripcion);
        return inscripcionDTO;
    }

    private TorneoDTO crearTorneoDTO(String estado, Integer cupoMaximo) {
        TorneoDTO torneoDTO = new TorneoDTO();
        torneoDTO.setId(1L);
        torneoDTO.setEstado(estado);
        torneoDTO.setCupoMaximo(cupoMaximo);
        torneoDTO.setFechaInicio(LocalDate.now().plusDays(10));
        return torneoDTO;
    }

    private EquipoDTO crearEquipoDTO(Long id, String estado) {
        EquipoDTO equipoDTO = new EquipoDTO();
        equipoDTO.setId(id);
        equipoDTO.setEstado(estado);
        return equipoDTO;
    }

    private UsuarioDTO crearUsuarioDTO(Long id, String estado) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(id);
        usuarioDTO.setEstado(estado);
        return usuarioDTO;
    }
}
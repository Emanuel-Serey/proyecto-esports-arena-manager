package cl.duoc.esports.sanctionservice.services;

import cl.duoc.esports.sanctionservice.clients.EquipoClient;
import cl.duoc.esports.sanctionservice.clients.UsuarioClient;
import cl.duoc.esports.sanctionservice.dto.EquipoDTO;
import cl.duoc.esports.sanctionservice.dto.SancionDTO;
import cl.duoc.esports.sanctionservice.dto.UsuarioDTO;
import cl.duoc.esports.sanctionservice.exceptions.SancionException;
import cl.duoc.esports.sanctionservice.models.Sancion;
import cl.duoc.esports.sanctionservice.repositories.SancionRepository;
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
class SancionServiceImplTest {

    @Mock
    private SancionRepository sancionRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private EquipoClient equipoClient;

    @InjectMocks
    private SancionServiceImpl sancionService;

    @Test
    void crearSancion_deberiaCrearSancionDeUsuarioCorrectamente() {
        // Given
        SancionDTO sancionDTO = crearSancionDTO(
                null,
                1L,
                null,
                "Conducta antideportiva",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 10),
                null,
                "media"
        );

        when(usuarioClient.buscarUsuarioPorId(1L)).thenReturn(crearUsuarioDTO(1L, "ACTIVO"));
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(invocation -> {
            Sancion sancion = invocation.getArgument(0);
            sancion.setId(1L);
            return sancion;
        });

        // When
        SancionDTO resultado = sancionService.crearSancion(sancionDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getUsuarioId());
        assertNull(resultado.getEquipoId());
        assertEquals("Conducta antideportiva", resultado.getMotivo());
        assertEquals("ACTIVA", resultado.getEstado());
        assertEquals("MEDIA", resultado.getSeveridad());

        verify(usuarioClient).buscarUsuarioPorId(1L);
        verify(sancionRepository).save(any(Sancion.class));
    }

    @Test
    void crearSancion_deberiaCrearSancionDeEquipoCorrectamente() {
        // Given
        SancionDTO sancionDTO = crearSancionDTO(
                null,
                null,
                2L,
                "Incumplimiento de reglas",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 10),
                null,
                "alta"
        );

        when(equipoClient.buscarEquipoPorId(2L)).thenReturn(crearEquipoDTO(2L, "ACTIVO"));
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(invocation -> {
            Sancion sancion = invocation.getArgument(0);
            sancion.setId(1L);
            return sancion;
        });

        // When
        SancionDTO resultado = sancionService.crearSancion(sancionDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertNull(resultado.getUsuarioId());
        assertEquals(2L, resultado.getEquipoId());
        assertEquals("Incumplimiento de reglas", resultado.getMotivo());
        assertEquals("ACTIVA", resultado.getEstado());
        assertEquals("ALTA", resultado.getSeveridad());

        verify(equipoClient).buscarEquipoPorId(2L);
        verify(sancionRepository).save(any(Sancion.class));
    }

    @Test
    void crearSancion_deberiaLanzarErrorSiNoIndicaUsuarioNiEquipo() {
        // Given
        SancionDTO sancionDTO = crearSancionDTO(
                null,
                null,
                null,
                "Conducta antideportiva",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 10),
                null,
                "media"
        );

        // When
        SancionException exception = assertThrows(
                SancionException.class,
                () -> sancionService.crearSancion(sancionDTO)
        );

        // Then
        assertEquals("Debe indicar usuarioId o equipoId", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(sancionRepository, never()).save(any(Sancion.class));
    }

    @Test
    void crearSancion_deberiaLanzarErrorSiIndicaUsuarioYEquipoAlMismoTiempo() {
        // Given
        SancionDTO sancionDTO = crearSancionDTO(
                null,
                1L,
                2L,
                "Conducta antideportiva",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 10),
                null,
                "media"
        );

        // When
        SancionException exception = assertThrows(
                SancionException.class,
                () -> sancionService.crearSancion(sancionDTO)
        );

        // Then
        assertEquals("Una sanción no puede tener usuarioId y equipoId al mismo tiempo", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(sancionRepository, never()).save(any(Sancion.class));
    }

    @Test
    void crearSancion_deberiaLanzarErrorSiFechaFinEsAnteriorAFechaInicio() {
        // Given
        SancionDTO sancionDTO = crearSancionDTO(
                null,
                1L,
                null,
                "Conducta antideportiva",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 1),
                null,
                "media"
        );

        // When
        SancionException exception = assertThrows(
                SancionException.class,
                () -> sancionService.crearSancion(sancionDTO)
        );

        // Then
        assertEquals("La fecha de fin no puede ser anterior a la fecha de inicio", exception.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatus());

        verify(sancionRepository, never()).save(any(Sancion.class));
    }

    @Test
    void crearSancion_deberiaLanzarErrorSiUsuarioNoEstaActivo() {
        // Given
        SancionDTO sancionDTO = crearSancionDTO(
                null,
                1L,
                null,
                "Conducta antideportiva",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 10),
                null,
                "media"
        );

        when(usuarioClient.buscarUsuarioPorId(1L)).thenReturn(crearUsuarioDTO(1L, "INACTIVO"));

        // When
        SancionException exception = assertThrows(
                SancionException.class,
                () -> sancionService.crearSancion(sancionDTO)
        );

        // Then
        assertEquals("El usuario no está activo", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(sancionRepository, never()).save(any(Sancion.class));
    }

    @Test
    void crearSancion_deberiaLanzarErrorSiEquipoNoEstaActivo() {
        // Given
        SancionDTO sancionDTO = crearSancionDTO(
                null,
                null,
                2L,
                "Conducta antideportiva",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 10),
                null,
                "media"
        );

        when(equipoClient.buscarEquipoPorId(2L)).thenReturn(crearEquipoDTO(2L, "INACTIVO"));

        // When
        SancionException exception = assertThrows(
                SancionException.class,
                () -> sancionService.crearSancion(sancionDTO)
        );

        // Then
        assertEquals("El equipo no está activo", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(sancionRepository, never()).save(any(Sancion.class));
    }

    @Test
    void crearSancion_deberiaLanzarErrorSiUserServiceNoResponde() {
        // Given
        SancionDTO sancionDTO = crearSancionDTO(
                null,
                1L,
                null,
                "Conducta antideportiva",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 10),
                null,
                "media"
        );

        when(usuarioClient.buscarUsuarioPorId(1L)).thenThrow(new RuntimeException("Servicio caído"));

        // When
        SancionException exception = assertThrows(
                SancionException.class,
                () -> sancionService.crearSancion(sancionDTO)
        );

        // Then
        assertEquals("No se pudo validar el usuario desde user-service", exception.getMessage());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());

        verify(sancionRepository, never()).save(any(Sancion.class));
    }

    @Test
    void crearSancion_deberiaLanzarErrorSiTeamServiceNoResponde() {
        // Given
        SancionDTO sancionDTO = crearSancionDTO(
                null,
                null,
                2L,
                "Conducta antideportiva",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 10),
                null,
                "media"
        );

        when(equipoClient.buscarEquipoPorId(2L)).thenThrow(new RuntimeException("Servicio caído"));

        // When
        SancionException exception = assertThrows(
                SancionException.class,
                () -> sancionService.crearSancion(sancionDTO)
        );

        // Then
        assertEquals("No se pudo validar el equipo desde team-service", exception.getMessage());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());

        verify(sancionRepository, never()).save(any(Sancion.class));
    }

    @Test
    void listarSanciones_deberiaRetornarListaDeSanciones() {
        // Given
        Sancion sancion1 = crearSancion(
                1L, 1L, null, "Conducta antideportiva",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10),
                "ACTIVA", "MEDIA"
        );

        Sancion sancion2 = crearSancion(
                2L, null, 2L, "Incumplimiento de reglas",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10),
                "CERRADA", "ALTA"
        );

        when(sancionRepository.findAll()).thenReturn(List.of(sancion1, sancion2));

        // When
        List<SancionDTO> resultado = sancionService.listarSanciones();

        // Then
        assertEquals(2, resultado.size());
        assertEquals("ACTIVA", resultado.get(0).getEstado());
        assertEquals("CERRADA", resultado.get(1).getEstado());

        verify(sancionRepository).findAll();
    }

    @Test
    void buscarSancionPorId_deberiaRetornarSancionSiExiste() {
        // Given
        Sancion sancion = crearSancion(
                1L, 1L, null, "Conducta antideportiva",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10),
                "ACTIVA", "MEDIA"
        );

        when(sancionRepository.findById(1L)).thenReturn(Optional.of(sancion));

        // When
        SancionDTO resultado = sancionService.buscarSancionPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getUsuarioId());
        assertEquals("ACTIVA", resultado.getEstado());
        assertEquals("MEDIA", resultado.getSeveridad());

        verify(sancionRepository).findById(1L);
    }

    @Test
    void buscarSancionPorId_deberiaLanzarErrorSiNoExiste() {
        // Given
        when(sancionRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        SancionException exception = assertThrows(
                SancionException.class,
                () -> sancionService.buscarSancionPorId(99L)
        );

        // Then
        assertEquals("Sanción no encontrada", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(sancionRepository).findById(99L);
    }

    @Test
    void actualizarSancion_deberiaActualizarSancionCorrectamente() {
        // Given
        Sancion sancionExistente = crearSancion(
                1L, 1L, null, "Conducta antideportiva",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10),
                "ACTIVA", "MEDIA"
        );

        SancionDTO sancionDTO = crearSancionDTO(
                null,
                1L,
                null,
                "Nueva conducta",
                LocalDate.of(2026, 5, 2),
                LocalDate.of(2026, 5, 12),
                "cerrada",
                "alta"
        );

        when(sancionRepository.findById(1L)).thenReturn(Optional.of(sancionExistente));
        when(usuarioClient.buscarUsuarioPorId(1L)).thenReturn(crearUsuarioDTO(1L, "ACTIVO"));
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SancionDTO resultado = sancionService.actualizarSancion(1L, sancionDTO);

        // Then
        assertEquals(1L, resultado.getId());
        assertEquals("Nueva conducta", resultado.getMotivo());
        assertEquals("CERRADA", resultado.getEstado());
        assertEquals("ALTA", resultado.getSeveridad());

        verify(sancionRepository).findById(1L);
        verify(usuarioClient).buscarUsuarioPorId(1L);
        verify(sancionRepository).save(any(Sancion.class));
    }

    @Test
    void actualizarSancion_deberiaLanzarErrorSiNoExiste() {
        // Given
        SancionDTO sancionDTO = crearSancionDTO(
                null,
                1L,
                null,
                "Nueva conducta",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 10),
                "activa",
                "media"
        );

        when(sancionRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        SancionException exception = assertThrows(
                SancionException.class,
                () -> sancionService.actualizarSancion(99L, sancionDTO)
        );

        // Then
        assertEquals("Sanción no encontrada", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(sancionRepository).findById(99L);
        verify(sancionRepository, never()).save(any(Sancion.class));
    }

    @Test
    void cerrarSancion_deberiaCambiarEstadoACerrada() {
        // Given
        Sancion sancion = crearSancion(
                1L, 1L, null, "Conducta antideportiva",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10),
                "ACTIVA", "MEDIA"
        );

        when(sancionRepository.findById(1L)).thenReturn(Optional.of(sancion));
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        sancionService.cerrarSancion(1L);

        // Then
        assertEquals("CERRADA", sancion.getEstado());

        verify(sancionRepository).findById(1L);
        verify(sancionRepository).save(sancion);
    }

    @Test
    void listarPorUsuario_deberiaRetornarSancionesPorUsuario() {
        // Given
        Sancion sancion = crearSancion(
                1L, 1L, null, "Conducta antideportiva",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10),
                "ACTIVA", "MEDIA"
        );

        when(sancionRepository.findByUsuarioId(1L)).thenReturn(List.of(sancion));

        // When
        List<SancionDTO> resultado = sancionService.listarPorUsuario(1L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getUsuarioId());

        verify(sancionRepository).findByUsuarioId(1L);
    }

    @Test
    void listarPorEquipo_deberiaRetornarSancionesPorEquipo() {
        // Given
        Sancion sancion = crearSancion(
                1L, null, 2L, "Conducta antideportiva",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10),
                "ACTIVA", "MEDIA"
        );

        when(sancionRepository.findByEquipoId(2L)).thenReturn(List.of(sancion));

        // When
        List<SancionDTO> resultado = sancionService.listarPorEquipo(2L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).getEquipoId());

        verify(sancionRepository).findByEquipoId(2L);
    }

    @Test
    void listarPorEstado_deberiaRetornarSancionesPorEstado() {
        // Given
        Sancion sancion = crearSancion(
                1L, 1L, null, "Conducta antideportiva",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10),
                "ACTIVA", "MEDIA"
        );

        when(sancionRepository.findByEstado("ACTIVA")).thenReturn(List.of(sancion));

        // When
        List<SancionDTO> resultado = sancionService.listarPorEstado("activa");

        // Then
        assertEquals(1, resultado.size());
        assertEquals("ACTIVA", resultado.get(0).getEstado());

        verify(sancionRepository).findByEstado("ACTIVA");
    }

    @Test
    void existeSancionActivaUsuario_deberiaRetornarTrueSiExiste() {
        // Given
        when(sancionRepository.existsByUsuarioIdAndEstado(1L, "ACTIVA")).thenReturn(true);

        // When
        boolean resultado = sancionService.existeSancionActivaUsuario(1L);

        // Then
        assertTrue(resultado);

        verify(sancionRepository).existsByUsuarioIdAndEstado(1L, "ACTIVA");
    }

    @Test
    void existeSancionActivaEquipo_deberiaRetornarTrueSiExiste() {
        // Given
        when(sancionRepository.existsByEquipoIdAndEstado(2L, "ACTIVA")).thenReturn(true);

        // When
        boolean resultado = sancionService.existeSancionActivaEquipo(2L);

        // Then
        assertTrue(resultado);

        verify(sancionRepository).existsByEquipoIdAndEstado(2L, "ACTIVA");
    }

    private Sancion crearSancion(
            Long id,
            Long usuarioId,
            Long equipoId,
            String motivo,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String estado,
            String severidad) {

        Sancion sancion = new Sancion();
        sancion.setId(id);
        sancion.setUsuarioId(usuarioId);
        sancion.setEquipoId(equipoId);
        sancion.setMotivo(motivo);
        sancion.setFechaInicio(fechaInicio);
        sancion.setFechaFin(fechaFin);
        sancion.setEstado(estado);
        sancion.setSeveridad(severidad);
        return sancion;
    }

    private SancionDTO crearSancionDTO(
            Long id,
            Long usuarioId,
            Long equipoId,
            String motivo,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String estado,
            String severidad) {

        SancionDTO sancionDTO = new SancionDTO();
        sancionDTO.setId(id);
        sancionDTO.setUsuarioId(usuarioId);
        sancionDTO.setEquipoId(equipoId);
        sancionDTO.setMotivo(motivo);
        sancionDTO.setFechaInicio(fechaInicio);
        sancionDTO.setFechaFin(fechaFin);
        sancionDTO.setEstado(estado);
        sancionDTO.setSeveridad(severidad);
        return sancionDTO;
    }

    private UsuarioDTO crearUsuarioDTO(Long id, String estado) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(id);
        usuarioDTO.setEstado(estado);
        return usuarioDTO;
    }

    private EquipoDTO crearEquipoDTO(Long id, String estado) {
        EquipoDTO equipoDTO = new EquipoDTO();
        equipoDTO.setId(id);
        equipoDTO.setEstado(estado);
        return equipoDTO;
    }
}
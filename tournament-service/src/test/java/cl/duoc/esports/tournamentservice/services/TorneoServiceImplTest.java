package cl.duoc.esports.tournamentservice.services;

import cl.duoc.esports.tournamentservice.clients.JuegoClient;
import cl.duoc.esports.tournamentservice.dto.JuegoDTO;
import cl.duoc.esports.tournamentservice.dto.TorneoDTO;
import cl.duoc.esports.tournamentservice.exceptions.TorneoException;
import cl.duoc.esports.tournamentservice.models.Torneo;
import cl.duoc.esports.tournamentservice.repositories.TorneoRepository;
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
class TorneoServiceImplTest {

    @Mock
    private TorneoRepository torneoRepository;

    @Mock
    private JuegoClient juegoClient;

    @InjectMocks
    private TorneoServiceImpl torneoService;

    @Test
    void crearTorneo_deberiaCrearTorneoCorrectamente() {
        // Given
        TorneoDTO torneoDTO = crearTorneoDTO(
                null,
                "Torneo Apertura",
                1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10),
                16,
                "BORRADOR",
                "EQUIPO"
        );

        JuegoDTO juegoDTO = new JuegoDTO();
        juegoDTO.setEstado(true);

        when(juegoClient.buscarJuegoPorId(1L)).thenReturn(juegoDTO);
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(invocation -> {
            Torneo torneo = invocation.getArgument(0);
            torneo.setId(1L);
            return torneo;
        });

        // When
        TorneoDTO resultado = torneoService.crearTorneo(torneoDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Torneo Apertura", resultado.getNombre());
        assertEquals(1L, resultado.getJuegoId());
        assertEquals(LocalDate.of(2026, 6, 1), resultado.getFechaInicio());
        assertEquals(LocalDate.of(2026, 6, 10), resultado.getFechaFin());
        assertEquals(16, resultado.getCupoMaximo());
        assertEquals("BORRADOR", resultado.getEstado());
        assertEquals("EQUIPO", resultado.getModalidad());

        verify(juegoClient).buscarJuegoPorId(1L);
        verify(torneoRepository).save(any(Torneo.class));
    }

    @Test
    void crearTorneo_deberiaLanzarErrorSiFechaFinEsAnteriorAFechaInicio() {
        // Given
        TorneoDTO torneoDTO = crearTorneoDTO(
                null,
                "Torneo Inválido",
                1L,
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 1),
                16,
                "BORRADOR",
                "EQUIPO"
        );

        JuegoDTO juegoDTO = new JuegoDTO();
        juegoDTO.setEstado(true);

        when(juegoClient.buscarJuegoPorId(1L)).thenReturn(juegoDTO);

        // When
        TorneoException exception = assertThrows(
                TorneoException.class,
                () -> torneoService.crearTorneo(torneoDTO)
        );

        // Then
        assertEquals("La fecha de fin no puede ser anterior a la fecha de inicio", exception.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatus());

        verify(juegoClient).buscarJuegoPorId(1L);
        verify(torneoRepository, never()).save(any(Torneo.class));
    }

    @Test
    void crearTorneo_deberiaLanzarErrorSiJuegoNoEstaActivo() {
        // Given
        TorneoDTO torneoDTO = crearTorneoDTO(
                null,
                "Torneo Apertura",
                1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10),
                16,
                "BORRADOR",
                "EQUIPO"
        );

        JuegoDTO juegoDTO = new JuegoDTO();
        juegoDTO.setEstado(false);

        when(juegoClient.buscarJuegoPorId(1L)).thenReturn(juegoDTO);

        // When
        TorneoException exception = assertThrows(
                TorneoException.class,
                () -> torneoService.crearTorneo(torneoDTO)
        );

        // Then
        assertEquals("El juego no está activo", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(juegoClient).buscarJuegoPorId(1L);
        verify(torneoRepository, never()).save(any(Torneo.class));
    }

    @Test
    void crearTorneo_deberiaLanzarErrorSiGameServiceNoResponde() {
        // Given
        TorneoDTO torneoDTO = crearTorneoDTO(
                null,
                "Torneo Apertura",
                1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10),
                16,
                "BORRADOR",
                "EQUIPO"
        );

        when(juegoClient.buscarJuegoPorId(1L)).thenThrow(new RuntimeException("Servicio caído"));

        // When
        TorneoException exception = assertThrows(
                TorneoException.class,
                () -> torneoService.crearTorneo(torneoDTO)
        );

        // Then
        assertEquals("No se pudo validar el juego desde game-service", exception.getMessage());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());

        verify(juegoClient).buscarJuegoPorId(1L);
        verify(torneoRepository, never()).save(any(Torneo.class));
    }

    @Test
    void listarTorneos_deberiaRetornarListaDeTorneos() {
        // Given
        Torneo torneo1 = crearTorneo(
                1L,
                "Torneo Apertura",
                1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10),
                16,
                "BORRADOR",
                "EQUIPO"
        );

        Torneo torneo2 = crearTorneo(
                2L,
                "Torneo Clausura",
                1L,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 10),
                8,
                "ABIERTO",
                "EQUIPO"
        );

        when(torneoRepository.findAll()).thenReturn(List.of(torneo1, torneo2));

        // When
        List<TorneoDTO> resultado = torneoService.listarTorneos();

        // Then
        assertEquals(2, resultado.size());
        assertEquals("Torneo Apertura", resultado.get(0).getNombre());
        assertEquals("Torneo Clausura", resultado.get(1).getNombre());

        verify(torneoRepository).findAll();
    }

    @Test
    void buscarTorneoPorId_deberiaRetornarTorneoSiExiste() {
        // Given
        Torneo torneo = crearTorneo(
                1L,
                "Torneo Apertura",
                1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10),
                16,
                "BORRADOR",
                "EQUIPO"
        );

        when(torneoRepository.findById(1L)).thenReturn(Optional.of(torneo));

        // When
        TorneoDTO resultado = torneoService.buscarTorneoPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Torneo Apertura", resultado.getNombre());
        assertEquals(1L, resultado.getJuegoId());
        assertEquals("BORRADOR", resultado.getEstado());

        verify(torneoRepository).findById(1L);
    }

    @Test
    void buscarTorneoPorId_deberiaLanzarErrorSiNoExiste() {
        // Given
        when(torneoRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        TorneoException exception = assertThrows(
                TorneoException.class,
                () -> torneoService.buscarTorneoPorId(99L)
        );

        // Then
        assertEquals("Torneo no encontrado", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(torneoRepository).findById(99L);
    }

    @Test
    void actualizarTorneo_deberiaActualizarTorneoCorrectamente() {
        // Given
        Torneo torneoExistente = crearTorneo(
                1L,
                "Torneo Apertura",
                1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10),
                16,
                "BORRADOR",
                "EQUIPO"
        );

        TorneoDTO torneoDTO = crearTorneoDTO(
                null,
                "Torneo Actualizado",
                1L,
                LocalDate.of(2026, 6, 2),
                LocalDate.of(2026, 6, 12),
                32,
                "ABIERTO",
                "EQUIPO"
        );

        JuegoDTO juegoDTO = new JuegoDTO();
        juegoDTO.setEstado(true);

        when(torneoRepository.findById(1L)).thenReturn(Optional.of(torneoExistente));
        when(juegoClient.buscarJuegoPorId(1L)).thenReturn(juegoDTO);
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TorneoDTO resultado = torneoService.actualizarTorneo(1L, torneoDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Torneo Actualizado", resultado.getNombre());
        assertEquals(LocalDate.of(2026, 6, 2), resultado.getFechaInicio());
        assertEquals(LocalDate.of(2026, 6, 12), resultado.getFechaFin());
        assertEquals(32, resultado.getCupoMaximo());
        assertEquals("ABIERTO", resultado.getEstado());

        verify(torneoRepository).findById(1L);
        verify(juegoClient).buscarJuegoPorId(1L);
        verify(torneoRepository).save(any(Torneo.class));
    }

    @Test
    void actualizarTorneo_deberiaLanzarErrorSiNoExiste() {
        // Given
        TorneoDTO torneoDTO = crearTorneoDTO(
                null,
                "Torneo Actualizado",
                1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10),
                16,
                "ABIERTO",
                "EQUIPO"
        );

        when(torneoRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        TorneoException exception = assertThrows(
                TorneoException.class,
                () -> torneoService.actualizarTorneo(99L, torneoDTO)
        );

        // Then
        assertEquals("Torneo no encontrado", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(torneoRepository).findById(99L);
        verify(torneoRepository, never()).save(any(Torneo.class));
    }

    @Test
    void cancelarTorneo_deberiaCambiarEstadoACancelado() {
        // Given
        Torneo torneo = crearTorneo(
                1L,
                "Torneo Apertura",
                1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10),
                16,
                "BORRADOR",
                "EQUIPO"
        );

        when(torneoRepository.findById(1L)).thenReturn(Optional.of(torneo));
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        torneoService.cancelarTorneo(1L);

        // Then
        assertEquals("CANCELADO", torneo.getEstado());

        verify(torneoRepository).findById(1L);
        verify(torneoRepository).save(torneo);
    }

    @Test
    void cerrarTorneo_deberiaCambiarEstadoACerrado() {
        // Given
        Torneo torneo = crearTorneo(
                1L,
                "Torneo Apertura",
                1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10),
                16,
                "ABIERTO",
                "EQUIPO"
        );

        when(torneoRepository.findById(1L)).thenReturn(Optional.of(torneo));
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        torneoService.cerrarTorneo(1L);

        // Then
        assertEquals("CERRADO", torneo.getEstado());

        verify(torneoRepository).findById(1L);
        verify(torneoRepository).save(torneo);
    }

    @Test
    void listarPorJuego_deberiaRetornarTorneosPorJuego() {
        // Given
        Torneo torneo = crearTorneo(
                1L,
                "Torneo Apertura",
                1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10),
                16,
                "BORRADOR",
                "EQUIPO"
        );

        when(torneoRepository.findByJuegoId(1L)).thenReturn(List.of(torneo));

        // When
        List<TorneoDTO> resultado = torneoService.listarPorJuego(1L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getJuegoId());

        verify(torneoRepository).findByJuegoId(1L);
    }

    @Test
    void listarPorEstado_deberiaRetornarTorneosPorEstado() {
        // Given
        Torneo torneo = crearTorneo(
                1L,
                "Torneo Apertura",
                1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 10),
                16,
                "ABIERTO",
                "EQUIPO"
        );

        when(torneoRepository.findByEstado("ABIERTO")).thenReturn(List.of(torneo));

        // When
        List<TorneoDTO> resultado = torneoService.listarPorEstado("ABIERTO");

        // Then
        assertEquals(1, resultado.size());
        assertEquals("ABIERTO", resultado.get(0).getEstado());

        verify(torneoRepository).findByEstado("ABIERTO");
    }

    @Test
    void listarPorFecha_deberiaRetornarTorneosPorFechaInicio() {
        // Given
        LocalDate fecha = LocalDate.of(2026, 6, 1);

        Torneo torneo = crearTorneo(
                1L,
                "Torneo Apertura",
                1L,
                fecha,
                LocalDate.of(2026, 6, 10),
                16,
                "BORRADOR",
                "EQUIPO"
        );

        when(torneoRepository.findByFechaInicio(fecha)).thenReturn(List.of(torneo));

        // When
        List<TorneoDTO> resultado = torneoService.listarPorFecha(fecha);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(fecha, resultado.get(0).getFechaInicio());

        verify(torneoRepository).findByFechaInicio(fecha);
    }

    private Torneo crearTorneo(
            Long id,
            String nombre,
            Long juegoId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Integer cupoMaximo,
            String estado,
            String modalidad) {

        Torneo torneo = new Torneo();
        torneo.setId(id);
        torneo.setNombre(nombre);
        torneo.setJuegoId(juegoId);
        torneo.setFechaInicio(fechaInicio);
        torneo.setFechaFin(fechaFin);
        torneo.setCupoMaximo(cupoMaximo);
        torneo.setEstado(estado);
        torneo.setModalidad(modalidad);
        return torneo;
    }

    private TorneoDTO crearTorneoDTO(
            Long id,
            String nombre,
            Long juegoId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Integer cupoMaximo,
            String estado,
            String modalidad) {

        TorneoDTO torneoDTO = new TorneoDTO();
        torneoDTO.setId(id);
        torneoDTO.setNombre(nombre);
        torneoDTO.setJuegoId(juegoId);
        torneoDTO.setFechaInicio(fechaInicio);
        torneoDTO.setFechaFin(fechaFin);
        torneoDTO.setCupoMaximo(cupoMaximo);
        torneoDTO.setEstado(estado);
        torneoDTO.setModalidad(modalidad);
        return torneoDTO;
    }
}
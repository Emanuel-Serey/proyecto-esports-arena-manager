package cl.duoc.esports.matchservice.services;

import cl.duoc.esports.matchservice.clients.InscripcionClient;
import cl.duoc.esports.matchservice.clients.TorneoClient;
import cl.duoc.esports.matchservice.dto.InscripcionDTO;
import cl.duoc.esports.matchservice.dto.PartidaDTO;
import cl.duoc.esports.matchservice.dto.TorneoDTO;
import cl.duoc.esports.matchservice.exceptions.PartidaException;
import cl.duoc.esports.matchservice.models.Partida;
import cl.duoc.esports.matchservice.repositories.PartidaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartidaServiceImplTest {

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private TorneoClient torneoClient;

    @Mock
    private InscripcionClient inscripcionClient;

    @InjectMocks
    private PartidaServiceImpl partidaService;

    @Test
    void crearPartida_deberiaCrearPartidaCorrectamente() {
        // Given
        PartidaDTO partidaDTO = crearPartidaDTO(
                null,
                1L,
                10L,
                20L,
                "ronda 1",
                LocalDateTime.of(2026, 6, 2, 18, 0),
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(10L)).thenReturn(crearInscripcionDTO(10L, 1L, "APROBADA"));
        when(inscripcionClient.buscarInscripcionPorId(20L)).thenReturn(crearInscripcionDTO(20L, 1L, "APROBADA"));

        when(partidaRepository.existsByTorneoIdAndRondaAndParticipanteAIdAndParticipanteBId(
                1L, "RONDA 1", 10L, 20L
        )).thenReturn(false);

        when(partidaRepository.existsByTorneoIdAndRondaAndParticipanteBIdAndParticipanteAId(
                1L, "RONDA 1", 10L, 20L
        )).thenReturn(false);

        when(partidaRepository.save(any(Partida.class))).thenAnswer(invocation -> {
            Partida partida = invocation.getArgument(0);
            partida.setId(1L);
            return partida;
        });

        // When
        PartidaDTO resultado = partidaService.crearPartida(partidaDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getTorneoId());
        assertEquals(10L, resultado.getParticipanteAId());
        assertEquals(20L, resultado.getParticipanteBId());
        assertEquals("RONDA 1", resultado.getRonda());
        assertEquals("PROGRAMADA", resultado.getEstado());

        verify(torneoClient).buscarTorneoPorId(1L);
        verify(inscripcionClient).buscarInscripcionPorId(10L);
        verify(inscripcionClient).buscarInscripcionPorId(20L);
        verify(partidaRepository).save(any(Partida.class));
    }

    @Test
    void crearPartida_deberiaLanzarErrorSiParticipantesSonIguales() {
        // Given
        PartidaDTO partidaDTO = crearPartidaDTO(
                null,
                1L,
                10L,
                10L,
                "RONDA 1",
                LocalDateTime.of(2026, 6, 2, 18, 0),
                null
        );

        // When
        PartidaException exception = assertThrows(
                PartidaException.class,
                () -> partidaService.crearPartida(partidaDTO)
        );

        // Then
        assertEquals("No se puede crear una partida con el mismo participante", exception.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatus());

        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    void crearPartida_deberiaLanzarErrorSiTorneoEstaCancelado() {
        // Given
        PartidaDTO partidaDTO = crearPartidaDTO(
                null,
                1L,
                10L,
                20L,
                "RONDA 1",
                LocalDateTime.of(2026, 6, 2, 18, 0),
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("CANCELADO"));

        // When
        PartidaException exception = assertThrows(
                PartidaException.class,
                () -> partidaService.crearPartida(partidaDTO)
        );

        // Then
        assertEquals("No se puede crear una partida en un torneo cancelado", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    void crearPartida_deberiaLanzarErrorSiTorneoEstaCerrado() {
        // Given
        PartidaDTO partidaDTO = crearPartidaDTO(
                null,
                1L,
                10L,
                20L,
                "RONDA 1",
                LocalDateTime.of(2026, 6, 2, 18, 0),
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("CERRADO"));

        // When
        PartidaException exception = assertThrows(
                PartidaException.class,
                () -> partidaService.crearPartida(partidaDTO)
        );

        // Then
        assertEquals("No se puede crear una partida en un torneo cerrado", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    void crearPartida_deberiaLanzarErrorSiParticipanteNoPerteneceAlTorneo() {
        // Given
        PartidaDTO partidaDTO = crearPartidaDTO(
                null,
                1L,
                10L,
                20L,
                "RONDA 1",
                LocalDateTime.of(2026, 6, 2, 18, 0),
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(10L)).thenReturn(crearInscripcionDTO(10L, 99L, "APROBADA"));

        // When
        PartidaException exception = assertThrows(
                PartidaException.class,
                () -> partidaService.crearPartida(partidaDTO)
        );

        // Then
        assertEquals("El participante no está inscrito en este torneo", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    void crearPartida_deberiaLanzarErrorSiParticipanteTieneInscripcionCancelada() {
        // Given
        PartidaDTO partidaDTO = crearPartidaDTO(
                null,
                1L,
                10L,
                20L,
                "RONDA 1",
                LocalDateTime.of(2026, 6, 2, 18, 0),
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(10L)).thenReturn(crearInscripcionDTO(10L, 1L, "CANCELADA"));

        // When
        PartidaException exception = assertThrows(
                PartidaException.class,
                () -> partidaService.crearPartida(partidaDTO)
        );

        // Then
        assertEquals("No se puede crear una partida con una inscripción cancelada", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    void crearPartida_deberiaLanzarErrorSiPartidaYaExisteMismoOrden() {
        // Given
        PartidaDTO partidaDTO = crearPartidaDTO(
                null,
                1L,
                10L,
                20L,
                "ronda 1",
                LocalDateTime.of(2026, 6, 2, 18, 0),
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(10L)).thenReturn(crearInscripcionDTO(10L, 1L, "APROBADA"));
        when(inscripcionClient.buscarInscripcionPorId(20L)).thenReturn(crearInscripcionDTO(20L, 1L, "APROBADA"));

        when(partidaRepository.existsByTorneoIdAndRondaAndParticipanteAIdAndParticipanteBId(
                1L, "RONDA 1", 10L, 20L
        )).thenReturn(true);

        // When
        PartidaException exception = assertThrows(
                PartidaException.class,
                () -> partidaService.crearPartida(partidaDTO)
        );

        // Then
        assertEquals("Ya existe una partida entre estos participantes en la misma ronda", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    void crearPartida_deberiaLanzarErrorSiPartidaYaExisteOrdenInvertido() {
        // Given
        PartidaDTO partidaDTO = crearPartidaDTO(
                null,
                1L,
                10L,
                20L,
                "ronda 1",
                LocalDateTime.of(2026, 6, 2, 18, 0),
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(10L)).thenReturn(crearInscripcionDTO(10L, 1L, "APROBADA"));
        when(inscripcionClient.buscarInscripcionPorId(20L)).thenReturn(crearInscripcionDTO(20L, 1L, "APROBADA"));

        when(partidaRepository.existsByTorneoIdAndRondaAndParticipanteAIdAndParticipanteBId(
                1L, "RONDA 1", 10L, 20L
        )).thenReturn(false);

        when(partidaRepository.existsByTorneoIdAndRondaAndParticipanteBIdAndParticipanteAId(
                1L, "RONDA 1", 10L, 20L
        )).thenReturn(true);

        // When
        PartidaException exception = assertThrows(
                PartidaException.class,
                () -> partidaService.crearPartida(partidaDTO)
        );

        // Then
        assertEquals("Ya existe una partida entre estos participantes en la misma ronda", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    void crearPartida_deberiaLanzarErrorSiTournamentServiceNoResponde() {
        // Given
        PartidaDTO partidaDTO = crearPartidaDTO(
                null,
                1L,
                10L,
                20L,
                "RONDA 1",
                LocalDateTime.of(2026, 6, 2, 18, 0),
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenThrow(new RuntimeException("Servicio caído"));

        // When
        PartidaException exception = assertThrows(
                PartidaException.class,
                () -> partidaService.crearPartida(partidaDTO)
        );

        // Then
        assertEquals("No se pudo validar el torneo desde tournament-service", exception.getMessage());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());

        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    void crearPartida_deberiaLanzarErrorSiRegistrationServiceNoResponde() {
        // Given
        PartidaDTO partidaDTO = crearPartidaDTO(
                null,
                1L,
                10L,
                20L,
                "RONDA 1",
                LocalDateTime.of(2026, 6, 2, 18, 0),
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(10L)).thenThrow(new RuntimeException("Servicio caído"));

        // When
        PartidaException exception = assertThrows(
                PartidaException.class,
                () -> partidaService.crearPartida(partidaDTO)
        );

        // Then
        assertEquals("No se pudo validar el participante desde registration-service", exception.getMessage());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());

        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    void listarPartidas_deberiaRetornarListaDePartidas() {
        // Given
        Partida partida1 = crearPartida(1L, 1L, 10L, 20L, "RONDA 1", "PROGRAMADA");
        Partida partida2 = crearPartida(2L, 1L, 30L, 40L, "RONDA 2", "FINALIZADA");

        when(partidaRepository.findAll()).thenReturn(List.of(partida1, partida2));

        // When
        List<PartidaDTO> resultado = partidaService.listarPartidas();

        // Then
        assertEquals(2, resultado.size());
        assertEquals("RONDA 1", resultado.get(0).getRonda());
        assertEquals("RONDA 2", resultado.get(1).getRonda());

        verify(partidaRepository).findAll();
    }

    @Test
    void buscarPartidaPorId_deberiaRetornarPartidaSiExiste() {
        // Given
        Partida partida = crearPartida(1L, 1L, 10L, 20L, "RONDA 1", "PROGRAMADA");

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));

        // When
        PartidaDTO resultado = partidaService.buscarPartidaPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getTorneoId());
        assertEquals(10L, resultado.getParticipanteAId());
        assertEquals(20L, resultado.getParticipanteBId());
        assertEquals("PROGRAMADA", resultado.getEstado());

        verify(partidaRepository).findById(1L);
    }

    @Test
    void buscarPartidaPorId_deberiaLanzarErrorSiNoExiste() {
        // Given
        when(partidaRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        PartidaException exception = assertThrows(
                PartidaException.class,
                () -> partidaService.buscarPartidaPorId(99L)
        );

        // Then
        assertEquals("Partida no encontrada", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(partidaRepository).findById(99L);
    }

    @Test
    void actualizarPartida_deberiaActualizarPartidaCorrectamente() {
        // Given
        Partida partidaExistente = crearPartida(1L, 1L, 10L, 20L, "RONDA 1", "PROGRAMADA");

        PartidaDTO partidaDTO = crearPartidaDTO(
                null,
                1L,
                30L,
                40L,
                "ronda 2",
                LocalDateTime.of(2026, 6, 3, 20, 0),
                "finalizada"
        );

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partidaExistente));
        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(30L)).thenReturn(crearInscripcionDTO(30L, 1L, "APROBADA"));
        when(inscripcionClient.buscarInscripcionPorId(40L)).thenReturn(crearInscripcionDTO(40L, 1L, "APROBADA"));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PartidaDTO resultado = partidaService.actualizarPartida(1L, partidaDTO);

        // Then
        assertEquals(1L, resultado.getId());
        assertEquals(30L, resultado.getParticipanteAId());
        assertEquals(40L, resultado.getParticipanteBId());
        assertEquals("RONDA 2", resultado.getRonda());
        assertEquals("FINALIZADA", resultado.getEstado());

        verify(partidaRepository).findById(1L);
        verify(partidaRepository).save(any(Partida.class));
    }

    @Test
    void actualizarPartida_deberiaLanzarErrorSiNoExiste() {
        // Given
        PartidaDTO partidaDTO = crearPartidaDTO(
                null,
                1L,
                10L,
                20L,
                "RONDA 1",
                LocalDateTime.of(2026, 6, 2, 18, 0),
                "PROGRAMADA"
        );

        when(partidaRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        PartidaException exception = assertThrows(
                PartidaException.class,
                () -> partidaService.actualizarPartida(99L, partidaDTO)
        );

        // Then
        assertEquals("Partida no encontrada", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(partidaRepository).findById(99L);
        verify(partidaRepository, never()).save(any(Partida.class));
    }

    @Test
    void actualizarEstado_deberiaActualizarEstadoCorrectamente() {
        // Given
        Partida partida = crearPartida(1L, 1L, 10L, 20L, "RONDA 1", "PROGRAMADA");

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PartidaDTO resultado = partidaService.actualizarEstado(1L, "finalizada");

        // Then
        assertEquals("FINALIZADA", resultado.getEstado());

        verify(partidaRepository).findById(1L);
        verify(partidaRepository).save(partida);
    }

    @Test
    void cancelarPartida_deberiaCambiarEstadoACancelada() {
        // Given
        Partida partida = crearPartida(1L, 1L, 10L, 20L, "RONDA 1", "PROGRAMADA");

        when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        partidaService.cancelarPartida(1L);

        // Then
        assertEquals("CANCELADA", partida.getEstado());

        verify(partidaRepository).findById(1L);
        verify(partidaRepository).save(partida);
    }

    @Test
    void listarPorTorneo_deberiaRetornarPartidasPorTorneo() {
        // Given
        Partida partida = crearPartida(1L, 1L, 10L, 20L, "RONDA 1", "PROGRAMADA");

        when(partidaRepository.findByTorneoId(1L)).thenReturn(List.of(partida));

        // When
        List<PartidaDTO> resultado = partidaService.listarPorTorneo(1L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getTorneoId());

        verify(partidaRepository).findByTorneoId(1L);
    }

    @Test
    void listarPorRonda_deberiaRetornarPartidasPorRonda() {
        // Given
        Partida partida = crearPartida(1L, 1L, 10L, 20L, "RONDA 1", "PROGRAMADA");

        when(partidaRepository.findByRonda("RONDA 1")).thenReturn(List.of(partida));

        // When
        List<PartidaDTO> resultado = partidaService.listarPorRonda("ronda 1");

        // Then
        assertEquals(1, resultado.size());
        assertEquals("RONDA 1", resultado.get(0).getRonda());

        verify(partidaRepository).findByRonda("RONDA 1");
    }

    @Test
    void listarPorEstado_deberiaRetornarPartidasPorEstado() {
        // Given
        Partida partida = crearPartida(1L, 1L, 10L, 20L, "RONDA 1", "PROGRAMADA");

        when(partidaRepository.findByEstado("PROGRAMADA")).thenReturn(List.of(partida));

        // When
        List<PartidaDTO> resultado = partidaService.listarPorEstado("programada");

        // Then
        assertEquals(1, resultado.size());
        assertEquals("PROGRAMADA", resultado.get(0).getEstado());

        verify(partidaRepository).findByEstado("PROGRAMADA");
    }

    private Partida crearPartida(
            Long id,
            Long torneoId,
            Long participanteAId,
            Long participanteBId,
            String ronda,
            String estado) {

        Partida partida = new Partida();
        partida.setId(id);
        partida.setTorneoId(torneoId);
        partida.setParticipanteAId(participanteAId);
        partida.setParticipanteBId(participanteBId);
        partida.setRonda(ronda);
        partida.setFechaHora(LocalDateTime.of(2026, 6, 2, 18, 0));
        partida.setEstado(estado);
        return partida;
    }

    private PartidaDTO crearPartidaDTO(
            Long id,
            Long torneoId,
            Long participanteAId,
            Long participanteBId,
            String ronda,
            LocalDateTime fechaHora,
            String estado) {

        PartidaDTO partidaDTO = new PartidaDTO();
        partidaDTO.setId(id);
        partidaDTO.setTorneoId(torneoId);
        partidaDTO.setParticipanteAId(participanteAId);
        partidaDTO.setParticipanteBId(participanteBId);
        partidaDTO.setRonda(ronda);
        partidaDTO.setFechaHora(fechaHora);
        partidaDTO.setEstado(estado);
        return partidaDTO;
    }

    private TorneoDTO crearTorneoDTO(String estado) {
        TorneoDTO torneoDTO = new TorneoDTO();
        torneoDTO.setId(1L);
        torneoDTO.setEstado(estado);
        return torneoDTO;
    }

    private InscripcionDTO crearInscripcionDTO(Long id, Long torneoId, String estado) {
        InscripcionDTO inscripcionDTO = new InscripcionDTO();
        inscripcionDTO.setId(id);
        inscripcionDTO.setTorneoId(torneoId);
        inscripcionDTO.setEstado(estado);
        return inscripcionDTO;
    }
}
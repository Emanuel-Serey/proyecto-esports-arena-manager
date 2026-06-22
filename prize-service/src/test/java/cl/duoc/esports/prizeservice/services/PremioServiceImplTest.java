package cl.duoc.esports.prizeservice.services;

import cl.duoc.esports.prizeservice.clients.RankingClient;
import cl.duoc.esports.prizeservice.clients.TorneoClient;
import cl.duoc.esports.prizeservice.dto.PremioDTO;
import cl.duoc.esports.prizeservice.dto.RankingDTO;
import cl.duoc.esports.prizeservice.dto.TorneoDTO;
import cl.duoc.esports.prizeservice.exceptions.PremioException;
import cl.duoc.esports.prizeservice.models.Premio;
import cl.duoc.esports.prizeservice.repositories.PremioRepository;
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
class PremioServiceImplTest {

    @Mock
    private PremioRepository premioRepository;

    @Mock
    private TorneoClient torneoClient;

    @Mock
    private RankingClient rankingClient;

    @InjectMocks
    private PremioServiceImpl premioService;

    @Test
    void asignarPremio_deberiaAsignarPremioCorrectamente() {
        // Given
        PremioDTO premioDTO = crearPremioDTO(
                null,
                1L,
                10L,
                1,
                "MEDALLA",
                "Premio al primer lugar",
                null,
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(rankingClient.buscarRankingPorParticipante(1L, 10L)).thenReturn(crearRankingDTO(1L, 10L, 1));
        when(premioRepository.existsByTorneoIdAndParticipanteIdAndPosicion(1L, 10L, 1)).thenReturn(false);

        when(premioRepository.save(any(Premio.class))).thenAnswer(invocation -> {
            Premio premio = invocation.getArgument(0);
            premio.setId(1L);
            return premio;
        });

        // When
        PremioDTO resultado = premioService.asignarPremio(premioDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getTorneoId());
        assertEquals(10L, resultado.getParticipanteId());
        assertEquals(1, resultado.getPosicion());
        assertEquals("MEDALLA", resultado.getTipoPremio());
        assertEquals("PENDIENTE", resultado.getEstadoEntrega());
        assertNotNull(resultado.getFechaAsignacion());

        verify(torneoClient).buscarTorneoPorId(1L);
        verify(rankingClient).buscarRankingPorParticipante(1L, 10L);
        verify(premioRepository).save(any(Premio.class));
    }

    @Test
    void asignarPremio_deberiaLanzarErrorSiTorneoCancelado() {
        // Given
        PremioDTO premioDTO = crearPremioDTO(
                null,
                1L,
                10L,
                1,
                "MEDALLA",
                "Premio al primer lugar",
                null,
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("CANCELADO"));

        // When
        PremioException exception = assertThrows(
                PremioException.class,
                () -> premioService.asignarPremio(premioDTO)
        );

        // Then
        assertEquals("No se puede asignar premio en un torneo cancelado", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(premioRepository, never()).save(any(Premio.class));
    }

    @Test
    void asignarPremio_deberiaLanzarErrorSiPosicionNoCoincideConRanking() {
        // Given
        PremioDTO premioDTO = crearPremioDTO(
                null,
                1L,
                10L,
                1,
                "MEDALLA",
                "Premio al primer lugar",
                null,
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(rankingClient.buscarRankingPorParticipante(1L, 10L)).thenReturn(crearRankingDTO(1L, 10L, 2));

        // When
        PremioException exception = assertThrows(
                PremioException.class,
                () -> premioService.asignarPremio(premioDTO)
        );

        // Then
        assertEquals("La posición indicada no coincide con la posición del participante en el ranking", exception.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatus());

        verify(premioRepository, never()).save(any(Premio.class));
    }

    @Test
    void asignarPremio_deberiaLanzarErrorSiPremioYaFueAsignado() {
        // Given
        PremioDTO premioDTO = crearPremioDTO(
                null,
                1L,
                10L,
                1,
                "MEDALLA",
                "Premio al primer lugar",
                null,
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(rankingClient.buscarRankingPorParticipante(1L, 10L)).thenReturn(crearRankingDTO(1L, 10L, 1));
        when(premioRepository.existsByTorneoIdAndParticipanteIdAndPosicion(1L, 10L, 1)).thenReturn(true);

        // When
        PremioException exception = assertThrows(
                PremioException.class,
                () -> premioService.asignarPremio(premioDTO)
        );

        // Then
        assertEquals("El premio ya fue asignado a este participante en esta posición", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(premioRepository, never()).save(any(Premio.class));
    }

    @Test
    void asignarPremio_deberiaLanzarErrorSiTournamentServiceNoResponde() {
        // Given
        PremioDTO premioDTO = crearPremioDTO(
                null,
                1L,
                10L,
                1,
                "MEDALLA",
                "Premio al primer lugar",
                null,
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenThrow(new RuntimeException("Servicio caído"));

        // When
        PremioException exception = assertThrows(
                PremioException.class,
                () -> premioService.asignarPremio(premioDTO)
        );

        // Then
        assertEquals("No se pudo validar el torneo desde tournament-service", exception.getMessage());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());

        verify(premioRepository, never()).save(any(Premio.class));
    }

    @Test
    void asignarPremio_deberiaLanzarErrorSiRankingServiceNoResponde() {
        // Given
        PremioDTO premioDTO = crearPremioDTO(
                null,
                1L,
                10L,
                1,
                "MEDALLA",
                "Premio al primer lugar",
                null,
                null
        );

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(rankingClient.buscarRankingPorParticipante(1L, 10L)).thenThrow(new RuntimeException("Servicio caído"));

        // When
        PremioException exception = assertThrows(
                PremioException.class,
                () -> premioService.asignarPremio(premioDTO)
        );

        // Then
        assertEquals("No se pudo validar el ranking del participante desde ranking-service", exception.getMessage());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());

        verify(premioRepository, never()).save(any(Premio.class));
    }

    @Test
    void listarPremios_deberiaRetornarListaDePremios() {
        // Given
        Premio premio1 = crearPremio(1L, 1L, 10L, 1, "MEDALLA", "Primer lugar", "PENDIENTE");
        Premio premio2 = crearPremio(2L, 1L, 20L, 2, "DIPLOMA", "Segundo lugar", "ENTREGADO");

        when(premioRepository.findAll()).thenReturn(List.of(premio1, premio2));

        // When
        List<PremioDTO> resultado = premioService.listarPremios();

        // Then
        assertEquals(2, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstadoEntrega());
        assertEquals("ENTREGADO", resultado.get(1).getEstadoEntrega());

        verify(premioRepository).findAll();
    }

    @Test
    void buscarPremioPorId_deberiaRetornarPremioSiExiste() {
        // Given
        Premio premio = crearPremio(1L, 1L, 10L, 1, "MEDALLA", "Primer lugar", "PENDIENTE");

        when(premioRepository.findById(1L)).thenReturn(Optional.of(premio));

        // When
        PremioDTO resultado = premioService.buscarPremioPorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getTorneoId());
        assertEquals(10L, resultado.getParticipanteId());
        assertEquals("PENDIENTE", resultado.getEstadoEntrega());

        verify(premioRepository).findById(1L);
    }

    @Test
    void buscarPremioPorId_deberiaLanzarErrorSiNoExiste() {
        // Given
        when(premioRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        PremioException exception = assertThrows(
                PremioException.class,
                () -> premioService.buscarPremioPorId(99L)
        );

        // Then
        assertEquals("Premio no encontrado", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(premioRepository).findById(99L);
    }

    @Test
    void listarPremiosPorTorneo_deberiaRetornarPremiosPorTorneo() {
        // Given
        Premio premio = crearPremio(1L, 1L, 10L, 1, "MEDALLA", "Primer lugar", "PENDIENTE");

        when(premioRepository.findByTorneoId(1L)).thenReturn(List.of(premio));

        // When
        List<PremioDTO> resultado = premioService.listarPremiosPorTorneo(1L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getTorneoId());

        verify(premioRepository).findByTorneoId(1L);
    }

    @Test
    void listarPremiosPorParticipante_deberiaRetornarPremiosPorParticipante() {
        // Given
        Premio premio = crearPremio(1L, 1L, 10L, 1, "MEDALLA", "Primer lugar", "PENDIENTE");

        when(premioRepository.findByParticipanteId(10L)).thenReturn(List.of(premio));

        // When
        List<PremioDTO> resultado = premioService.listarPremiosPorParticipante(10L);

        // Then
        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getParticipanteId());

        verify(premioRepository).findByParticipanteId(10L);
    }

    @Test
    void listarPremiosPorEstado_deberiaRetornarPremiosPorEstado() {
        // Given
        Premio premio = crearPremio(1L, 1L, 10L, 1, "MEDALLA", "Primer lugar", "PENDIENTE");

        when(premioRepository.findByEstadoEntrega("PENDIENTE")).thenReturn(List.of(premio));

        // When
        List<PremioDTO> resultado = premioService.listarPremiosPorEstado("pendiente");

        // Then
        assertEquals(1, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstadoEntrega());

        verify(premioRepository).findByEstadoEntrega("PENDIENTE");
    }

    @Test
    void marcarComoEntregado_deberiaCambiarEstadoAEntregado() {
        // Given
        Premio premio = crearPremio(1L, 1L, 10L, 1, "MEDALLA", "Primer lugar", "PENDIENTE");

        when(premioRepository.findById(1L)).thenReturn(Optional.of(premio));
        when(premioRepository.save(any(Premio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PremioDTO resultado = premioService.marcarComoEntregado(1L);

        // Then
        assertEquals("ENTREGADO", resultado.getEstadoEntrega());

        verify(premioRepository).findById(1L);
        verify(premioRepository).save(premio);
    }

    @Test
    void marcarComoEntregado_deberiaLanzarErrorSiPremioEstaAnulado() {
        // Given
        Premio premio = crearPremio(1L, 1L, 10L, 1, "MEDALLA", "Primer lugar", "ANULADO");

        when(premioRepository.findById(1L)).thenReturn(Optional.of(premio));

        // When
        PremioException exception = assertThrows(
                PremioException.class,
                () -> premioService.marcarComoEntregado(1L)
        );

        // Then
        assertEquals("No se puede entregar un premio anulado", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(premioRepository, never()).save(any(Premio.class));
    }

    @Test
    void anularPremio_deberiaCambiarEstadoAAnulado() {
        // Given
        Premio premio = crearPremio(1L, 1L, 10L, 1, "MEDALLA", "Primer lugar", "PENDIENTE");

        when(premioRepository.findById(1L)).thenReturn(Optional.of(premio));
        when(premioRepository.save(any(Premio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        premioService.anularPremio(1L);

        // Then
        assertEquals("ANULADO", premio.getEstadoEntrega());

        verify(premioRepository).findById(1L);
        verify(premioRepository).save(premio);
    }

    @Test
    void anularPremio_deberiaLanzarErrorSiPremioYaEstaEntregado() {
        // Given
        Premio premio = crearPremio(1L, 1L, 10L, 1, "MEDALLA", "Primer lugar", "ENTREGADO");

        when(premioRepository.findById(1L)).thenReturn(Optional.of(premio));

        // When
        PremioException exception = assertThrows(
                PremioException.class,
                () -> premioService.anularPremio(1L)
        );

        // Then
        assertEquals("No se puede anular un premio ya entregado", exception.getMessage());

        verify(premioRepository, never()).save(any(Premio.class));
    }

    private Premio crearPremio(
            Long id,
            Long torneoId,
            Long participanteId,
            Integer posicion,
            String tipoPremio,
            String descripcion,
            String estadoEntrega) {

        Premio premio = new Premio();
        premio.setId(id);
        premio.setTorneoId(torneoId);
        premio.setParticipanteId(participanteId);
        premio.setPosicion(posicion);
        premio.setTipoPremio(tipoPremio);
        premio.setDescripcion(descripcion);
        premio.setEstadoEntrega(estadoEntrega);
        premio.setFechaAsignacion(LocalDate.now());
        return premio;
    }

    private PremioDTO crearPremioDTO(
            Long id,
            Long torneoId,
            Long participanteId,
            Integer posicion,
            String tipoPremio,
            String descripcion,
            String estadoEntrega,
            LocalDate fechaAsignacion) {

        PremioDTO premioDTO = new PremioDTO();
        premioDTO.setId(id);
        premioDTO.setTorneoId(torneoId);
        premioDTO.setParticipanteId(participanteId);
        premioDTO.setPosicion(posicion);
        premioDTO.setTipoPremio(tipoPremio);
        premioDTO.setDescripcion(descripcion);
        premioDTO.setEstadoEntrega(estadoEntrega);
        premioDTO.setFechaAsignacion(fechaAsignacion);
        return premioDTO;
    }

    private TorneoDTO crearTorneoDTO(String estado) {
        TorneoDTO torneoDTO = new TorneoDTO();
        torneoDTO.setId(1L);
        torneoDTO.setEstado(estado);
        return torneoDTO;
    }

    private RankingDTO crearRankingDTO(Long torneoId, Long participanteId, Integer posicion) {
        RankingDTO rankingDTO = new RankingDTO();
        rankingDTO.setTorneoId(torneoId);
        rankingDTO.setParticipanteId(participanteId);
        rankingDTO.setPosicion(posicion);
        return rankingDTO;
    }
}
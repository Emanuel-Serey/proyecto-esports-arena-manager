package cl.duoc.esports.rankingservice.services;

import cl.duoc.esports.rankingservice.clients.InscripcionClient;
import cl.duoc.esports.rankingservice.clients.ResultadoClient;
import cl.duoc.esports.rankingservice.clients.TorneoClient;
import cl.duoc.esports.rankingservice.dto.InscripcionDTO;
import cl.duoc.esports.rankingservice.dto.RankingDTO;
import cl.duoc.esports.rankingservice.dto.ResultadoDTO;
import cl.duoc.esports.rankingservice.dto.TorneoDTO;
import cl.duoc.esports.rankingservice.exceptions.RankingException;
import cl.duoc.esports.rankingservice.models.Ranking;
import cl.duoc.esports.rankingservice.repositories.RankingRepository;
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
class RankingServiceImplTest {

    @Mock
    private RankingRepository rankingRepository;

    @Mock
    private TorneoClient torneoClient;

    @Mock
    private InscripcionClient inscripcionClient;

    @Mock
    private ResultadoClient resultadoClient;

    @InjectMocks
    private RankingServiceImpl rankingService;

    @Test
    void crearRegistroRanking_deberiaCrearRankingCorrectamente() {
        RankingDTO rankingDTO = crearRankingDTO(null, 1L, 10L, 0, 0, 0, null, null);

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(10L)).thenReturn(crearInscripcionDTO(10L, 1L, "APROBADA"));
        when(rankingRepository.existsByTorneoIdAndParticipanteId(1L, 10L)).thenReturn(false);

        when(rankingRepository.save(any(Ranking.class))).thenAnswer(invocation -> {
            Ranking ranking = invocation.getArgument(0);
            ranking.setId(1L);
            return ranking;
        });

        when(rankingRepository.findByTorneoIdOrderByPuntosDescDiferenciaDescVictoriasDesc(1L))
                .thenAnswer(invocation -> List.of(crearRanking(1L, 1L, 10L, 0, 0, 0, 0, 1)));

        when(rankingRepository.findById(1L))
                .thenReturn(Optional.of(crearRanking(1L, 1L, 10L, 0, 0, 0, 0, 1)));

        RankingDTO resultado = rankingService.crearRegistroRanking(rankingDTO);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getTorneoId());
        assertEquals(10L, resultado.getParticipanteId());
        assertEquals(0, resultado.getDiferencia());

        verify(rankingRepository).save(any(Ranking.class));
        verify(rankingRepository).saveAll(anyList());
    }

    @Test
    void crearRegistroRanking_deberiaLanzarErrorSiTorneoCancelado() {
        RankingDTO rankingDTO = crearRankingDTO(null, 1L, 10L, 0, 0, 0, 0, null);

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("CANCELADO"));

        RankingException exception = assertThrows(
                RankingException.class,
                () -> rankingService.crearRegistroRanking(rankingDTO)
        );

        assertEquals("No se puede crear ranking para un torneo cancelado", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(rankingRepository, never()).save(any(Ranking.class));
    }

    @Test
    void crearRegistroRanking_deberiaLanzarErrorSiParticipanteNoPerteneceAlTorneo() {
        RankingDTO rankingDTO = crearRankingDTO(null, 1L, 10L, 0, 0, 0, 0, null);

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(10L)).thenReturn(crearInscripcionDTO(10L, 99L, "APROBADA"));

        RankingException exception = assertThrows(
                RankingException.class,
                () -> rankingService.crearRegistroRanking(rankingDTO)
        );

        assertEquals("El participante no pertenece a este torneo", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(rankingRepository, never()).save(any(Ranking.class));
    }

    @Test
    void crearRegistroRanking_deberiaLanzarErrorSiParticipanteTieneInscripcionCancelada() {
        RankingDTO rankingDTO = crearRankingDTO(null, 1L, 10L, 0, 0, 0, 0, null);

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(10L)).thenReturn(crearInscripcionDTO(10L, 1L, "CANCELADA"));

        RankingException exception = assertThrows(
                RankingException.class,
                () -> rankingService.crearRegistroRanking(rankingDTO)
        );

        assertEquals("No se puede usar una inscripción cancelada en el ranking", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(rankingRepository, never()).save(any(Ranking.class));
    }

    @Test
    void crearRegistroRanking_deberiaLanzarErrorSiParticipanteYaExisteEnRanking() {
        RankingDTO rankingDTO = crearRankingDTO(null, 1L, 10L, 0, 0, 0, 0, null);

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(10L)).thenReturn(crearInscripcionDTO(10L, 1L, "APROBADA"));
        when(rankingRepository.existsByTorneoIdAndParticipanteId(1L, 10L)).thenReturn(true);

        RankingException exception = assertThrows(
                RankingException.class,
                () -> rankingService.crearRegistroRanking(rankingDTO)
        );

        assertEquals("El participante ya existe en el ranking de este torneo", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(rankingRepository, never()).save(any(Ranking.class));
    }

    @Test
    void listarRankings_deberiaRetornarListaDeRankings() {
        Ranking ranking1 = crearRanking(1L, 1L, 10L, 3, 1, 0, 2, 1);
        Ranking ranking2 = crearRanking(2L, 1L, 20L, 0, 0, 1, -2, 2);

        when(rankingRepository.findAll()).thenReturn(List.of(ranking1, ranking2));

        List<RankingDTO> resultado = rankingService.listarRankings();

        assertEquals(2, resultado.size());
        assertEquals(10L, resultado.get(0).getParticipanteId());
        assertEquals(20L, resultado.get(1).getParticipanteId());

        verify(rankingRepository).findAll();
    }

    @Test
    void buscarRankingPorId_deberiaRetornarRankingSiExiste() {
        Ranking ranking = crearRanking(1L, 1L, 10L, 3, 1, 0, 2, 1);

        when(rankingRepository.findById(1L)).thenReturn(Optional.of(ranking));

        RankingDTO resultado = rankingService.buscarRankingPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(10L, resultado.getParticipanteId());
        assertEquals(3, resultado.getPuntos());

        verify(rankingRepository).findById(1L);
    }

    @Test
    void buscarRankingPorId_deberiaLanzarErrorSiNoExiste() {
        when(rankingRepository.findById(99L)).thenReturn(Optional.empty());

        RankingException exception = assertThrows(
                RankingException.class,
                () -> rankingService.buscarRankingPorId(99L)
        );

        assertEquals("Registro de ranking no encontrado", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(rankingRepository).findById(99L);
    }

    @Test
    void listarRankingPorTorneo_deberiaRetornarRankingOrdenadoPorTorneo() {
        Ranking ranking = crearRanking(1L, 1L, 10L, 3, 1, 0, 2, 1);

        when(rankingRepository.findByTorneoIdOrderByPosicionAsc(1L)).thenReturn(List.of(ranking));

        List<RankingDTO> resultado = rankingService.listarRankingPorTorneo(1L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getTorneoId());

        verify(rankingRepository).findByTorneoIdOrderByPosicionAsc(1L);
    }

    @Test
    void buscarPosicionPorParticipante_deberiaRetornarRankingSiExiste() {
        Ranking ranking = crearRanking(1L, 1L, 10L, 3, 1, 0, 2, 1);

        when(rankingRepository.findByTorneoIdAndParticipanteId(1L, 10L))
                .thenReturn(Optional.of(ranking));

        RankingDTO resultado = rankingService.buscarPosicionPorParticipante(1L, 10L);

        assertEquals(1L, resultado.getTorneoId());
        assertEquals(10L, resultado.getParticipanteId());
        assertEquals(1, resultado.getPosicion());

        verify(rankingRepository).findByTorneoIdAndParticipanteId(1L, 10L);
    }

    @Test
    void buscarPosicionPorParticipante_deberiaLanzarErrorSiNoExiste() {
        when(rankingRepository.findByTorneoIdAndParticipanteId(1L, 99L))
                .thenReturn(Optional.empty());

        RankingException exception = assertThrows(
                RankingException.class,
                () -> rankingService.buscarPosicionPorParticipante(1L, 99L)
        );

        assertEquals("El participante no existe en el ranking de este torneo", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void actualizarRanking_deberiaActualizarRankingCorrectamente() {
        Ranking ranking = crearRanking(1L, 1L, 10L, 0, 0, 0, 0, 0);
        RankingDTO rankingDTO = crearRankingDTO(null, null, null, 6, 2, 1, null, null);

        when(rankingRepository.findById(1L))
                .thenReturn(Optional.of(ranking))
                .thenReturn(Optional.of(crearRanking(1L, 1L, 10L, 6, 2, 1, 0, 1)));

        when(rankingRepository.save(any(Ranking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rankingRepository.findByTorneoIdOrderByPuntosDescDiferenciaDescVictoriasDesc(1L))
                .thenReturn(List.of(ranking));

        RankingDTO resultado = rankingService.actualizarRanking(1L, rankingDTO);

        assertEquals(6, resultado.getPuntos());
        assertEquals(2, resultado.getVictorias());
        assertEquals(1, resultado.getDerrotas());
        assertEquals(0, resultado.getDiferencia());

        verify(rankingRepository).save(any(Ranking.class));
        verify(rankingRepository).saveAll(anyList());
    }

    @Test
    void actualizarRankingPorResultado_deberiaActualizarRankingCorrectamente() {
        Ranking rankingGanador = crearRanking(1L, 1L, 10L, 0, 0, 0, 0, 0);
        Ranking rankingPerdedor = crearRanking(2L, 1L, 20L, 0, 0, 0, 0, 0);

        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(10L)).thenReturn(crearInscripcionDTO(10L, 1L, "APROBADA"));
        when(inscripcionClient.buscarInscripcionPorId(20L)).thenReturn(crearInscripcionDTO(20L, 1L, "APROBADA"));
        when(resultadoClient.buscarResultadoPorId(5L)).thenReturn(crearResultadoDTO(5L, 10L, 3, 1, "VALIDADO"));

        when(rankingRepository.findByTorneoIdAndParticipanteId(1L, 10L)).thenReturn(Optional.of(rankingGanador));
        when(rankingRepository.findByTorneoIdAndParticipanteId(1L, 20L)).thenReturn(Optional.of(rankingPerdedor));

        when(rankingRepository.save(any(Ranking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rankingRepository.findByTorneoIdOrderByPuntosDescDiferenciaDescVictoriasDesc(1L))
                .thenReturn(List.of(rankingGanador, rankingPerdedor));

        when(rankingRepository.findById(1L)).thenReturn(Optional.of(rankingGanador));

        RankingDTO resultado = rankingService.actualizarRankingPorResultado(5L, 1L, 10L, 20L);

        assertEquals(10L, resultado.getParticipanteId());
        assertEquals(3, resultado.getPuntos());
        assertEquals(1, resultado.getVictorias());
        assertEquals(2, resultado.getDiferencia());

        assertEquals(1, rankingPerdedor.getDerrotas());
        assertEquals(-2, rankingPerdedor.getDiferencia());

        verify(rankingRepository, times(2)).save(any(Ranking.class));
        verify(rankingRepository).saveAll(anyList());
    }

    @Test
    void actualizarRankingPorResultado_deberiaLanzarErrorSiResultadoNoEstaValidado() {
        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(10L)).thenReturn(crearInscripcionDTO(10L, 1L, "APROBADA"));
        when(inscripcionClient.buscarInscripcionPorId(20L)).thenReturn(crearInscripcionDTO(20L, 1L, "APROBADA"));
        when(resultadoClient.buscarResultadoPorId(5L)).thenReturn(crearResultadoDTO(5L, 10L, 3, 1, "PENDIENTE"));

        RankingException exception = assertThrows(
                RankingException.class,
                () -> rankingService.actualizarRankingPorResultado(5L, 1L, 10L, 20L)
        );

        assertEquals("Solo resultados validados pueden actualizar el ranking", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(rankingRepository, never()).save(any(Ranking.class));
    }

    @Test
    void actualizarRankingPorResultado_deberiaLanzarErrorSiGanadorNoCorresponde() {
        when(torneoClient.buscarTorneoPorId(1L)).thenReturn(crearTorneoDTO("ABIERTO"));
        when(inscripcionClient.buscarInscripcionPorId(10L)).thenReturn(crearInscripcionDTO(10L, 1L, "APROBADA"));
        when(inscripcionClient.buscarInscripcionPorId(20L)).thenReturn(crearInscripcionDTO(20L, 1L, "APROBADA"));
        when(resultadoClient.buscarResultadoPorId(5L)).thenReturn(crearResultadoDTO(5L, 99L, 3, 1, "VALIDADO"));

        RankingException exception = assertThrows(
                RankingException.class,
                () -> rankingService.actualizarRankingPorResultado(5L, 1L, 10L, 20L)
        );

        assertEquals("El ganador no corresponde a los participantes indicados", exception.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatus());

        verify(rankingRepository, never()).save(any(Ranking.class));
    }

    @Test
    void reiniciarRankingPorTorneo_deberiaEliminarRankingsDelTorneo() {
        Ranking ranking1 = crearRanking(1L, 1L, 10L, 3, 1, 0, 2, 1);
        Ranking ranking2 = crearRanking(2L, 1L, 20L, 0, 0, 1, -2, 2);

        when(rankingRepository.findByTorneoIdOrderByPosicionAsc(1L)).thenReturn(List.of(ranking1, ranking2));

        rankingService.reiniciarRankingPorTorneo(1L);

        verify(rankingRepository).deleteAll(List.of(ranking1, ranking2));
    }

    @Test
    void reiniciarRankingPorTorneo_deberiaLanzarErrorSiNoExistenRegistros() {
        when(rankingRepository.findByTorneoIdOrderByPosicionAsc(1L)).thenReturn(List.of());

        RankingException exception = assertThrows(
                RankingException.class,
                () -> rankingService.reiniciarRankingPorTorneo(1L)
        );

        assertEquals("No existen registros de ranking para este torneo", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(rankingRepository, never()).deleteAll(anyList());
    }

    private Ranking crearRanking(
            Long id,
            Long torneoId,
            Long participanteId,
            Integer puntos,
            Integer victorias,
            Integer derrotas,
            Integer diferencia,
            Integer posicion) {

        Ranking ranking = new Ranking();
        ranking.setId(id);
        ranking.setTorneoId(torneoId);
        ranking.setParticipanteId(participanteId);
        ranking.setPuntos(puntos);
        ranking.setVictorias(victorias);
        ranking.setDerrotas(derrotas);
        ranking.setDiferencia(diferencia);
        ranking.setPosicion(posicion);
        return ranking;
    }

    private RankingDTO crearRankingDTO(
            Long id,
            Long torneoId,
            Long participanteId,
            Integer puntos,
            Integer victorias,
            Integer derrotas,
            Integer diferencia,
            Integer posicion) {

        RankingDTO rankingDTO = new RankingDTO();
        rankingDTO.setId(id);
        rankingDTO.setTorneoId(torneoId);
        rankingDTO.setParticipanteId(participanteId);
        rankingDTO.setPuntos(puntos);
        rankingDTO.setVictorias(victorias);
        rankingDTO.setDerrotas(derrotas);
        rankingDTO.setDiferencia(diferencia);
        rankingDTO.setPosicion(posicion);
        return rankingDTO;
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

    private ResultadoDTO crearResultadoDTO(
            Long id,
            Long ganadorId,
            Integer puntajeA,
            Integer puntajeB,
            String estadoValidacion) {

        ResultadoDTO resultadoDTO = new ResultadoDTO();
        resultadoDTO.setId(id);
        resultadoDTO.setGanadorId(ganadorId);
        resultadoDTO.setPuntajeA(puntajeA);
        resultadoDTO.setPuntajeB(puntajeB);
        resultadoDTO.setEstadoValidacion(estadoValidacion);
        return resultadoDTO;
    }
}
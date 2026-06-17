package cl.duoc.esports.resultservice.services;

import cl.duoc.esports.resultservice.clients.PartidaClient;
import cl.duoc.esports.resultservice.dto.PartidaDTO;
import cl.duoc.esports.resultservice.dto.ResultadoDTO;
import cl.duoc.esports.resultservice.exceptions.ResultadoException;
import cl.duoc.esports.resultservice.models.Resultado;
import cl.duoc.esports.resultservice.repositories.ResultadoRepository;
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
class ResultadoServiceImplTest {

    @Mock
    private ResultadoRepository resultadoRepository;

    @Mock
    private PartidaClient partidaClient;

    @InjectMocks
    private ResultadoServiceImpl resultadoService;

    @Test
    void crearResultado_deberiaCrearResultadoCorrectamente() {
        // Given
        ResultadoDTO resultadoDTO = crearResultadoDTO(
                null,
                1L,
                10L,
                2,
                1,
                null,
                null
        );

        when(partidaClient.buscarPartidaPorId(1L))
                .thenReturn(crearPartidaDTO(1L, 10L, 20L, "PROGRAMADA"));

        when(resultadoRepository.existsByPartidaId(1L)).thenReturn(false);

        when(resultadoRepository.save(any(Resultado.class))).thenAnswer(invocation -> {
            Resultado resultado = invocation.getArgument(0);
            resultado.setId(1L);
            return resultado;
        });

        // When
        ResultadoDTO resultado = resultadoService.crearResultado(resultadoDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getPartidaId());
        assertEquals(10L, resultado.getGanadorId());
        assertEquals(2, resultado.getPuntajeA());
        assertEquals(1, resultado.getPuntajeB());
        assertEquals("PENDIENTE", resultado.getEstadoValidacion());
        assertNotNull(resultado.getFechaRegistro());

        verify(partidaClient).buscarPartidaPorId(1L);
        verify(resultadoRepository).existsByPartidaId(1L);
        verify(resultadoRepository).save(any(Resultado.class));
    }

    @Test
    void crearResultado_deberiaLanzarErrorSiPartidaEstaCancelada() {
        // Given
        ResultadoDTO resultadoDTO = crearResultadoDTO(
                null,
                1L,
                10L,
                2,
                1,
                null,
                null
        );

        when(partidaClient.buscarPartidaPorId(1L))
                .thenReturn(crearPartidaDTO(1L, 10L, 20L, "CANCELADA"));

        // When
        ResultadoException exception = assertThrows(
                ResultadoException.class,
                () -> resultadoService.crearResultado(resultadoDTO)
        );

        // Then
        assertEquals("No se puede registrar resultado de una partida cancelada", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(resultadoRepository, never()).save(any(Resultado.class));
    }

    @Test
    void crearResultado_deberiaLanzarErrorSiGanadorNoPerteneceALaPartida() {
        // Given
        ResultadoDTO resultadoDTO = crearResultadoDTO(
                null,
                1L,
                99L,
                2,
                1,
                null,
                null
        );

        when(partidaClient.buscarPartidaPorId(1L))
                .thenReturn(crearPartidaDTO(1L, 10L, 20L, "PROGRAMADA"));

        // When
        ResultadoException exception = assertThrows(
                ResultadoException.class,
                () -> resultadoService.crearResultado(resultadoDTO)
        );

        // Then
        assertEquals("El ganador debe ser uno de los participantes de la partida", exception.getMessage());
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatus());

        verify(resultadoRepository, never()).save(any(Resultado.class));
    }

    @Test
    void crearResultado_deberiaLanzarErrorSiPartidaYaTieneResultado() {
        // Given
        ResultadoDTO resultadoDTO = crearResultadoDTO(
                null,
                1L,
                10L,
                2,
                1,
                null,
                null
        );

        when(partidaClient.buscarPartidaPorId(1L))
                .thenReturn(crearPartidaDTO(1L, 10L, 20L, "PROGRAMADA"));

        when(resultadoRepository.existsByPartidaId(1L)).thenReturn(true);

        // When
        ResultadoException exception = assertThrows(
                ResultadoException.class,
                () -> resultadoService.crearResultado(resultadoDTO)
        );

        // Then
        assertEquals("La partida ya tiene un resultado registrado", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(resultadoRepository, never()).save(any(Resultado.class));
    }

    @Test
    void crearResultado_deberiaLanzarErrorSiMatchServiceNoResponde() {
        // Given
        ResultadoDTO resultadoDTO = crearResultadoDTO(
                null,
                1L,
                10L,
                2,
                1,
                null,
                null
        );

        when(partidaClient.buscarPartidaPorId(1L))
                .thenThrow(new RuntimeException("Servicio caído"));

        // When
        ResultadoException exception = assertThrows(
                ResultadoException.class,
                () -> resultadoService.crearResultado(resultadoDTO)
        );

        // Then
        assertEquals("No se pudo validar la partida desde match-service", exception.getMessage());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());

        verify(resultadoRepository, never()).save(any(Resultado.class));
    }

    @Test
    void listarResultados_deberiaRetornarListaDeResultados() {
        // Given
        Resultado resultado1 = crearResultado(
                1L,
                1L,
                10L,
                2,
                1,
                "PENDIENTE"
        );

        Resultado resultado2 = crearResultado(
                2L,
                2L,
                30L,
                3,
                0,
                "VALIDADO"
        );

        when(resultadoRepository.findAll()).thenReturn(List.of(resultado1, resultado2));

        // When
        List<ResultadoDTO> resultado = resultadoService.listarResultados();

        // Then
        assertEquals(2, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstadoValidacion());
        assertEquals("VALIDADO", resultado.get(1).getEstadoValidacion());

        verify(resultadoRepository).findAll();
    }

    @Test
    void buscarResultadoPorId_deberiaRetornarResultadoSiExiste() {
        // Given
        Resultado resultado = crearResultado(
                1L,
                1L,
                10L,
                2,
                1,
                "PENDIENTE"
        );

        when(resultadoRepository.findById(1L)).thenReturn(Optional.of(resultado));

        // When
        ResultadoDTO resultadoDTO = resultadoService.buscarResultadoPorId(1L);

        // Then
        assertNotNull(resultadoDTO);
        assertEquals(1L, resultadoDTO.getId());
        assertEquals(1L, resultadoDTO.getPartidaId());
        assertEquals(10L, resultadoDTO.getGanadorId());
        assertEquals("PENDIENTE", resultadoDTO.getEstadoValidacion());

        verify(resultadoRepository).findById(1L);
    }

    @Test
    void buscarResultadoPorId_deberiaLanzarErrorSiNoExiste() {
        // Given
        when(resultadoRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        ResultadoException exception = assertThrows(
                ResultadoException.class,
                () -> resultadoService.buscarResultadoPorId(99L)
        );

        // Then
        assertEquals("Resultado no encontrado", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(resultadoRepository).findById(99L);
    }

    @Test
    void actualizarResultado_deberiaActualizarResultadoCorrectamente() {
        // Given
        Resultado resultadoExistente = crearResultado(
                1L,
                1L,
                10L,
                2,
                1,
                "PENDIENTE"
        );

        ResultadoDTO resultadoDTO = crearResultadoDTO(
                null,
                1L,
                20L,
                1,
                3,
                null,
                null
        );

        when(resultadoRepository.findById(1L)).thenReturn(Optional.of(resultadoExistente));
        when(partidaClient.buscarPartidaPorId(1L))
                .thenReturn(crearPartidaDTO(1L, 10L, 20L, "PROGRAMADA"));

        when(resultadoRepository.save(any(Resultado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ResultadoDTO resultado = resultadoService.actualizarResultado(1L, resultadoDTO);

        // Then
        assertEquals(1L, resultado.getId());
        assertEquals(20L, resultado.getGanadorId());
        assertEquals(1, resultado.getPuntajeA());
        assertEquals(3, resultado.getPuntajeB());

        verify(resultadoRepository).findById(1L);
        verify(partidaClient).buscarPartidaPorId(1L);
        verify(resultadoRepository).save(any(Resultado.class));
    }

    @Test
    void actualizarResultado_deberiaLanzarErrorSiResultadoEstaValidado() {
        // Given
        Resultado resultadoExistente = crearResultado(
                1L,
                1L,
                10L,
                2,
                1,
                "VALIDADO"
        );

        ResultadoDTO resultadoDTO = crearResultadoDTO(
                null,
                1L,
                20L,
                1,
                3,
                null,
                null
        );

        when(resultadoRepository.findById(1L)).thenReturn(Optional.of(resultadoExistente));

        // When
        ResultadoException exception = assertThrows(
                ResultadoException.class,
                () -> resultadoService.actualizarResultado(1L, resultadoDTO)
        );

        // Then
        assertEquals("No se puede modificar un resultado validado", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(resultadoRepository, never()).save(any(Resultado.class));
    }

    @Test
    void actualizarResultado_deberiaLanzarErrorSiResultadoEstaAnulado() {
        // Given
        Resultado resultadoExistente = crearResultado(
                1L,
                1L,
                10L,
                2,
                1,
                "ANULADO"
        );

        ResultadoDTO resultadoDTO = crearResultadoDTO(
                null,
                1L,
                20L,
                1,
                3,
                null,
                null
        );

        when(resultadoRepository.findById(1L)).thenReturn(Optional.of(resultadoExistente));

        // When
        ResultadoException exception = assertThrows(
                ResultadoException.class,
                () -> resultadoService.actualizarResultado(1L, resultadoDTO)
        );

        // Then
        assertEquals("No se puede modificar un resultado anulado", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(resultadoRepository, never()).save(any(Resultado.class));
    }

    @Test
    void validarResultado_deberiaCambiarEstadoAValidado() {
        // Given
        Resultado resultadoExistente = crearResultado(
                1L,
                1L,
                10L,
                2,
                1,
                "PENDIENTE"
        );

        when(resultadoRepository.findById(1L)).thenReturn(Optional.of(resultadoExistente));
        when(resultadoRepository.save(any(Resultado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ResultadoDTO resultado = resultadoService.validarResultado(1L);

        // Then
        assertEquals("VALIDADO", resultado.getEstadoValidacion());

        verify(resultadoRepository).findById(1L);
        verify(resultadoRepository).save(resultadoExistente);
    }

    @Test
    void validarResultado_deberiaLanzarErrorSiResultadoEstaAnulado() {
        // Given
        Resultado resultadoExistente = crearResultado(
                1L,
                1L,
                10L,
                2,
                1,
                "ANULADO"
        );

        when(resultadoRepository.findById(1L)).thenReturn(Optional.of(resultadoExistente));

        // When
        ResultadoException exception = assertThrows(
                ResultadoException.class,
                () -> resultadoService.validarResultado(1L)
        );

        // Then
        assertEquals("No se puede validar un resultado anulado", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(resultadoRepository, never()).save(any(Resultado.class));
    }

    @Test
    void anularResultado_deberiaCambiarEstadoAAnulado() {
        // Given
        Resultado resultadoExistente = crearResultado(
                1L,
                1L,
                10L,
                2,
                1,
                "PENDIENTE"
        );

        when(resultadoRepository.findById(1L)).thenReturn(Optional.of(resultadoExistente));
        when(resultadoRepository.save(any(Resultado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        resultadoService.anularResultado(1L, "Error en el registro");

        // Then
        assertEquals("ANULADO", resultadoExistente.getEstadoValidacion());

        verify(resultadoRepository).findById(1L);
        verify(resultadoRepository).save(resultadoExistente);
    }

    @Test
    void anularResultado_deberiaLanzarErrorSiNoTieneJustificacion() {
        // Given
        Resultado resultadoExistente = crearResultado(
                1L,
                1L,
                10L,
                2,
                1,
                "PENDIENTE"
        );

        when(resultadoRepository.findById(1L)).thenReturn(Optional.of(resultadoExistente));

        // When
        ResultadoException exception = assertThrows(
                ResultadoException.class,
                () -> resultadoService.anularResultado(1L, " ")
        );

        // Then
        assertEquals("Debe indicar una justificación para anular el resultado", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        verify(resultadoRepository, never()).save(any(Resultado.class));
    }

    @Test
    void listarPorPartida_deberiaRetornarResultadosPorPartida() {
        // Given
        Resultado resultado = crearResultado(
                1L,
                1L,
                10L,
                2,
                1,
                "PENDIENTE"
        );

        when(resultadoRepository.findByPartidaId(1L)).thenReturn(List.of(resultado));

        // When
        List<ResultadoDTO> resultadoDTO = resultadoService.listarPorPartida(1L);

        // Then
        assertEquals(1, resultadoDTO.size());
        assertEquals(1L, resultadoDTO.get(0).getPartidaId());

        verify(resultadoRepository).findByPartidaId(1L);
    }

    private Resultado crearResultado(
            Long id,
            Long partidaId,
            Long ganadorId,
            Integer puntajeA,
            Integer puntajeB,
            String estadoValidacion) {

        Resultado resultado = new Resultado();
        resultado.setId(id);
        resultado.setPartidaId(partidaId);
        resultado.setGanadorId(ganadorId);
        resultado.setPuntajeA(puntajeA);
        resultado.setPuntajeB(puntajeB);
        resultado.setEstadoValidacion(estadoValidacion);
        resultado.setFechaRegistro(LocalDate.now());
        return resultado;
    }

    private ResultadoDTO crearResultadoDTO(
            Long id,
            Long partidaId,
            Long ganadorId,
            Integer puntajeA,
            Integer puntajeB,
            String estadoValidacion,
            LocalDate fechaRegistro) {

        ResultadoDTO resultadoDTO = new ResultadoDTO();
        resultadoDTO.setId(id);
        resultadoDTO.setPartidaId(partidaId);
        resultadoDTO.setGanadorId(ganadorId);
        resultadoDTO.setPuntajeA(puntajeA);
        resultadoDTO.setPuntajeB(puntajeB);
        resultadoDTO.setEstadoValidacion(estadoValidacion);
        resultadoDTO.setFechaRegistro(fechaRegistro);
        return resultadoDTO;
    }

    private PartidaDTO crearPartidaDTO(
            Long id,
            Long participanteAId,
            Long participanteBId,
            String estado) {

        PartidaDTO partidaDTO = new PartidaDTO();
        partidaDTO.setId(id);
        partidaDTO.setParticipanteAId(participanteAId);
        partidaDTO.setParticipanteBId(participanteBId);
        partidaDTO.setEstado(estado);
        return partidaDTO;
    }
}
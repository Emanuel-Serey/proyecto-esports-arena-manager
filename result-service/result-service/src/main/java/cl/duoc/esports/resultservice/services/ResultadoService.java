package cl.duoc.esports.resultservice.services;

import cl.duoc.esports.resultservice.dto.ResultadoDTO;

import java.util.List;

public interface ResultadoService {

    ResultadoDTO crearResultado(ResultadoDTO resultadoDTO);

    List<ResultadoDTO> listarResultados();

    ResultadoDTO buscarResultadoPorId(Long id);

    ResultadoDTO actualizarResultado(Long id, ResultadoDTO resultadoDTO);

    ResultadoDTO validarResultado(Long id);

    void anularResultado(Long id, String justificacion);

    List<ResultadoDTO> listarPorPartida(Long partidaId);
}
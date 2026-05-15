package cl.duoc.esports.matchservice.services;

import cl.duoc.esports.matchservice.dto.PartidaDTO;

import java.util.List;

public interface PartidaService {

    PartidaDTO crearPartida(PartidaDTO partidaDTO);

    List<PartidaDTO> listarPartidas();

    PartidaDTO buscarPartidaPorId(Long id);

    PartidaDTO actualizarPartida(Long id, PartidaDTO partidaDTO);

    PartidaDTO actualizarEstado(Long id, String estado);

    void cancelarPartida(Long id);

    List<PartidaDTO> listarPorTorneo(Long torneoId);

    List<PartidaDTO> listarPorRonda(String ronda);

    List<PartidaDTO> listarPorEstado(String estado);
}
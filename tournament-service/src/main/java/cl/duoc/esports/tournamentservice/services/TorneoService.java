package cl.duoc.esports.tournamentservice.services;

import cl.duoc.esports.tournamentservice.dto.TorneoDTO;

import java.time.LocalDate;
import java.util.List;

public interface TorneoService {

    TorneoDTO crearTorneo(TorneoDTO torneoDTO);

    List<TorneoDTO> listarTorneos();

    TorneoDTO buscarTorneoPorId(Long id);

    TorneoDTO actualizarTorneo(Long id, TorneoDTO torneoDTO);

    void cancelarTorneo(Long id);

    void cerrarTorneo(Long id);

    List<TorneoDTO> listarPorJuego(Long juegoId);

    List<TorneoDTO> listarPorEstado(String estado);

    List<TorneoDTO> listarPorFecha(LocalDate fechaInicio);
}
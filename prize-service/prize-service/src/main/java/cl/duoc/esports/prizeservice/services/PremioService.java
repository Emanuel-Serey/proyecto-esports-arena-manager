package cl.duoc.esports.prizeservice.services;

import cl.duoc.esports.prizeservice.dto.PremioDTO;

import java.util.List;

public interface PremioService {

    PremioDTO asignarPremio(PremioDTO premioDTO);

    List<PremioDTO> listarPremios();

    PremioDTO buscarPremioPorId(Long id);

    List<PremioDTO> listarPremiosPorTorneo(Long torneoId);

    List<PremioDTO> listarPremiosPorParticipante(Long participanteId);

    List<PremioDTO> listarPremiosPorEstado(String estadoEntrega);

    PremioDTO marcarComoEntregado(Long id);

    void anularPremio(Long id);
}
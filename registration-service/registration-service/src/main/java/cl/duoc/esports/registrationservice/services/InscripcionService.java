package cl.duoc.esports.registrationservice.services;

import cl.duoc.esports.registrationservice.dto.InscripcionDTO;

import java.util.List;

public interface InscripcionService {

    InscripcionDTO crearInscripcion(InscripcionDTO inscripcionDTO);

    List<InscripcionDTO> listarInscripciones();

    InscripcionDTO buscarInscripcionPorId(Long id);

    InscripcionDTO actualizarEstado(Long id, String estado);

    void cancelarInscripcion(Long id);

    List<InscripcionDTO> listarPorTorneo(Long torneoId);

    List<InscripcionDTO> listarPorEquipo(Long equipoId);

    List<InscripcionDTO> listarPorJugador(Long jugadorId);
}
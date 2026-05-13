package cl.duoc.esports.gameservice.services;

import cl.duoc.esports.gameservice.dto.JuegoDTO;

import java.util.List;

public interface JuegoService {

    JuegoDTO crearJuego(JuegoDTO juegoDTO);

    List<JuegoDTO> listarJuegos();

    List<JuegoDTO> listarJuegosActivos();

    JuegoDTO buscarJuegoPorId(Long id);

    JuegoDTO actualizarJuego(Long id, JuegoDTO juegoDTO);

    void desactivarJuego(Long id);
}
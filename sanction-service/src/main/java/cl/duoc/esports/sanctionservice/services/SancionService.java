package cl.duoc.esports.sanctionservice.services;

import cl.duoc.esports.sanctionservice.dto.SancionDTO;

import java.util.List;

public interface SancionService {

    SancionDTO crearSancion(SancionDTO sancionDTO);

    List<SancionDTO> listarSanciones();

    SancionDTO buscarSancionPorId(Long id);

    SancionDTO actualizarSancion(Long id, SancionDTO sancionDTO);

    void cerrarSancion(Long id);

    List<SancionDTO> listarPorUsuario(Long usuarioId);

    List<SancionDTO> listarPorEquipo(Long equipoId);

    List<SancionDTO> listarPorEstado(String estado);

    boolean existeSancionActivaUsuario(Long usuarioId);

    boolean existeSancionActivaEquipo(Long equipoId);
}
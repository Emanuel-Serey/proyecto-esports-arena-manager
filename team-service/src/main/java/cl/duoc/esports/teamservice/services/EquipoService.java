package cl.duoc.esports.teamservice.services;

import cl.duoc.esports.teamservice.dto.EquipoDTO;
import cl.duoc.esports.teamservice.dto.MiembroEquipoDTO;

import java.util.List;

public interface EquipoService {

    EquipoDTO crearEquipo(EquipoDTO equipoDTO);

    List<EquipoDTO> listarEquipos();

    EquipoDTO buscarEquipoPorId(Long id);

    EquipoDTO actualizarEquipo(Long id, EquipoDTO equipoDTO);

    void desactivarEquipo(Long id);

    List<EquipoDTO> listarPorEstado(String estado);

    List<EquipoDTO> listarPorJuegoPrincipal(Long juegoPrincipalId);

    List<EquipoDTO> listarPorCapitan(Long capitanId);

    MiembroEquipoDTO agregarMiembro(Long equipoId, MiembroEquipoDTO miembroDTO);

    List<MiembroEquipoDTO> listarMiembros(Long equipoId);

    void eliminarMiembro(Long equipoId, Long miembroId);
}
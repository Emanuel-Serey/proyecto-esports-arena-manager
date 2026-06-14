package cl.duoc.esports.teamservice.services;

import cl.duoc.esports.teamservice.repositories.MiembroEquipoRepository;
import cl.duoc.esports.teamservice.clients.UsuarioClient;
import cl.duoc.esports.teamservice.dto.UsuarioDTO;
import cl.duoc.esports.teamservice.clients.JuegoClient;
import cl.duoc.esports.teamservice.dto.EquipoDTO;
import cl.duoc.esports.teamservice.dto.JuegoDTO;
import cl.duoc.esports.teamservice.dto.MiembroEquipoDTO;
import cl.duoc.esports.teamservice.exceptions.EquipoException;
import cl.duoc.esports.teamservice.models.Equipo;
import cl.duoc.esports.teamservice.models.MiembroEquipo;
import cl.duoc.esports.teamservice.repositories.EquipoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EquipoServiceImpl implements EquipoService {

    private static final Logger logger = LoggerFactory.getLogger(EquipoServiceImpl.class);

    @Autowired
    private MiembroEquipoRepository miembroEquipoRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private JuegoClient juegoClient;

    @Autowired
    private UsuarioClient usuarioClient;

    @Transactional
    @Override
    public EquipoDTO crearEquipo(EquipoDTO equipoDTO) {

        logger.info("Creando equipo nombre={} para juegoPrincipalId={}",
                equipoDTO.getNombre(), equipoDTO.getJuegoPrincipalId());

        if (equipoRepository.existsByNombre(equipoDTO.getNombre())) {
            logger.warn("Intento de crear equipo duplicado nombre={}", equipoDTO.getNombre());
            throw new EquipoException("Ya existe un equipo con ese nombre", HttpStatus.CONFLICT);
        }

        validarJuegoActivo(equipoDTO.getJuegoPrincipalId());

        if (equipoDTO.getMiembros() == null || equipoDTO.getMiembros().isEmpty()) {
            logger.warn("Intento de crear equipo sin miembros nombre={}", equipoDTO.getNombre());
            throw new EquipoException("El equipo debe tener al menos un miembro", HttpStatus.UNPROCESSABLE_CONTENT);
        }

        validarUsuarioActivo(equipoDTO.getCapitanId());
        validarUsuariosMiembrosActivos(equipoDTO.getMiembros());

        validarCapitanIncluido(equipoDTO.getCapitanId(), equipoDTO.getMiembros());
        validarMiembrosDuplicados(equipoDTO.getMiembros());
        validarUsuariosNoPertenecenAOtroEquipo(equipoDTO.getMiembros());

        Equipo equipo = new Equipo();

        equipo.setNombre(equipoDTO.getNombre());
        equipo.setCapitanId(equipoDTO.getCapitanId());
        equipo.setJuegoPrincipalId(equipoDTO.getJuegoPrincipalId());
        equipo.setEstado("ACTIVO");

        for (MiembroEquipoDTO miembroDTO : equipoDTO.getMiembros()) {
            MiembroEquipo miembro = new MiembroEquipo();

            miembro.setUsuarioId(miembroDTO.getUsuarioId());
            miembro.setRolDentroEquipo(miembroDTO.getRolDentroEquipo());
            miembro.setEquipo(equipo);

            equipo.getMiembros().add(miembro);
        }

        Equipo equipoGuardado = equipoRepository.save(equipo);

        logger.info("Equipo creado correctamente id={}", equipoGuardado.getId());

        return convertirADTO(equipoGuardado);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EquipoDTO> listarEquipos() {

        logger.info("Listando equipos");

        return equipoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public EquipoDTO buscarEquipoPorId(Long id) {

        logger.info("Buscando equipo id={}", id);

        Equipo equipo = obtenerEquipoPorId(id);

        return convertirADTO(equipo);
    }

    @Transactional
    @Override
    public EquipoDTO actualizarEquipo(Long id, EquipoDTO equipoDTO) {

        logger.info("Actualizando equipo id={}", id);

        Equipo equipo = obtenerEquipoPorId(id);

        if (!equipo.getNombre().equalsIgnoreCase(equipoDTO.getNombre())
                && equipoRepository.existsByNombre(equipoDTO.getNombre())) {

            logger.warn("Intento de actualizar equipo id={} con nombre duplicado={}",
                    id, equipoDTO.getNombre());

            throw new EquipoException("Ya existe un equipo con ese nombre", HttpStatus.CONFLICT);
        }


        validarJuegoActivo(equipoDTO.getJuegoPrincipalId());
        validarUsuarioActivo(equipoDTO.getCapitanId());

        if (!existeMiembroEnEquipo(equipo, equipoDTO.getCapitanId())) {
            logger.warn("Capitán id={} no está incluido como miembro del equipo id={}",
                    equipoDTO.getCapitanId(), id);

            throw new EquipoException("El capitán debe estar incluido como miembro del equipo", HttpStatus.UNPROCESSABLE_CONTENT);
        }

        equipo.setNombre(equipoDTO.getNombre());
        equipo.setCapitanId(equipoDTO.getCapitanId());
        equipo.setJuegoPrincipalId(equipoDTO.getJuegoPrincipalId());

        Equipo equipoActualizado = equipoRepository.save(equipo);

        logger.info("Equipo actualizado correctamente id={}", id);

        return convertirADTO(equipoActualizado);
    }

    @Transactional
    @Override
    public void desactivarEquipo(Long id) {

        logger.info("Desactivando equipo id={}", id);

        Equipo equipo = obtenerEquipoPorId(id);

        equipo.setEstado("INACTIVO");
        equipoRepository.save(equipo);

        logger.info("Equipo desactivado correctamente id={}", id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EquipoDTO> listarPorEstado(String estado) {

        logger.info("Listando equipos por estado={}", estado);

        return equipoRepository.findByEstado(estado.toUpperCase())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<EquipoDTO> listarPorJuegoPrincipal(Long juegoPrincipalId) {

        logger.info("Listando equipos por juegoPrincipalId={}", juegoPrincipalId);

        return equipoRepository.findByJuegoPrincipalId(juegoPrincipalId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<EquipoDTO> listarPorCapitan(Long capitanId) {

        logger.info("Listando equipos por capitanId={}", capitanId);

        return equipoRepository.findByCapitanId(capitanId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional
    @Override
    public MiembroEquipoDTO agregarMiembro(Long equipoId, MiembroEquipoDTO miembroDTO) {

        logger.info("Agregando miembro usuarioId={} al equipo id={}",
                miembroDTO.getUsuarioId(), equipoId);

        Equipo equipo = obtenerEquipoPorId(equipoId);

        validarUsuarioActivo(miembroDTO.getUsuarioId());

        if (existeMiembroEnEquipo(equipo, miembroDTO.getUsuarioId())) {
            logger.warn("Intento de agregar miembro duplicado usuarioId={} al equipo id={}",
                    miembroDTO.getUsuarioId(), equipoId);

            throw new EquipoException("El usuario ya pertenece al equipo", HttpStatus.CONFLICT);
        }

        if (miembroEquipoRepository.existsByUsuarioIdAndEquipo_Estado(miembroDTO.getUsuarioId(), "ACTIVO")) {
            logger.warn("UsuarioId={} ya pertenece a otro equipo activo", miembroDTO.getUsuarioId());
            throw new EquipoException("El usuario ya pertenece a otro equipo activo", HttpStatus.CONFLICT);
        }

        MiembroEquipo miembro = new MiembroEquipo();

        miembro.setUsuarioId(miembroDTO.getUsuarioId());
        miembro.setRolDentroEquipo(miembroDTO.getRolDentroEquipo());
        miembro.setEquipo(equipo);

        equipo.getMiembros().add(miembro);

        Equipo equipoActualizado = equipoRepository.save(equipo);

        MiembroEquipo miembroGuardado = equipoActualizado.getMiembros()
                .stream()
                .filter(m -> m.getUsuarioId().equals(miembroDTO.getUsuarioId()))
                .findFirst()
                .orElseThrow(() -> new EquipoException("No se pudo agregar el miembro al equipo", HttpStatus.INTERNAL_SERVER_ERROR));

        logger.info("Miembro usuarioId={} agregado correctamente al equipo id={}",
                miembroDTO.getUsuarioId(), equipoId);

        return convertirMiembroADTO(miembroGuardado);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MiembroEquipoDTO> listarMiembros(Long equipoId) {

        logger.info("Listando miembros del equipo id={}", equipoId);

        Equipo equipo = obtenerEquipoPorId(equipoId);

        return equipo.getMiembros()
                .stream()
                .map(this::convertirMiembroADTO)
                .toList();
    }

    @Transactional
    @Override
    public void eliminarMiembro(Long equipoId, Long miembroId) {

        logger.info("Eliminando miembro id={} del equipo id={}", miembroId, equipoId);

        Equipo equipo = obtenerEquipoPorId(equipoId);

        MiembroEquipo miembro = equipo.getMiembros()
                .stream()
                .filter(m -> m.getId().equals(miembroId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Miembro id={} no encontrado en equipo id={}", miembroId, equipoId);
                    return new EquipoException("Miembro no encontrado en el equipo", HttpStatus.NOT_FOUND);
                });

        if (miembro.getUsuarioId().equals(equipo.getCapitanId())) {
            logger.warn("Intento de eliminar capitán usuarioId={} del equipo id={}",
                    miembro.getUsuarioId(), equipoId);

            throw new EquipoException("No se puede eliminar al capitán del equipo", HttpStatus.UNPROCESSABLE_CONTENT);
        }

        equipo.getMiembros().remove(miembro);
        equipoRepository.save(equipo);

        logger.info("Miembro id={} eliminado correctamente del equipo id={}", miembroId, equipoId);
    }

    private Equipo obtenerEquipoPorId(Long id) {
        return equipoRepository.findById(id).orElseThrow(() -> {
            logger.warn("Equipo no encontrado id={}", id);
            return new EquipoException("Equipo no encontrado", HttpStatus.NOT_FOUND);
        });
    }

    private void validarJuegoActivo(Long juegoId) {
        try {
            logger.info("Validando juegoPrincipalId={} desde game-service", juegoId);

            JuegoDTO juegoDTO = juegoClient.buscarJuegoPorId(juegoId);

            if (juegoDTO.getEstado() == null || !juegoDTO.getEstado()) {
                logger.warn("JuegoPrincipalId={} no está activo", juegoId);
                throw new EquipoException("El juego principal no está activo", HttpStatus.CONFLICT);
            }

        } catch (EquipoException ex) {
            throw ex;

        } catch (FeignException.NotFound ex) {
            logger.warn("JuegoPrincipalId={} no encontrado en game-service", juegoId);
            throw new EquipoException("El juego principal no existe", HttpStatus.NOT_FOUND);

        } catch (Exception ex) {
            logger.error("No se pudo validar juegoPrincipalId={} desde game-service", juegoId);
            throw new EquipoException("No se pudo validar el juego principal desde game-service", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private void validarCapitanIncluido(Long capitanId, List<MiembroEquipoDTO> miembros) {
        boolean existeCapitan = miembros.stream()
                .anyMatch(miembro -> miembro.getUsuarioId().equals(capitanId));

        if (!existeCapitan) {
            logger.warn("El capitán id={} no está incluido como miembro del equipo", capitanId);
            throw new EquipoException("El capitán debe estar incluido como miembro del equipo", HttpStatus.UNPROCESSABLE_CONTENT);
        }
    }

    private void validarMiembrosDuplicados(List<MiembroEquipoDTO> miembros) {
        Set<Long> usuarios = new HashSet<>();

        for (MiembroEquipoDTO miembro : miembros) {
            if (!usuarios.add(miembro.getUsuarioId())) {
                logger.warn("Usuario duplicado dentro del equipo usuarioId={}", miembro.getUsuarioId());
                throw new EquipoException("No se puede repetir un usuario dentro del mismo equipo", HttpStatus.CONFLICT);
            }
        }
    }

    private boolean existeMiembroEnEquipo(Equipo equipo, Long usuarioId) {
        return equipo.getMiembros()
                .stream()
                .anyMatch(miembro -> miembro.getUsuarioId().equals(usuarioId));
    }

    private void validarUsuarioActivo(Long usuarioId) {
        try {
            logger.info("Validando usuarioId={} desde user-service", usuarioId);

            UsuarioDTO usuarioDTO = usuarioClient.buscarUsuarioPorId(usuarioId);

            if (usuarioDTO.getEstado() == null || !usuarioDTO.getEstado().equalsIgnoreCase("ACTIVO")) {
                logger.warn("UsuarioId={} no está activo", usuarioId);
                throw new EquipoException("El usuario no está activo", HttpStatus.CONFLICT);
            }

        } catch (EquipoException ex) {
            throw ex;

        } catch (FeignException.NotFound ex) {
            logger.warn("UsuarioId={} no encontrado en user-service", usuarioId);
            throw new EquipoException("El usuario no existe", HttpStatus.NOT_FOUND);

        } catch (Exception ex) {
            logger.error("No se pudo validar usuarioId={} desde user-service", usuarioId);
            throw new EquipoException("No se pudo validar el usuario desde user-service", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private void validarUsuariosMiembrosActivos(List<MiembroEquipoDTO> miembros) {
        for (MiembroEquipoDTO miembro : miembros) {
            validarUsuarioActivo(miembro.getUsuarioId());
        }
    }

    private void validarUsuariosNoPertenecenAOtroEquipo(List<MiembroEquipoDTO> miembros) {
        for (MiembroEquipoDTO miembro : miembros) {
            if (miembroEquipoRepository.existsByUsuarioIdAndEquipo_Estado(miembro.getUsuarioId(), "ACTIVO")) {
                logger.warn("UsuarioId={} ya pertenece a otro equipo activo", miembro.getUsuarioId());
                throw new EquipoException("El usuario ya pertenece a otro equipo activo", HttpStatus.CONFLICT);
            }
        }
    }

    private EquipoDTO convertirADTO(Equipo equipo) {
        EquipoDTO equipoDTO = new EquipoDTO();

        equipoDTO.setId(equipo.getId());
        equipoDTO.setNombre(equipo.getNombre());
        equipoDTO.setCapitanId(equipo.getCapitanId());
        equipoDTO.setJuegoPrincipalId(equipo.getJuegoPrincipalId());
        equipoDTO.setEstado(equipo.getEstado());

        List<MiembroEquipoDTO> miembrosDTO = equipo.getMiembros()
                .stream()
                .map(this::convertirMiembroADTO)
                .toList();

        equipoDTO.setMiembros(miembrosDTO);

        return equipoDTO;
    }

    private MiembroEquipoDTO convertirMiembroADTO(MiembroEquipo miembro) {
        MiembroEquipoDTO miembroDTO = new MiembroEquipoDTO();

        miembroDTO.setId(miembro.getId());
        miembroDTO.setUsuarioId(miembro.getUsuarioId());
        miembroDTO.setRolDentroEquipo(miembro.getRolDentroEquipo());

        return miembroDTO;
    }
}
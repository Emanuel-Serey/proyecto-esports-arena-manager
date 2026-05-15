package cl.duoc.esports.sanctionservice.services;

import cl.duoc.esports.sanctionservice.clients.EquipoClient;
import cl.duoc.esports.sanctionservice.clients.UsuarioClient;
import cl.duoc.esports.sanctionservice.dto.EquipoDTO;
import cl.duoc.esports.sanctionservice.dto.UsuarioDTO;
import cl.duoc.esports.sanctionservice.dto.SancionDTO;
import cl.duoc.esports.sanctionservice.exceptions.SancionException;
import cl.duoc.esports.sanctionservice.models.Sancion;
import cl.duoc.esports.sanctionservice.repositories.SancionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SancionServiceImpl implements SancionService {

    private static final Logger logger = LoggerFactory.getLogger(SancionServiceImpl.class);

    @Autowired
    private SancionRepository sancionRepository;

    @Autowired
    private UsuarioClient usuarioClient;

    @Autowired
    private EquipoClient equipoClient;

    @Transactional
    @Override
    public SancionDTO crearSancion(SancionDTO sancionDTO) {

        logger.info("Creando sanción severidad={} usuarioId={} equipoId={}",
                sancionDTO.getSeveridad(), sancionDTO.getUsuarioId(), sancionDTO.getEquipoId());

        validarUsuarioOEquipo(sancionDTO);
        validarFechas(sancionDTO);
        validarUsuarioOEquipoExistente(sancionDTO);

        Sancion sancion = new Sancion();

        sancion.setUsuarioId(sancionDTO.getUsuarioId());
        sancion.setEquipoId(sancionDTO.getEquipoId());
        sancion.setMotivo(sancionDTO.getMotivo());
        sancion.setFechaInicio(sancionDTO.getFechaInicio());
        sancion.setFechaFin(sancionDTO.getFechaFin());
        sancion.setEstado("ACTIVA");
        sancion.setSeveridad(sancionDTO.getSeveridad().toUpperCase());

        Sancion sancionGuardada = sancionRepository.save(sancion);

        logger.info("Sanción creada correctamente id={} estado={}",
                sancionGuardada.getId(), sancionGuardada.getEstado());

        return convertirADTO(sancionGuardada);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SancionDTO> listarSanciones() {

        logger.info("Listando sanciones");

        return sancionRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public SancionDTO buscarSancionPorId(Long id) {

        logger.info("Buscando sanción id={}", id);

        Sancion sancion = obtenerSancionPorId(id);
        return convertirADTO(sancion);
    }

    @Transactional
    @Override
    public SancionDTO actualizarSancion(Long id, SancionDTO sancionDTO) {

        logger.info("Actualizando sanción id={}", id);

        Sancion sancion = obtenerSancionPorId(id);

        validarUsuarioOEquipo(sancionDTO);
        validarFechas(sancionDTO);
        validarUsuarioOEquipoExistente(sancionDTO);

        sancion.setUsuarioId(sancionDTO.getUsuarioId());
        sancion.setEquipoId(sancionDTO.getEquipoId());
        sancion.setMotivo(sancionDTO.getMotivo());
        sancion.setFechaInicio(sancionDTO.getFechaInicio());
        sancion.setFechaFin(sancionDTO.getFechaFin());
        sancion.setSeveridad(sancionDTO.getSeveridad().toUpperCase());

        if (sancionDTO.getEstado() != null && !sancionDTO.getEstado().isBlank()) {
            sancion.setEstado(sancionDTO.getEstado().toUpperCase());
        }

        Sancion sancionActualizada = sancionRepository.save(sancion);

        logger.info("Sanción actualizada correctamente id={} estado={}",
                id, sancionActualizada.getEstado());

        return convertirADTO(sancionActualizada);
    }

    @Transactional
    @Override
    public void cerrarSancion(Long id) {

        logger.info("Cerrando sanción id={}", id);

        Sancion sancion = obtenerSancionPorId(id);

        sancion.setEstado("CERRADA");
        sancionRepository.save(sancion);

        logger.info("Sanción cerrada correctamente id={}", id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SancionDTO> listarPorUsuario(Long usuarioId) {

        logger.info("Listando sanciones por usuarioId={}", usuarioId);

        return sancionRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<SancionDTO> listarPorEquipo(Long equipoId) {

        logger.info("Listando sanciones por equipoId={}", equipoId);

        return sancionRepository.findByEquipoId(equipoId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<SancionDTO> listarPorEstado(String estado) {

        logger.info("Listando sanciones por estado={}", estado);

        return sancionRepository.findByEstado(estado.toUpperCase())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existeSancionActivaUsuario(Long usuarioId) {

        logger.info("Validando si usuarioId={} tiene sanción activa", usuarioId);

        return sancionRepository.existsByUsuarioIdAndEstado(usuarioId, "ACTIVA");
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existeSancionActivaEquipo(Long equipoId) {

        logger.info("Validando si equipoId={} tiene sanción activa", equipoId);

        return sancionRepository.existsByEquipoIdAndEstado(equipoId, "ACTIVA");
    }

    private Sancion obtenerSancionPorId(Long id) {
        return sancionRepository.findById(id).orElseThrow(() -> {
            logger.warn("Sanción no encontrada id={}", id);
            return new SancionException("Sanción no encontrada");
        });
    }

    private void validarUsuarioOEquipo(SancionDTO sancionDTO) {
        if (sancionDTO.getUsuarioId() == null && sancionDTO.getEquipoId() == null) {
            logger.warn("Sanción inválida: no se indicó usuarioId ni equipoId");
            throw new SancionException("Debe indicar usuarioId o equipoId");
        }

        if (sancionDTO.getUsuarioId() != null && sancionDTO.getEquipoId() != null) {
            logger.warn("Sanción inválida: usuarioId={} y equipoId={} enviados al mismo tiempo",
                    sancionDTO.getUsuarioId(), sancionDTO.getEquipoId());

            throw new SancionException("Una sanción no puede tener usuarioId y equipoId al mismo tiempo");
        }
    }

    private void validarFechas(SancionDTO sancionDTO) {
        if (sancionDTO.getFechaFin().isBefore(sancionDTO.getFechaInicio())) {
            logger.warn("Fechas inválidas para sanción. fechaInicio={} fechaFin={}",
                    sancionDTO.getFechaInicio(), sancionDTO.getFechaFin());

            throw new SancionException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }

    private void validarUsuarioOEquipoExistente(SancionDTO sancionDTO) {

        if (sancionDTO.getUsuarioId() != null) {
            try {
                logger.info("Validando usuarioId={} desde user-service", sancionDTO.getUsuarioId());

                UsuarioDTO usuarioDTO = usuarioClient.buscarUsuarioPorId(sancionDTO.getUsuarioId());

                if (usuarioDTO.getEstado() == null || !usuarioDTO.getEstado().equalsIgnoreCase("ACTIVO")) {
                    logger.warn("UsuarioId={} no está activo", sancionDTO.getUsuarioId());
                    throw new SancionException("El usuario no está activo");
                }

            } catch (SancionException ex) {
                throw ex;
            } catch (Exception ex) {
                logger.error("No se pudo validar usuarioId={} desde user-service",
                        sancionDTO.getUsuarioId());

                throw new SancionException("El usuario no existe o no está disponible");
            }
        }

        if (sancionDTO.getEquipoId() != null) {
            try {
                logger.info("Validando equipoId={} desde team-service", sancionDTO.getEquipoId());

                EquipoDTO equipoDTO = equipoClient.buscarEquipoPorId(sancionDTO.getEquipoId());

                if (equipoDTO.getEstado() == null || !equipoDTO.getEstado().equalsIgnoreCase("ACTIVO")) {
                    logger.warn("EquipoId={} no está activo", sancionDTO.getEquipoId());
                    throw new SancionException("El equipo no está activo");
                }

            } catch (SancionException ex) {
                throw ex;
            } catch (Exception ex) {
                logger.error("No se pudo validar equipoId={} desde team-service",
                        sancionDTO.getEquipoId());

                throw new SancionException("El equipo no existe o no está disponible");
            }
        }
    }

    private SancionDTO convertirADTO(Sancion sancion) {
        SancionDTO sancionDTO = new SancionDTO();

        sancionDTO.setId(sancion.getId());
        sancionDTO.setUsuarioId(sancion.getUsuarioId());
        sancionDTO.setEquipoId(sancion.getEquipoId());
        sancionDTO.setMotivo(sancion.getMotivo());
        sancionDTO.setFechaInicio(sancion.getFechaInicio());
        sancionDTO.setFechaFin(sancion.getFechaFin());
        sancionDTO.setEstado(sancion.getEstado());
        sancionDTO.setSeveridad(sancion.getSeveridad());

        return sancionDTO;
    }
}
package cl.duoc.esports.registrationservice.services;

import cl.duoc.esports.registrationservice.clients.UsuarioClient;
import cl.duoc.esports.registrationservice.dto.UsuarioDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cl.duoc.esports.registrationservice.clients.SancionClient;
import cl.duoc.esports.registrationservice.clients.EquipoClient;
import cl.duoc.esports.registrationservice.clients.TorneoClient;
import cl.duoc.esports.registrationservice.dto.EquipoDTO;
import cl.duoc.esports.registrationservice.dto.TorneoDTO;
import cl.duoc.esports.registrationservice.dto.InscripcionDTO;
import cl.duoc.esports.registrationservice.exceptions.InscripcionException;
import cl.duoc.esports.registrationservice.models.Inscripcion;
import cl.duoc.esports.registrationservice.repositories.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InscripcionServiceImpl implements InscripcionService {

    private static final Logger logger = LoggerFactory.getLogger(InscripcionServiceImpl.class);

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private TorneoClient torneoClient;

    @Autowired
    private EquipoClient equipoClient;

    @Autowired
    private SancionClient sancionClient;

    @Autowired
    private UsuarioClient usuarioClient;

    @Transactional
    @Override
    public InscripcionDTO crearInscripcion(InscripcionDTO inscripcionDTO) {

        logger.info("Creando inscripción para torneoId={} tipoParticipante={}",
                inscripcionDTO.getTorneoId(), inscripcionDTO.getTipoParticipante());

        validarTipoParticipante(inscripcionDTO);

        TorneoDTO torneoDTO = validarTorneoDisponible(inscripcionDTO.getTorneoId());

        validarCuposDisponibles(torneoDTO);

        if (inscripcionDTO.getTipoParticipante().equalsIgnoreCase("EQUIPO")) {
            validarEquipoActivo(inscripcionDTO.getEquipoId());
        }

        validarSancionActiva(inscripcionDTO);

        if (inscripcionDTO.getTipoParticipante().equalsIgnoreCase("EQUIPO")) {
            if (inscripcionRepository.existsByTorneoIdAndEquipoId(
                    inscripcionDTO.getTorneoId(),
                    inscripcionDTO.getEquipoId())) {

                logger.warn("Intento de inscripción duplicada para equipoId={} en torneoId={}",
                        inscripcionDTO.getEquipoId(), inscripcionDTO.getTorneoId());

                throw new InscripcionException("El equipo ya está inscrito en este torneo");
            }
        }

        if (inscripcionDTO.getTipoParticipante().equalsIgnoreCase("JUGADOR")) {

            validarJugadorActivo(inscripcionDTO.getJugadorId());

            if (inscripcionRepository.existsByTorneoIdAndJugadorId(
                    inscripcionDTO.getTorneoId(),
                    inscripcionDTO.getJugadorId())) {

                logger.warn("Intento de inscripción duplicada para jugadorId={} en torneoId={}",
                        inscripcionDTO.getJugadorId(), inscripcionDTO.getTorneoId());

                throw new InscripcionException("El jugador ya está inscrito en este torneo");
            }
        }

        Inscripcion inscripcion = new Inscripcion();

        inscripcion.setTorneoId(inscripcionDTO.getTorneoId());
        inscripcion.setEquipoId(inscripcionDTO.getEquipoId());
        inscripcion.setJugadorId(inscripcionDTO.getJugadorId());
        inscripcion.setTipoParticipante(inscripcionDTO.getTipoParticipante().toUpperCase());
        inscripcion.setEstado("PENDIENTE");
        inscripcion.setFechaInscripcion(LocalDate.now());

        Inscripcion inscripcionGuardada = inscripcionRepository.save(inscripcion);

        logger.info("Inscripción creada correctamente id={} torneoId={}",
                inscripcionGuardada.getId(), inscripcionGuardada.getTorneoId());

        return convertirADTO(inscripcionGuardada);
    }

    @Transactional(readOnly = true)
    @Override
    public List<InscripcionDTO> listarInscripciones() {

        logger.info("Listando inscripciones");

        return inscripcionRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public InscripcionDTO buscarInscripcionPorId(Long id) {

        logger.info("Buscando inscripción id={}", id);

        Inscripcion inscripcion = obtenerInscripcionPorId(id);
        return convertirADTO(inscripcion);
    }

    @Transactional
    @Override
    public InscripcionDTO actualizarEstado(Long id, String estado) {

        logger.info("Actualizando estado de inscripción id={} a estado={}", id, estado);

        Inscripcion inscripcion = obtenerInscripcionPorId(id);

        inscripcion.setEstado(estado.toUpperCase());

        Inscripcion inscripcionActualizada = inscripcionRepository.save(inscripcion);

        logger.info("Estado de inscripción actualizado correctamente id={} estado={}",
                id, estado.toUpperCase());

        return convertirADTO(inscripcionActualizada);
    }

    @Transactional
    @Override
    public void cancelarInscripcion(Long id) {

        logger.info("Cancelando inscripción id={}", id);

        Inscripcion inscripcion = obtenerInscripcionPorId(id);

        inscripcion.setEstado("CANCELADA");
        inscripcionRepository.save(inscripcion);

        logger.info("Inscripción cancelada correctamente id={}", id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<InscripcionDTO> listarPorTorneo(Long torneoId) {

        logger.info("Listando inscripciones por torneoId={}", torneoId);

        return inscripcionRepository.findByTorneoId(torneoId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<InscripcionDTO> listarPorEquipo(Long equipoId) {

        logger.info("Listando inscripciones por equipoId={}", equipoId);

        return inscripcionRepository.findByEquipoId(equipoId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<InscripcionDTO> listarPorJugador(Long jugadorId) {

        logger.info("Listando inscripciones por jugadorId={}", jugadorId);

        return inscripcionRepository.findByJugadorId(jugadorId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    private Inscripcion obtenerInscripcionPorId(Long id) {
        return inscripcionRepository.findById(id).orElseThrow(() -> {
            logger.warn("Inscripción no encontrada id={}", id);
            return new InscripcionException("Inscripción no encontrada");
        });
    }

    private void validarTipoParticipante(InscripcionDTO inscripcionDTO) {

        if (inscripcionDTO.getTipoParticipante().equalsIgnoreCase("EQUIPO")) {
            if (inscripcionDTO.getEquipoId() == null) {
                logger.warn("Inscripción inválida: tipo EQUIPO sin equipoId");
                throw new InscripcionException("Si el tipo de participante es EQUIPO, debe indicar equipoId");
            }

            if (inscripcionDTO.getJugadorId() != null) {
                logger.warn("Inscripción inválida: tipo EQUIPO incluye jugadorId={}",
                        inscripcionDTO.getJugadorId());

                throw new InscripcionException("Una inscripción de tipo EQUIPO no debe incluir jugadorId");
            }

            return;
        }

        if (inscripcionDTO.getTipoParticipante().equalsIgnoreCase("JUGADOR")) {
            if (inscripcionDTO.getJugadorId() == null) {
                logger.warn("Inscripción inválida: tipo JUGADOR sin jugadorId");
                throw new InscripcionException("Si el tipo de participante es JUGADOR, debe indicar jugadorId");
            }

            if (inscripcionDTO.getEquipoId() != null) {
                logger.warn("Inscripción inválida: tipo JUGADOR incluye equipoId={}",
                        inscripcionDTO.getEquipoId());

                throw new InscripcionException("Una inscripción de tipo JUGADOR no debe incluir equipoId");
            }

            return;
        }

        logger.warn("Tipo de participante inválido tipo={}", inscripcionDTO.getTipoParticipante());
        throw new InscripcionException("El tipo de participante debe ser EQUIPO o JUGADOR");
    }

    private TorneoDTO validarTorneoDisponible(Long torneoId) {
        try {
            logger.info("Validando torneoId={} desde tournament-service", torneoId);

            TorneoDTO torneoDTO = torneoClient.buscarTorneoPorId(torneoId);

            if (torneoDTO.getEstado().equalsIgnoreCase("CANCELADO")) {
                logger.warn("No se puede inscribir en torneo cancelado torneoId={}", torneoId);
                throw new InscripcionException("No se puede inscribir en un torneo cancelado");
            }

            if (torneoDTO.getEstado().equalsIgnoreCase("CERRADO")) {
                logger.warn("No se puede inscribir en torneo cerrado torneoId={}", torneoId);
                throw new InscripcionException("No se puede inscribir en un torneo cerrado");
            }

            if (LocalDate.now().isAfter(torneoDTO.getFechaInicio())) {
                logger.warn("No se puede inscribir después del inicio del torneo torneoId={}", torneoId);
                throw new InscripcionException("No se puede inscribir después del inicio del torneo");
            }

            return torneoDTO;

        } catch (InscripcionException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("No se pudo validar torneoId={} desde tournament-service", torneoId);
            throw new InscripcionException("El torneo no existe o no está disponible");
        }
    }

    private void validarEquipoActivo(Long equipoId) {
        try {
            logger.info("Validando equipoId={} desde team-service", equipoId);

            EquipoDTO equipoDTO = equipoClient.buscarEquipoPorId(equipoId);

            if (equipoDTO.getEstado() == null || !equipoDTO.getEstado().equalsIgnoreCase("ACTIVO")) {
                logger.warn("EquipoId={} no está activo", equipoId);
                throw new InscripcionException("El equipo no está activo");
            }

        } catch (InscripcionException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("No se pudo validar equipoId={} desde team-service", equipoId);
            throw new InscripcionException("El equipo no existe o no está disponible");
        }
    }

    private void validarCuposDisponibles(TorneoDTO torneoDTO) {

        logger.info("Validando cupos disponibles para torneoId={}", torneoDTO.getId());

        long inscritosActuales = inscripcionRepository.findByTorneoId(torneoDTO.getId())
                .stream()
                .filter(inscripcion -> !inscripcion.getEstado().equalsIgnoreCase("CANCELADA"))
                .count();

        if (inscritosActuales >= torneoDTO.getCupoMaximo()) {
            logger.warn("No existen cupos disponibles para torneoId={}. inscritosActuales={} cupoMaximo={}",
                    torneoDTO.getId(), inscritosActuales, torneoDTO.getCupoMaximo());

            throw new InscripcionException("No existen cupos disponibles para este torneo");
        }
    }

    private void validarJugadorActivo(Long jugadorId) {
        try {
            logger.info("Validando jugadorId={} desde user-service", jugadorId);

            UsuarioDTO usuarioDTO = usuarioClient.buscarUsuarioPorId(jugadorId);

            if (usuarioDTO.getEstado() == null || !usuarioDTO.getEstado().equalsIgnoreCase("ACTIVO")) {
                logger.warn("JugadorId={} no está activo", jugadorId);
                throw new InscripcionException("El jugador no está activo");
            }

        } catch (InscripcionException ex) {
            throw ex;

        } catch (Exception ex) {
            logger.error("No se pudo validar jugadorId={} desde user-service", jugadorId);
            throw new InscripcionException("El jugador no existe o no está disponible");
        }
    }

    private void validarSancionActiva(InscripcionDTO inscripcionDTO) {

        try {
            if (inscripcionDTO.getTipoParticipante().equalsIgnoreCase("EQUIPO")) {
                logger.info("Validando sanción activa para equipoId={} desde sanction-service",
                        inscripcionDTO.getEquipoId());

                Boolean tieneSancionActiva = sancionClient.existeSancionActivaEquipo(inscripcionDTO.getEquipoId());

                if (Boolean.TRUE.equals(tieneSancionActiva)) {
                    logger.warn("EquipoId={} tiene sanción activa", inscripcionDTO.getEquipoId());
                    throw new InscripcionException("No se puede inscribir un equipo con sanción activa");
                }
            }

            if (inscripcionDTO.getTipoParticipante().equalsIgnoreCase("JUGADOR")) {
                logger.info("Validando sanción activa para jugadorId={} desde sanction-service",
                        inscripcionDTO.getJugadorId());

                Boolean tieneSancionActiva = sancionClient.existeSancionActivaUsuario(inscripcionDTO.getJugadorId());

                if (Boolean.TRUE.equals(tieneSancionActiva)) {
                    logger.warn("JugadorId={} tiene sanción activa", inscripcionDTO.getJugadorId());
                    throw new InscripcionException("No se puede inscribir un jugador con sanción activa");
                }
            }

        } catch (InscripcionException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("No se pudo validar sanción activa para torneoId={}",
                    inscripcionDTO.getTorneoId());

            throw new InscripcionException("No se pudo validar si el participante tiene sanciones activas");
        }
    }


    private InscripcionDTO convertirADTO(Inscripcion inscripcion) {
        InscripcionDTO inscripcionDTO = new InscripcionDTO();

        inscripcionDTO.setId(inscripcion.getId());
        inscripcionDTO.setTorneoId(inscripcion.getTorneoId());
        inscripcionDTO.setEquipoId(inscripcion.getEquipoId());
        inscripcionDTO.setJugadorId(inscripcion.getJugadorId());
        inscripcionDTO.setTipoParticipante(inscripcion.getTipoParticipante());
        inscripcionDTO.setEstado(inscripcion.getEstado());
        inscripcionDTO.setFechaInscripcion(inscripcion.getFechaInscripcion());

        return inscripcionDTO;
    }
}
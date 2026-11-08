package cl.duoc.esports.registrationservice.services;

import cl.duoc.esports.registrationservice.clients.UsuarioClient;
import cl.duoc.esports.registrationservice.dto.UsuarioDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import feign.FeignException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import cl.duoc.esports.registrationservice.clients.JuegoClient;
import cl.duoc.esports.registrationservice.dto.JuegoDTO;
import java.time.LocalDate;
import java.util.List;

@Service
public class InscripcionServiceImpl implements InscripcionService {

    private static final Logger logger = LoggerFactory.getLogger(InscripcionServiceImpl.class);

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private JuegoClient juegoClient;

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
    public InscripcionDTO crearInscripcion(
            InscripcionDTO inscripcionDTO
    ) {

        logger.info(
                "Creando inscripción para torneoId={} tipoParticipante={}",
                inscripcionDTO.getTorneoId(),
                inscripcionDTO.getTipoParticipante()
        );

        validarTipoParticipante(inscripcionDTO);

        validarPermisoCrearInscripcion(inscripcionDTO);

        TorneoDTO torneoDTO = validarTorneoDisponible(inscripcionDTO.getTorneoId());

        validarModalidadTorneo(inscripcionDTO, torneoDTO);

        validarCuposDisponibles(torneoDTO);

        if (inscripcionDTO.getTipoParticipante().equalsIgnoreCase("EQUIPO")) {

            validarEquipoParaTorneo(inscripcionDTO.getEquipoId(), torneoDTO);
        }

        validarSancionActiva(inscripcionDTO);


        if (inscripcionDTO.getTipoParticipante().equalsIgnoreCase("EQUIPO")) {
            if (inscripcionRepository.existsByTorneoIdAndEquipoId(
                    inscripcionDTO.getTorneoId(),
                    inscripcionDTO.getEquipoId())) {

                logger.warn("Intento de inscripción duplicada para equipoId={} en torneoId={}",
                        inscripcionDTO.getEquipoId(), inscripcionDTO.getTorneoId());

                throw new InscripcionException("El equipo ya está inscrito en este torneo", HttpStatus.CONFLICT);
            }
        }

        if (inscripcionDTO.getTipoParticipante().equalsIgnoreCase("JUGADOR")) {

            validarJugadorActivo(inscripcionDTO.getJugadorId());

            if (inscripcionRepository.existsByTorneoIdAndJugadorId(
                    inscripcionDTO.getTorneoId(),
                    inscripcionDTO.getJugadorId())) {

                logger.warn("Intento de inscripción duplicada para jugadorId={} en torneoId={}",
                        inscripcionDTO.getJugadorId(), inscripcionDTO.getTorneoId());

                throw new InscripcionException("El jugador ya está inscrito en este torneo", HttpStatus.CONFLICT);
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

        Inscripcion inscripcion =
                obtenerInscripcionPorId(id);

        validarPermisoSobreInscripcion(inscripcion);

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

        Inscripcion inscripcion =
                obtenerInscripcionPorId(id);

        validarPermisoSobreInscripcion(inscripcion);

        inscripcion.setEstado("CANCELADA");

        inscripcionRepository.save(inscripcion);

        logger.info(
                "Inscripción cancelada correctamente id={}",
                id
        );
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

        logger.info(
                "Listando inscripciones por equipoId={}",
                equipoId
        );

        validarPermisoConsultarEquipo(equipoId);

        return inscripcionRepository
                .findByEquipoId(equipoId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<InscripcionDTO> listarPorJugador(Long jugadorId) {

        logger.info(
                "Listando inscripciones por jugadorId={}",
                jugadorId
        );

        validarPermisoConsultarJugador(jugadorId);

        return inscripcionRepository
                .findByJugadorId(jugadorId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    private Inscripcion obtenerInscripcionPorId(Long id) {
        return inscripcionRepository.findById(id).orElseThrow(() -> {
            logger.warn("Inscripción no encontrada id={}", id);
            return new InscripcionException("Inscripción no encontrada", HttpStatus.NOT_FOUND);
        });
    }

    private void validarTipoParticipante(InscripcionDTO inscripcionDTO) {

        if (inscripcionDTO.getTipoParticipante().equalsIgnoreCase("EQUIPO")) {
            if (inscripcionDTO.getEquipoId() == null) {
                logger.warn("Inscripción inválida: tipo EQUIPO sin equipoId");
                throw new InscripcionException("Si el tipo de participante es EQUIPO, debe indicar equipoId", HttpStatus.BAD_REQUEST);
            }

            if (inscripcionDTO.getJugadorId() != null) {
                logger.warn("Inscripción inválida: tipo EQUIPO incluye jugadorId={}",
                        inscripcionDTO.getJugadorId());

                throw new InscripcionException("Una inscripción de tipo EQUIPO no debe incluir jugadorId", HttpStatus.BAD_REQUEST);
            }

            return;
        }

        if (inscripcionDTO.getTipoParticipante().equalsIgnoreCase("JUGADOR")) {
            if (inscripcionDTO.getJugadorId() == null) {
                logger.warn("Inscripción inválida: tipo JUGADOR sin jugadorId");
                throw new InscripcionException("Si el tipo de participante es JUGADOR, debe indicar jugadorId", HttpStatus.BAD_REQUEST);
            }

            if (inscripcionDTO.getEquipoId() != null) {
                logger.warn("Inscripción inválida: tipo JUGADOR incluye equipoId={}",
                        inscripcionDTO.getEquipoId());

                throw new InscripcionException("Una inscripción de tipo JUGADOR no debe incluir equipoId", HttpStatus.BAD_REQUEST);
            }

            return;
        }

        logger.warn("Tipo de participante inválido tipo={}", inscripcionDTO.getTipoParticipante());
        throw new InscripcionException("El tipo de participante debe ser EQUIPO o JUGADOR", HttpStatus.BAD_REQUEST);
    }

    private TorneoDTO validarTorneoDisponible(Long torneoId) {
        try {
            logger.info("Validando torneoId={} desde tournament-service", torneoId);

            TorneoDTO torneoDTO = torneoClient.buscarTorneoPorId(torneoId);

            if (torneoDTO.getEstado().equalsIgnoreCase("CANCELADO")) {
                throw new InscripcionException("No se puede inscribir en un torneo cancelado", HttpStatus.CONFLICT);
            }

            if (torneoDTO.getEstado().equalsIgnoreCase("CERRADO")) {
                throw new InscripcionException("No se puede inscribir en un torneo cerrado", HttpStatus.CONFLICT);
            }

            if (torneoDTO.getCierreInscripcion() != null && LocalDate.now().isAfter(torneoDTO.getCierreInscripcion())) {
                throw new InscripcionException("El plazo de inscripción para este torneo ya finalizó", HttpStatus.CONFLICT);
            }

            if (LocalDate.now().isAfter(torneoDTO.getFechaInicio())) {
                throw new InscripcionException("No se puede inscribir después del inicio del torneo", HttpStatus.CONFLICT);
            }

            return torneoDTO;

        } catch (InscripcionException ex) {
            throw ex;

        } catch (FeignException.NotFound ex) {
            throw new InscripcionException("El torneo no existe", HttpStatus.NOT_FOUND);

        } catch (Exception ex) {
            logger.error("No se pudo validar torneoId={} desde tournament-service", torneoId);
            throw new InscripcionException("No se pudo validar el torneo desde tournament-service", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private void validarEquipoParaTorneo(
            Long equipoId,
            TorneoDTO torneoDTO
    ) {

        try {

            logger.info(
                    "Validando equipoId={} para torneoId={}",
                    equipoId,
                    torneoDTO.getId()
            );

            EquipoDTO equipo =
                    equipoClient.buscarEquipoPorId(equipoId);

            if (equipo.getEstado() == null ||
                    !equipo.getEstado()
                            .equalsIgnoreCase("ACTIVO")) {

                throw new InscripcionException(
                        "El equipo no está activo",
                        HttpStatus.CONFLICT
                );
            }

            if (!torneoDTO.getJuegoId()
                    .equals(equipo.getJuegoPrincipalId())) {

                throw new InscripcionException(
                        "El equipo no pertenece al juego del torneo",
                        HttpStatus.UNPROCESSABLE_CONTENT
                );
            }

            JuegoDTO juego =
                    juegoClient.buscarJuegoPorId(
                            torneoDTO.getJuegoId()
                    );

            int cantidadMiembros =
                    equipo.getMiembros() == null
                            ? 0
                            : equipo.getMiembros().size();

            if (juego.getJugadoresPorEquipo() == null) {

                throw new InscripcionException(
                        "No se pudo determinar la cantidad de jugadores requerida",
                        HttpStatus.SERVICE_UNAVAILABLE
                );
            }

            if (cantidadMiembros <
                    juego.getJugadoresPorEquipo()) {

                throw new InscripcionException(
                        "El equipo no tiene la cantidad mínima de integrantes requerida. " +
                                "Requeridos: " +
                                juego.getJugadoresPorEquipo() +
                                ", actuales: " +
                                cantidadMiembros,
                        HttpStatus.UNPROCESSABLE_CONTENT
                );
            }

        } catch (InscripcionException ex) {
            throw ex;

        } catch (FeignException.NotFound ex) {

            throw new InscripcionException(
                    "El equipo o juego asociado no existe",
                    HttpStatus.NOT_FOUND
            );

        } catch (Exception ex) {

            logger.error(
                    "No se pudo validar equipoId={} para torneoId={}",
                    equipoId,
                    torneoDTO.getId(),
                    ex
            );

            throw new InscripcionException(
                    "No se pudo validar el equipo para el torneo",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
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

            throw new InscripcionException("No existen cupos disponibles para este torneo", HttpStatus.CONFLICT);
        }
    }

    private void validarJugadorActivo(Long jugadorId) {
        try {
            logger.info("Validando jugadorId={} desde user-service", jugadorId);

            UsuarioDTO usuarioDTO = usuarioClient.buscarUsuarioPorId(jugadorId);

            if (usuarioDTO.getEstado() == null || !usuarioDTO.getEstado().equalsIgnoreCase("ACTIVO")) {
                logger.warn("JugadorId={} no está activo", jugadorId);

                throw new InscripcionException("El jugador no está activo", HttpStatus.CONFLICT);
            }

        } catch (InscripcionException ex) {
            throw ex;

        } catch (FeignException.NotFound ex) {
            logger.warn("JugadorId={} no encontrado en user-service", jugadorId);

            throw new InscripcionException("El jugador no existe", HttpStatus.NOT_FOUND);

        } catch (Exception ex) {
            logger.error("No se pudo validar jugadorId={} desde user-service", jugadorId);

            throw new InscripcionException("No se pudo validar el jugador desde user-service", HttpStatus.SERVICE_UNAVAILABLE);
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
                    throw new InscripcionException("No se puede inscribir un equipo con sanción activa", HttpStatus.CONFLICT);
                }
            }

            if (inscripcionDTO.getTipoParticipante().equalsIgnoreCase("JUGADOR")) {
                logger.info("Validando sanción activa para jugadorId={} desde sanction-service",
                        inscripcionDTO.getJugadorId());

                Boolean tieneSancionActiva = sancionClient.existeSancionActivaUsuario(inscripcionDTO.getJugadorId());

                if (Boolean.TRUE.equals(tieneSancionActiva)) {
                    logger.warn("JugadorId={} tiene sanción activa", inscripcionDTO.getJugadorId());
                    throw new InscripcionException("No se puede inscribir un jugador con sanción activa", HttpStatus.CONFLICT);
                }
            }

        } catch (InscripcionException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("No se pudo validar sanción activa para torneoId={}",
                    inscripcionDTO.getTorneoId());

            throw new InscripcionException("No se pudo validar si el participante tiene sanciones activas", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private void validarPermisoCrearInscripcion(
            InscripcionDTO inscripcionDTO
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new InscripcionException(
                    "Usuario no autenticado",
                    HttpStatus.FORBIDDEN
            );
        }

        boolean esAdminOOrganizador =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN") ||
                                        authority.getAuthority().equals("ROLE_ORGANIZADOR")
                        );

        if (esAdminOOrganizador) {
            return;
        }

        try {

            UsuarioDTO usuarioAutenticado =
                    usuarioClient.buscarUsuarioPorEmail(
                            authentication.getName()
                    );

            if (inscripcionDTO.getTipoParticipante()
                    .equalsIgnoreCase("JUGADOR")) {

                if (!usuarioAutenticado.getId()
                        .equals(inscripcionDTO.getJugadorId())) {

                    throw new InscripcionException(
                            "Un jugador solo puede inscribirse a sí mismo",
                            HttpStatus.FORBIDDEN
                    );
                }

                return;
            }

            if (inscripcionDTO.getTipoParticipante()
                    .equalsIgnoreCase("EQUIPO")) {

                EquipoDTO equipo =
                        equipoClient.buscarEquipoPorId(
                                inscripcionDTO.getEquipoId()
                        );

                if (!usuarioAutenticado.getId()
                        .equals(equipo.getCapitanId())) {

                    throw new InscripcionException(
                            "Solo el capitán puede inscribir al equipo",
                            HttpStatus.FORBIDDEN
                    );
                }
            }

        } catch (InscripcionException ex) {
            throw ex;

        } catch (Exception ex) {

            logger.error(
                    "No se pudo validar permisos de inscripción para email={}",
                    authentication.getName()
            );

            throw new InscripcionException(
                    "No se pudo validar el usuario autenticado",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

    private void validarPermisoSobreInscripcion(
            Inscripcion inscripcion
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new InscripcionException(
                    "Usuario no autenticado",
                    HttpStatus.FORBIDDEN
            );
        }

        boolean esAdminOOrganizador =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN") ||
                                        authority.getAuthority().equals("ROLE_ORGANIZADOR")
                        );

        if (esAdminOOrganizador) {
            return;
        }

        try {

            UsuarioDTO usuario =
                    usuarioClient.buscarUsuarioPorEmail(
                            authentication.getName()
                    );

            if ("JUGADOR".equalsIgnoreCase(
                    inscripcion.getTipoParticipante())) {

                if (!usuario.getId()
                        .equals(inscripcion.getJugadorId())) {

                    throw new InscripcionException(
                            "No tiene permisos sobre esta inscripción",
                            HttpStatus.FORBIDDEN
                    );
                }

                return;
            }

            if ("EQUIPO".equalsIgnoreCase(
                    inscripcion.getTipoParticipante())) {

                EquipoDTO equipo =
                        equipoClient.buscarEquipoPorId(
                                inscripcion.getEquipoId()
                        );

                if (!usuario.getId()
                        .equals(equipo.getCapitanId())) {

                    throw new InscripcionException(
                            "Solo el capitán puede gestionar esta inscripción",
                            HttpStatus.FORBIDDEN
                    );
                }
            }

        } catch (InscripcionException ex) {
            throw ex;

        } catch (Exception ex) {

            logger.error(
                    "No se pudo validar permisos sobre inscripción id={}",
                    inscripcion.getId()
            );

            throw new InscripcionException(
                    "No se pudo validar permisos de la inscripción",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

    private void validarPermisoConsultarJugador(Long jugadorId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new InscripcionException(
                    "Usuario no autenticado",
                    HttpStatus.FORBIDDEN
            );
        }

        boolean esAdminOOrganizador =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN") ||
                                        authority.getAuthority().equals("ROLE_ORGANIZADOR")
                        );

        if (esAdminOOrganizador) {
            return;
        }

        try {

            UsuarioDTO usuario =
                    usuarioClient.buscarUsuarioPorEmail(
                            authentication.getName()
                    );

            if (!usuario.getId().equals(jugadorId)) {

                throw new InscripcionException(
                        "Un jugador solo puede consultar sus propias inscripciones",
                        HttpStatus.FORBIDDEN
                );
            }

        } catch (InscripcionException ex) {
            throw ex;

        } catch (Exception ex) {

            logger.error(
                    "No se pudo validar consulta de inscripciones para jugadorId={}",
                    jugadorId
            );

            throw new InscripcionException(
                    "No se pudo validar permisos del usuario",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

    private void validarPermisoConsultarEquipo(Long equipoId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new InscripcionException(
                    "Usuario no autenticado",
                    HttpStatus.FORBIDDEN
            );
        }

        boolean esAdminOOrganizador =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN") ||
                                        authority.getAuthority().equals("ROLE_ORGANIZADOR")
                        );

        if (esAdminOOrganizador) {
            return;
        }

        try {

            UsuarioDTO usuario =
                    usuarioClient.buscarUsuarioPorEmail(
                            authentication.getName()
                    );

            EquipoDTO equipo =
                    equipoClient.buscarEquipoPorId(equipoId);

            if (!usuario.getId()
                    .equals(equipo.getCapitanId())) {

                throw new InscripcionException(
                        "Solo el capitán puede consultar las inscripciones del equipo",
                        HttpStatus.FORBIDDEN
                );
            }

        } catch (InscripcionException ex) {
            throw ex;

        } catch (Exception ex) {

            logger.error(
                    "No se pudo validar consulta de inscripciones para equipoId={}",
                    equipoId
            );

            throw new InscripcionException(
                    "No se pudo validar permisos del equipo",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

    private void validarModalidadTorneo(
            InscripcionDTO inscripcionDTO,
            TorneoDTO torneoDTO
    ) {

        if (torneoDTO.getModalidad() == null) {
            return;
        }

        if (!torneoDTO.getModalidad()
                .equalsIgnoreCase(
                        inscripcionDTO.getTipoParticipante()
                )) {

            throw new InscripcionException(
                    "La modalidad del participante no corresponde a la modalidad del torneo",
                    HttpStatus.UNPROCESSABLE_CONTENT
            );
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
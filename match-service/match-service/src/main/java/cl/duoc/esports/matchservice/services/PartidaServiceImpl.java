package cl.duoc.esports.matchservice.services;

import cl.duoc.esports.matchservice.clients.InscripcionClient;
import cl.duoc.esports.matchservice.clients.TorneoClient;
import cl.duoc.esports.matchservice.dto.InscripcionDTO;
import cl.duoc.esports.matchservice.dto.TorneoDTO;
import cl.duoc.esports.matchservice.dto.PartidaDTO;
import cl.duoc.esports.matchservice.exceptions.PartidaException;
import cl.duoc.esports.matchservice.models.Partida;
import cl.duoc.esports.matchservice.repositories.PartidaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PartidaServiceImpl implements PartidaService {

    private static final Logger logger = LoggerFactory.getLogger(PartidaServiceImpl.class);

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private TorneoClient torneoClient;

    @Autowired
    private InscripcionClient inscripcionClient;

    @Transactional
    @Override
    public PartidaDTO crearPartida(PartidaDTO partidaDTO) {

        logger.info("Creando partida torneoId={} participanteAId={} participanteBId={} ronda={}",
                partidaDTO.getTorneoId(),
                partidaDTO.getParticipanteAId(),
                partidaDTO.getParticipanteBId(),
                partidaDTO.getRonda());

        validarParticipantesDiferentes(partidaDTO);

        validarTorneoDisponible(partidaDTO.getTorneoId());

        validarParticipanteInscrito(partidaDTO.getParticipanteAId(), partidaDTO.getTorneoId());

        validarParticipanteInscrito(partidaDTO.getParticipanteBId(), partidaDTO.getTorneoId());

        validarPartidaDuplicada(partidaDTO);

        Partida partida = new Partida();

        partida.setTorneoId(partidaDTO.getTorneoId());
        partida.setParticipanteAId(partidaDTO.getParticipanteAId());
        partida.setParticipanteBId(partidaDTO.getParticipanteBId());
        partida.setRonda(partidaDTO.getRonda().toUpperCase());
        partida.setFechaHora(partidaDTO.getFechaHora());
        partida.setEstado("PROGRAMADA");

        Partida partidaGuardada = partidaRepository.save(partida);

        logger.info("Partida creada correctamente id={} torneoId={}",
                partidaGuardada.getId(), partidaGuardada.getTorneoId());

        return convertirADTO(partidaGuardada);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PartidaDTO> listarPartidas() {

        logger.info("Listando partidas");

        return partidaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PartidaDTO buscarPartidaPorId(Long id) {

        logger.info("Buscando partida id={}", id);

        Partida partida = obtenerPartidaPorId(id);
        return convertirADTO(partida);
    }

    @Transactional
    @Override
    public PartidaDTO actualizarPartida(Long id, PartidaDTO partidaDTO) {

        logger.info("Actualizando partida id={}", id);

        Partida partida = obtenerPartidaPorId(id);

        validarParticipantesDiferentes(partidaDTO);

        validarTorneoDisponible(partidaDTO.getTorneoId());

        validarParticipanteInscrito(partidaDTO.getParticipanteAId(), partidaDTO.getTorneoId());

        validarParticipanteInscrito(partidaDTO.getParticipanteBId(), partidaDTO.getTorneoId());

        partida.setTorneoId(partidaDTO.getTorneoId());
        partida.setParticipanteAId(partidaDTO.getParticipanteAId());
        partida.setParticipanteBId(partidaDTO.getParticipanteBId());
        partida.setRonda(partidaDTO.getRonda().toUpperCase());
        partida.setFechaHora(partidaDTO.getFechaHora());

        if (partidaDTO.getEstado() != null && !partidaDTO.getEstado().isBlank()) {
            partida.setEstado(partidaDTO.getEstado().toUpperCase());
        }

        Partida partidaActualizada = partidaRepository.save(partida);

        logger.info("Partida actualizada correctamente id={}", id);

        return convertirADTO(partidaActualizada);
    }

    @Transactional
    @Override
    public PartidaDTO actualizarEstado(Long id, String estado) {

        logger.info("Actualizando estado de partida id={} a estado={}", id, estado);

        Partida partida = obtenerPartidaPorId(id);

        partida.setEstado(estado.toUpperCase());

        Partida partidaActualizada = partidaRepository.save(partida);

        logger.info("Estado de partida actualizado correctamente id={} estado={}",
                id, estado.toUpperCase());

        return convertirADTO(partidaActualizada);
    }

    @Transactional
    @Override
    public void cancelarPartida(Long id) {

        logger.info("Cancelando partida id={}", id);

        Partida partida = obtenerPartidaPorId(id);

        partida.setEstado("CANCELADA");
        partidaRepository.save(partida);

        logger.info("Partida cancelada correctamente id={}", id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PartidaDTO> listarPorTorneo(Long torneoId) {

        logger.info("Listando partidas por torneoId={}", torneoId);

        return partidaRepository.findByTorneoId(torneoId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PartidaDTO> listarPorRonda(String ronda) {

        logger.info("Listando partidas por ronda={}", ronda);

        return partidaRepository.findByRonda(ronda.toUpperCase())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PartidaDTO> listarPorEstado(String estado) {

        logger.info("Listando partidas por estado={}", estado);

        return partidaRepository.findByEstado(estado.toUpperCase())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    private Partida obtenerPartidaPorId(Long id) {
        return partidaRepository.findById(id).orElseThrow(() -> {
            logger.warn("Partida no encontrada id={}", id);
            return new PartidaException("Partida no encontrada");
        });
    }

    private void validarParticipantesDiferentes(PartidaDTO partidaDTO) {
        if (partidaDTO.getParticipanteAId().equals(partidaDTO.getParticipanteBId())) {
            logger.warn("Intento de crear partida con el mismo participante participanteId={}",
                    partidaDTO.getParticipanteAId());

            throw new PartidaException("No se puede crear una partida con el mismo participante");
        }
    }

    private void validarPartidaDuplicada(PartidaDTO partidaDTO) {
        boolean existeMismoOrden = partidaRepository
                .existsByTorneoIdAndRondaAndParticipanteAIdAndParticipanteBId(
                        partidaDTO.getTorneoId(),
                        partidaDTO.getRonda().toUpperCase(),
                        partidaDTO.getParticipanteAId(),
                        partidaDTO.getParticipanteBId()
                );

        boolean existeOrdenInvertido = partidaRepository
                .existsByTorneoIdAndRondaAndParticipanteBIdAndParticipanteAId(
                        partidaDTO.getTorneoId(),
                        partidaDTO.getRonda().toUpperCase(),
                        partidaDTO.getParticipanteAId(),
                        partidaDTO.getParticipanteBId()
                );

        if (existeMismoOrden || existeOrdenInvertido) {
            logger.warn("Intento de crear partida duplicada torneoId={} ronda={} participanteAId={} participanteBId={}",
                    partidaDTO.getTorneoId(),
                    partidaDTO.getRonda(),
                    partidaDTO.getParticipanteAId(),
                    partidaDTO.getParticipanteBId());

            throw new PartidaException("Ya existe una partida entre estos participantes en la misma ronda");
        }
    }

    private void validarTorneoDisponible(Long torneoId) {
        try {
            logger.info("Validando torneoId={} desde tournament-service", torneoId);

            TorneoDTO torneoDTO = torneoClient.buscarTorneoPorId(torneoId);

            if (torneoDTO.getEstado().equalsIgnoreCase("CANCELADO")) {
                logger.warn("No se puede crear partida en torneo cancelado torneoId={}", torneoId);
                throw new PartidaException("No se puede crear una partida en un torneo cancelado");
            }

            if (torneoDTO.getEstado().equalsIgnoreCase("CERRADO")) {
                logger.warn("No se puede crear partida en torneo cerrado torneoId={}", torneoId);
                throw new PartidaException("No se puede crear una partida en un torneo cerrado");
            }

        } catch (PartidaException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("No se pudo validar torneoId={} desde tournament-service", torneoId);
            throw new PartidaException("El torneo no existe o no está disponible");
        }
    }

    private void validarParticipanteInscrito(Long inscripcionId, Long torneoId) {
        try {
            logger.info("Validando participante inscripcionId={} en torneoId={} desde registration-service",
                    inscripcionId, torneoId);

            InscripcionDTO inscripcionDTO = inscripcionClient.buscarInscripcionPorId(inscripcionId);

            if (!inscripcionDTO.getTorneoId().equals(torneoId)) {
                logger.warn("Participante inscripcionId={} no pertenece al torneoId={}",
                        inscripcionId, torneoId);

                throw new PartidaException("El participante no está inscrito en este torneo");
            }

            if (inscripcionDTO.getEstado().equalsIgnoreCase("CANCELADA")) {
                logger.warn("Participante inscripcionId={} tiene inscripción cancelada", inscripcionId);
                throw new PartidaException("No se puede crear una partida con una inscripción cancelada");
            }

        } catch (PartidaException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("No se pudo validar participante inscripcionId={} desde registration-service",
                    inscripcionId);

            throw new PartidaException("El participante no existe o no está inscrito");
        }
    }

    private PartidaDTO convertirADTO(Partida partida) {
        PartidaDTO partidaDTO = new PartidaDTO();

        partidaDTO.setId(partida.getId());
        partidaDTO.setTorneoId(partida.getTorneoId());
        partidaDTO.setParticipanteAId(partida.getParticipanteAId());
        partidaDTO.setParticipanteBId(partida.getParticipanteBId());
        partidaDTO.setRonda(partida.getRonda());
        partidaDTO.setFechaHora(partida.getFechaHora());
        partidaDTO.setEstado(partida.getEstado());

        return partidaDTO;
    }
}
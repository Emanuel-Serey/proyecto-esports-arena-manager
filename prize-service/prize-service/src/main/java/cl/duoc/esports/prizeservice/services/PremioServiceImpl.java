package cl.duoc.esports.prizeservice.services;

import cl.duoc.esports.prizeservice.clients.RankingClient;
import cl.duoc.esports.prizeservice.clients.TorneoClient;
import cl.duoc.esports.prizeservice.dto.PremioDTO;
import cl.duoc.esports.prizeservice.dto.RankingDTO;
import cl.duoc.esports.prizeservice.dto.TorneoDTO;
import cl.duoc.esports.prizeservice.exceptions.PremioException;
import cl.duoc.esports.prizeservice.models.Premio;
import cl.duoc.esports.prizeservice.repositories.PremioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PremioServiceImpl implements PremioService {

    private static final Logger logger = LoggerFactory.getLogger(PremioServiceImpl.class);

    @Autowired
    private PremioRepository premioRepository;

    @Autowired
    private TorneoClient torneoClient;

    @Autowired
    private RankingClient rankingClient;

    @Transactional
    @Override
    public PremioDTO asignarPremio(PremioDTO premioDTO) {

        logger.info("Asignando premio torneoId={} participanteId={} posicion={}",
                premioDTO.getTorneoId(),
                premioDTO.getParticipanteId(),
                premioDTO.getPosicion());

        validarTorneoDisponible(premioDTO.getTorneoId());

        RankingDTO rankingDTO = validarRankingParticipante(
                premioDTO.getTorneoId(),
                premioDTO.getParticipanteId()
        );

        if (!rankingDTO.getPosicion().equals(premioDTO.getPosicion())) {
            logger.warn("Posición incorrecta para participanteId={} en torneoId={}. Posición enviada={} posición real={}",
                    premioDTO.getParticipanteId(),
                    premioDTO.getTorneoId(),
                    premioDTO.getPosicion(),
                    rankingDTO.getPosicion());

            throw new PremioException("La posición indicada no coincide con la posición del participante en el ranking");
        }

        if (premioRepository.existsByTorneoIdAndParticipanteIdAndPosicion(
                premioDTO.getTorneoId(),
                premioDTO.getParticipanteId(),
                premioDTO.getPosicion())) {

            logger.warn("Intento de asignar premio duplicado torneoId={} participanteId={} posicion={}",
                    premioDTO.getTorneoId(),
                    premioDTO.getParticipanteId(),
                    premioDTO.getPosicion());

            throw new PremioException("El premio ya fue asignado a este participante en esta posición");
        }

        Premio premio = new Premio();

        premio.setTorneoId(premioDTO.getTorneoId());
        premio.setParticipanteId(premioDTO.getParticipanteId());
        premio.setPosicion(premioDTO.getPosicion());
        premio.setTipoPremio(premioDTO.getTipoPremio());
        premio.setDescripcion(premioDTO.getDescripcion());
        premio.setEstadoEntrega("PENDIENTE");
        premio.setFechaAsignacion(LocalDate.now());

        Premio premioGuardado = premioRepository.save(premio);

        logger.info("Premio asignado correctamente id={} torneoId={} participanteId={} estadoEntrega={}",
                premioGuardado.getId(),
                premioGuardado.getTorneoId(),
                premioGuardado.getParticipanteId(),
                premioGuardado.getEstadoEntrega());

        return convertirADTO(premioGuardado);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PremioDTO> listarPremios() {

        logger.info("Listando premios");

        return premioRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PremioDTO buscarPremioPorId(Long id) {

        logger.info("Buscando premio id={}", id);

        Premio premio = obtenerPremioPorId(id);
        return convertirADTO(premio);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PremioDTO> listarPremiosPorTorneo(Long torneoId) {

        logger.info("Listando premios por torneoId={}", torneoId);

        return premioRepository.findByTorneoId(torneoId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PremioDTO> listarPremiosPorParticipante(Long participanteId) {

        logger.info("Listando premios por participanteId={}", participanteId);

        return premioRepository.findByParticipanteId(participanteId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PremioDTO> listarPremiosPorEstado(String estadoEntrega) {

        logger.info("Listando premios por estadoEntrega={}", estadoEntrega);

        return premioRepository.findByEstadoEntrega(estadoEntrega.toUpperCase())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional
    @Override
    public PremioDTO marcarComoEntregado(Long id) {

        logger.info("Marcando premio id={} como ENTREGADO", id);

        Premio premio = obtenerPremioPorId(id);

        if (premio.getEstadoEntrega().equalsIgnoreCase("ANULADO")) {
            logger.warn("Intento de entregar premio anulado id={}", id);
            throw new PremioException("No se puede entregar un premio anulado");
        }

        premio.setEstadoEntrega("ENTREGADO");

        Premio premioActualizado = premioRepository.save(premio);

        logger.info("Premio marcado como ENTREGADO correctamente id={}", id);

        return convertirADTO(premioActualizado);
    }

    @Transactional
    @Override
    public void anularPremio(Long id) {

        logger.info("Anulando premio id={}", id);

        Premio premio = obtenerPremioPorId(id);

        if (premio.getEstadoEntrega().equalsIgnoreCase("ENTREGADO")) {
            logger.warn("Intento de anular premio ya entregado id={}", id);
            throw new PremioException("No se puede anular un premio ya entregado");
        }

        premio.setEstadoEntrega("ANULADO");

        premioRepository.save(premio);

        logger.info("Premio anulado correctamente id={}", id);
    }

    private void validarTorneoDisponible(Long torneoId) {
        try {
            logger.info("Validando torneoId={} desde tournament-service", torneoId);

            TorneoDTO torneoDTO = torneoClient.buscarTorneoPorId(torneoId);

            if (torneoDTO.getEstado().equalsIgnoreCase("CANCELADO")) {
                logger.warn("No se puede asignar premio en torneo cancelado torneoId={}", torneoId);
                throw new PremioException("No se puede asignar premio en un torneo cancelado");
            }

        } catch (PremioException ex) {
            throw ex;

        } catch (Exception ex) {
            logger.error("No se pudo validar torneoId={} desde tournament-service", torneoId);
            throw new PremioException("El torneo no existe o no está disponible");
        }
    }

    private RankingDTO validarRankingParticipante(Long torneoId, Long participanteId) {
        try {
            logger.info("Validando ranking de participanteId={} en torneoId={} desde ranking-service",
                    participanteId, torneoId);

            return rankingClient.buscarRankingPorParticipante(torneoId, participanteId);

        } catch (Exception ex) {
            logger.error("No se pudo validar participanteId={} en ranking del torneoId={} desde ranking-service",
                    participanteId, torneoId);

            throw new PremioException("El participante no existe en el ranking del torneo");
        }
    }

    private Premio obtenerPremioPorId(Long id) {
        return premioRepository.findById(id).orElseThrow(() -> {
            logger.warn("Premio no encontrado id={}", id);
            return new PremioException("Premio no encontrado");
        });
    }

    private PremioDTO convertirADTO(Premio premio) {
        PremioDTO premioDTO = new PremioDTO();

        premioDTO.setId(premio.getId());
        premioDTO.setTorneoId(premio.getTorneoId());
        premioDTO.setParticipanteId(premio.getParticipanteId());
        premioDTO.setPosicion(premio.getPosicion());
        premioDTO.setTipoPremio(premio.getTipoPremio());
        premioDTO.setDescripcion(premio.getDescripcion());
        premioDTO.setEstadoEntrega(premio.getEstadoEntrega());
        premioDTO.setFechaAsignacion(premio.getFechaAsignacion());

        return premioDTO;
    }
}
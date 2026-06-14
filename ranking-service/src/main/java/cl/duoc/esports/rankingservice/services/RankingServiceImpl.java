package cl.duoc.esports.rankingservice.services;

import cl.duoc.esports.rankingservice.clients.InscripcionClient;
import cl.duoc.esports.rankingservice.clients.ResultadoClient;
import cl.duoc.esports.rankingservice.clients.TorneoClient;
import cl.duoc.esports.rankingservice.dto.InscripcionDTO;
import cl.duoc.esports.rankingservice.dto.ResultadoDTO;
import cl.duoc.esports.rankingservice.dto.TorneoDTO;
import cl.duoc.esports.rankingservice.dto.RankingDTO;
import cl.duoc.esports.rankingservice.exceptions.RankingException;
import cl.duoc.esports.rankingservice.models.Ranking;
import cl.duoc.esports.rankingservice.repositories.RankingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RankingServiceImpl implements RankingService {

    private static final Logger logger = LoggerFactory.getLogger(RankingServiceImpl.class);

    @Autowired
    private RankingRepository rankingRepository;

    @Autowired
    private TorneoClient torneoClient;

    @Autowired
    private InscripcionClient inscripcionClient;

    @Autowired
    private ResultadoClient resultadoClient;

    @Transactional
    @Override
    public RankingDTO crearRegistroRanking(RankingDTO rankingDTO) {

        logger.info("Creando registro de ranking torneoId={} participanteId={}",
                rankingDTO.getTorneoId(), rankingDTO.getParticipanteId());

        validarTorneoDisponible(rankingDTO.getTorneoId());
        validarParticipanteInscritoEnTorneo(rankingDTO.getParticipanteId(), rankingDTO.getTorneoId());

        if (rankingRepository.existsByTorneoIdAndParticipanteId(
                rankingDTO.getTorneoId(),
                rankingDTO.getParticipanteId())) {

            logger.warn("Intento de duplicar participanteId={} en ranking del torneoId={}",
                    rankingDTO.getParticipanteId(), rankingDTO.getTorneoId());

            throw new RankingException("El participante ya existe en el ranking de este torneo", HttpStatus.CONFLICT);
        }

        Ranking ranking = new Ranking();

        ranking.setTorneoId(rankingDTO.getTorneoId());
        ranking.setParticipanteId(rankingDTO.getParticipanteId());
        ranking.setPuntos(rankingDTO.getPuntos());
        ranking.setVictorias(rankingDTO.getVictorias());
        ranking.setDerrotas(rankingDTO.getDerrotas());

        if (rankingDTO.getDiferencia() == null) {
            ranking.setDiferencia(0);
        } else {
            ranking.setDiferencia(rankingDTO.getDiferencia());
        }

        ranking.setPosicion(0);

        Ranking rankingGuardado = rankingRepository.save(ranking);

        logger.info("Registro de ranking creado id={} torneoId={} participanteId={}",
                rankingGuardado.getId(), rankingGuardado.getTorneoId(), rankingGuardado.getParticipanteId());

        recalcularPosiciones(rankingDTO.getTorneoId());

        Ranking rankingActualizado = obtenerRankingPorId(rankingGuardado.getId());

        return convertirADTO(rankingActualizado);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RankingDTO> listarRankings() {

        logger.info("Listando rankings");

        return rankingRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public RankingDTO buscarRankingPorId(Long id) {

        logger.info("Buscando ranking id={}", id);

        Ranking ranking = obtenerRankingPorId(id);
        return convertirADTO(ranking);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RankingDTO> listarRankingPorTorneo(Long torneoId) {

        logger.info("Listando ranking por torneoId={}", torneoId);

        return rankingRepository.findByTorneoIdOrderByPosicionAsc(torneoId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public RankingDTO buscarPosicionPorParticipante(Long torneoId, Long participanteId) {

        logger.info("Buscando posición de participanteId={} en torneoId={}",
                participanteId, torneoId);

        Ranking ranking = rankingRepository.findByTorneoIdAndParticipanteId(torneoId, participanteId)
                .orElseThrow(() -> {
                    logger.warn("ParticipanteId={} no existe en ranking del torneoId={}",
                            participanteId, torneoId);

                    return new RankingException("El participante no existe en el ranking de este torneo", HttpStatus.NOT_FOUND);
                });

        return convertirADTO(ranking);
    }

    @Transactional
    @Override
    public RankingDTO actualizarRanking(Long id, RankingDTO rankingDTO) {

        logger.info("Actualizando ranking id={}", id);

        Ranking ranking = obtenerRankingPorId(id);

        ranking.setPuntos(rankingDTO.getPuntos());
        ranking.setVictorias(rankingDTO.getVictorias());
        ranking.setDerrotas(rankingDTO.getDerrotas());

        if (rankingDTO.getDiferencia() == null) {
            ranking.setDiferencia(0);
        } else {
            ranking.setDiferencia(rankingDTO.getDiferencia());
        }

        Ranking rankingActualizado = rankingRepository.save(ranking);

        logger.info("Ranking actualizado id={} torneoId={}",
                rankingActualizado.getId(), rankingActualizado.getTorneoId());

        recalcularPosiciones(rankingActualizado.getTorneoId());

        Ranking rankingReordenado = obtenerRankingPorId(rankingActualizado.getId());

        return convertirADTO(rankingReordenado);
    }

    @Transactional
    @Override
    public RankingDTO actualizarRankingPorResultado(
            Long resultadoId,
            Long torneoId,
            Long participanteAId,
            Long participanteBId) {

        logger.info("Actualizando ranking por resultadoId={} torneoId={} participanteAId={} participanteBId={}",
                resultadoId, torneoId, participanteAId, participanteBId);

        validarTorneoDisponible(torneoId);
        validarParticipanteInscritoEnTorneo(participanteAId, torneoId);
        validarParticipanteInscritoEnTorneo(participanteBId, torneoId);

        ResultadoDTO resultadoDTO = validarResultadoDisponible(resultadoId);

        if (!resultadoDTO.getEstadoValidacion().equalsIgnoreCase("VALIDADO")) {
            logger.warn("ResultadoId={} no está validado. estadoValidacion={}",
                    resultadoId, resultadoDTO.getEstadoValidacion());

            throw new RankingException("Solo resultados validados pueden actualizar el ranking", HttpStatus.CONFLICT);
        }

        Long ganadorId = resultadoDTO.getGanadorId();

        if (!ganadorId.equals(participanteAId) && !ganadorId.equals(participanteBId)) {
            logger.warn("GanadorId={} no corresponde a participanteAId={} ni participanteBId={}",
                    ganadorId, participanteAId, participanteBId);

            throw new RankingException("El ganador no corresponde a los participantes indicados", HttpStatus.UNPROCESSABLE_CONTENT);
        }

        Long perdedorId;

        if (ganadorId.equals(participanteAId)) {
            perdedorId = participanteBId;
        } else {
            perdedorId = participanteAId;
        }

        Integer diferenciaGanador;
        Integer diferenciaPerdedor;

        if (ganadorId.equals(participanteAId)) {
            diferenciaGanador = resultadoDTO.getPuntajeA() - resultadoDTO.getPuntajeB();
            diferenciaPerdedor = resultadoDTO.getPuntajeB() - resultadoDTO.getPuntajeA();
        } else {
            diferenciaGanador = resultadoDTO.getPuntajeB() - resultadoDTO.getPuntajeA();
            diferenciaPerdedor = resultadoDTO.getPuntajeA() - resultadoDTO.getPuntajeB();
        }

        Ranking rankingGanador = obtenerOCrearRanking(torneoId, ganadorId);
        Ranking rankingPerdedor = obtenerOCrearRanking(torneoId, perdedorId);

        rankingGanador.setPuntos(rankingGanador.getPuntos() + 3);
        rankingGanador.setVictorias(rankingGanador.getVictorias() + 1);
        rankingGanador.setDiferencia(rankingGanador.getDiferencia() + diferenciaGanador);

        rankingPerdedor.setDerrotas(rankingPerdedor.getDerrotas() + 1);
        rankingPerdedor.setDiferencia(rankingPerdedor.getDiferencia() + diferenciaPerdedor);

        rankingRepository.save(rankingGanador);
        rankingRepository.save(rankingPerdedor);

        logger.info("Ranking actualizado por resultadoId={}. ganadorId={} perdedorId={}",
                resultadoId, ganadorId, perdedorId);

        recalcularPosiciones(torneoId);

        Ranking rankingActualizado = obtenerRankingPorId(rankingGanador.getId());

        return convertirADTO(rankingActualizado);
    }

    @Transactional
    @Override
    public void reiniciarRankingPorTorneo(Long torneoId) {

        logger.info("Reiniciando ranking del torneoId={}", torneoId);

        List<Ranking> rankings = rankingRepository.findByTorneoIdOrderByPosicionAsc(torneoId);

        if (rankings.isEmpty()) {
            logger.warn("No existen registros de ranking para reiniciar torneoId={}", torneoId);
            throw new RankingException("No existen registros de ranking para este torneo", HttpStatus.NOT_FOUND);
        }

        rankingRepository.deleteAll(rankings);

        logger.info("Ranking reiniciado correctamente para torneoId={}", torneoId);
    }

    private void validarTorneoDisponible(Long torneoId) {
        try {
            logger.info("Validando torneoId={} desde tournament-service", torneoId);

            TorneoDTO torneoDTO = torneoClient.buscarTorneoPorId(torneoId);

            if (torneoDTO.getEstado().equalsIgnoreCase("CANCELADO")) {
                logger.warn("No se puede crear ranking para torneo cancelado torneoId={}", torneoId);

                throw new RankingException("No se puede crear ranking para un torneo cancelado", HttpStatus.CONFLICT);
            }

        } catch (RankingException ex) {
            throw ex;

        } catch (FeignException.NotFound ex) {
            logger.warn("TorneoId={} no encontrado en tournament-service", torneoId);

            throw new RankingException("El torneo no existe", HttpStatus.NOT_FOUND);

        } catch (Exception ex) {
            logger.error("No se pudo validar torneoId={} desde tournament-service", torneoId);

            throw new RankingException("No se pudo validar el torneo desde tournament-service", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private void validarParticipanteInscritoEnTorneo(Long participanteId, Long torneoId) {
        try {
            logger.info("Validando participanteId={} en torneoId={} desde registration-service",
                    participanteId, torneoId);

            InscripcionDTO inscripcionDTO = inscripcionClient.buscarInscripcionPorId(participanteId);

            if (!inscripcionDTO.getTorneoId().equals(torneoId)) {
                logger.warn("ParticipanteId={} no pertenece al torneoId={}",
                        participanteId, torneoId);

                throw new RankingException("El participante no pertenece a este torneo", HttpStatus.CONFLICT);
            }

            if (inscripcionDTO.getEstado().equalsIgnoreCase("CANCELADA")) {
                logger.warn("ParticipanteId={} tiene inscripción cancelada", participanteId);

                throw new RankingException("No se puede usar una inscripción cancelada en el ranking", HttpStatus.CONFLICT);
            }

        } catch (RankingException ex) {
            throw ex;

        } catch (FeignException.NotFound ex) {
            logger.warn("ParticipanteId={} no encontrado en registration-service", participanteId);

            throw new RankingException("El participante no existe o no está inscrito", HttpStatus.NOT_FOUND);

        } catch (Exception ex) {
            logger.error("No se pudo validar participanteId={} desde registration-service",
                    participanteId);

            throw new RankingException("No se pudo validar el participante desde registration-service", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private ResultadoDTO validarResultadoDisponible(Long resultadoId) {
        try {
            logger.info("Validando resultadoId={} desde result-service", resultadoId);

            return resultadoClient.buscarResultadoPorId(resultadoId);

        } catch (FeignException.NotFound ex) {
            logger.warn("ResultadoId={} no encontrado en result-service", resultadoId);

            throw new RankingException("El resultado no existe", HttpStatus.NOT_FOUND);

        } catch (Exception ex) {
            logger.error("No se pudo validar resultadoId={} desde result-service", resultadoId);

            throw new RankingException("No se pudo validar el resultado desde result-service", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private Ranking obtenerOCrearRanking(Long torneoId, Long participanteId) {
        return rankingRepository.findByTorneoIdAndParticipanteId(torneoId, participanteId)
                .orElseGet(() -> {
                    logger.info("Creando ranking inicial para torneoId={} participanteId={}",
                            torneoId, participanteId);

                    Ranking ranking = new Ranking();

                    ranking.setTorneoId(torneoId);
                    ranking.setParticipanteId(participanteId);
                    ranking.setPuntos(0);
                    ranking.setVictorias(0);
                    ranking.setDerrotas(0);
                    ranking.setDiferencia(0);
                    ranking.setPosicion(0);

                    return rankingRepository.save(ranking);
                });
    }

    private void recalcularPosiciones(Long torneoId) {

        logger.info("Recalculando posiciones del ranking para torneoId={}", torneoId);

        List<Ranking> rankings = rankingRepository
                .findByTorneoIdOrderByPuntosDescDiferenciaDescVictoriasDesc(torneoId);

        int posicion = 1;

        for (Ranking ranking : rankings) {
            ranking.setPosicion(posicion);
            posicion++;
        }

        rankingRepository.saveAll(rankings);

        logger.info("Posiciones recalculadas correctamente para torneoId={}", torneoId);
    }

    private Ranking obtenerRankingPorId(Long id) {
        return rankingRepository.findById(id).orElseThrow(() -> {
            logger.warn("Registro de ranking no encontrado id={}", id);
            return new RankingException("Registro de ranking no encontrado", HttpStatus.NOT_FOUND);
        });
    }

    private RankingDTO convertirADTO(Ranking ranking) {
        RankingDTO rankingDTO = new RankingDTO();

        rankingDTO.setId(ranking.getId());
        rankingDTO.setTorneoId(ranking.getTorneoId());
        rankingDTO.setParticipanteId(ranking.getParticipanteId());
        rankingDTO.setPuntos(ranking.getPuntos());
        rankingDTO.setVictorias(ranking.getVictorias());
        rankingDTO.setDerrotas(ranking.getDerrotas());
        rankingDTO.setDiferencia(ranking.getDiferencia());
        rankingDTO.setPosicion(ranking.getPosicion());

        return rankingDTO;
    }
}
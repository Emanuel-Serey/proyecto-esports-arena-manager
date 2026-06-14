package cl.duoc.esports.resultservice.services;

import cl.duoc.esports.resultservice.clients.PartidaClient;
import cl.duoc.esports.resultservice.dto.PartidaDTO;
import cl.duoc.esports.resultservice.dto.ResultadoDTO;
import cl.duoc.esports.resultservice.exceptions.ResultadoException;
import cl.duoc.esports.resultservice.models.Resultado;
import cl.duoc.esports.resultservice.repositories.ResultadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ResultadoServiceImpl implements ResultadoService {

    private static final Logger logger = LoggerFactory.getLogger(ResultadoServiceImpl.class);

    @Autowired
    private ResultadoRepository resultadoRepository;

    @Autowired
    private PartidaClient partidaClient;

    @Transactional
    @Override
    public ResultadoDTO crearResultado(ResultadoDTO resultadoDTO) {

        logger.info("Creando resultado para partidaId={} ganadorId={}",
                resultadoDTO.getPartidaId(), resultadoDTO.getGanadorId());

        PartidaDTO partidaDTO = validarPartidaDisponible(resultadoDTO.getPartidaId());

        validarGanadorPerteneceAPartida(resultadoDTO, partidaDTO);

        if (resultadoRepository.existsByPartidaId(resultadoDTO.getPartidaId())) {
            logger.warn("Intento de registrar resultado duplicado para partidaId={}",
                    resultadoDTO.getPartidaId());

            throw new ResultadoException("La partida ya tiene un resultado registrado", HttpStatus.CONFLICT);
        }

        Resultado resultado = new Resultado();

        resultado.setPartidaId(resultadoDTO.getPartidaId());
        resultado.setGanadorId(resultadoDTO.getGanadorId());
        resultado.setPuntajeA(resultadoDTO.getPuntajeA());
        resultado.setPuntajeB(resultadoDTO.getPuntajeB());
        resultado.setEstadoValidacion("PENDIENTE");
        resultado.setFechaRegistro(LocalDate.now());

        Resultado resultadoGuardado = resultadoRepository.save(resultado);

        logger.info("Resultado creado correctamente id={} partidaId={} estadoValidacion={}",
                resultadoGuardado.getId(),
                resultadoGuardado.getPartidaId(),
                resultadoGuardado.getEstadoValidacion());

        return convertirADTO(resultadoGuardado);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ResultadoDTO> listarResultados() {

        logger.info("Listando resultados");

        return resultadoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public ResultadoDTO buscarResultadoPorId(Long id) {

        logger.info("Buscando resultado id={}", id);

        Resultado resultado = obtenerResultadoPorId(id);
        return convertirADTO(resultado);
    }

    @Transactional
    @Override
    public ResultadoDTO actualizarResultado(Long id, ResultadoDTO resultadoDTO) {

        logger.info("Actualizando resultado id={}", id);

        Resultado resultado = obtenerResultadoPorId(id);

        if (resultado.getEstadoValidacion().equalsIgnoreCase("VALIDADO")) {
            logger.warn("Intento de modificar resultado validado id={}", id);
            throw new ResultadoException("No se puede modificar un resultado validado", HttpStatus.CONFLICT);
        }

        if (resultado.getEstadoValidacion().equalsIgnoreCase("ANULADO")) {
            logger.warn("Intento de modificar resultado anulado id={}", id);
            throw new ResultadoException("No se puede modificar un resultado anulado", HttpStatus.CONFLICT);
        }

        PartidaDTO partidaDTO = validarPartidaDisponible(resultado.getPartidaId());

        validarGanadorPerteneceAPartida(resultadoDTO, partidaDTO);

        resultado.setGanadorId(resultadoDTO.getGanadorId());
        resultado.setPuntajeA(resultadoDTO.getPuntajeA());
        resultado.setPuntajeB(resultadoDTO.getPuntajeB());

        Resultado resultadoActualizado = resultadoRepository.save(resultado);

        logger.info("Resultado actualizado correctamente id={}", id);

        return convertirADTO(resultadoActualizado);
    }

    @Transactional
    @Override
    public ResultadoDTO validarResultado(Long id) {

        logger.info("Validando resultado id={}", id);

        Resultado resultado = obtenerResultadoPorId(id);

        if (resultado.getEstadoValidacion().equalsIgnoreCase("ANULADO")) {
            logger.warn("Intento de validar resultado anulado id={}", id);
            throw new ResultadoException("No se puede validar un resultado anulado", HttpStatus.CONFLICT);
        }

        resultado.setEstadoValidacion("VALIDADO");

        Resultado resultadoValidado = resultadoRepository.save(resultado);

        logger.info("Resultado validado correctamente id={}", id);

        return convertirADTO(resultadoValidado);
    }

    @Transactional
    @Override
    public void anularResultado(Long id, String justificacion) {

        logger.info("Anulando resultado id={}", id);

        Resultado resultado = obtenerResultadoPorId(id);

        if (justificacion == null || justificacion.isBlank()) {
            logger.warn("Intento de anular resultado id={} sin justificación", id);
            throw new ResultadoException("Debe indicar una justificación para anular el resultado", HttpStatus.BAD_REQUEST);
        }

        resultado.setEstadoValidacion("ANULADO");
        resultadoRepository.save(resultado);

        logger.info("Resultado anulado correctamente id={} justificacion={}", id, justificacion);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ResultadoDTO> listarPorPartida(Long partidaId) {

        logger.info("Listando resultados por partidaId={}", partidaId);

        return resultadoRepository.findByPartidaId(partidaId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    private Resultado obtenerResultadoPorId(Long id) {
        return resultadoRepository.findById(id).orElseThrow(() -> {
            logger.warn("Resultado no encontrado id={}", id);
            return new ResultadoException("Resultado no encontrado", HttpStatus.NOT_FOUND);
        });
    }

    private PartidaDTO validarPartidaDisponible(Long partidaId) {
        try {
            logger.info("Validando partidaId={} desde match-service", partidaId);

            PartidaDTO partidaDTO = partidaClient.buscarPartidaPorId(partidaId);

            if (partidaDTO.getEstado().equalsIgnoreCase("CANCELADA")) {
                logger.warn("No se puede registrar resultado para partida cancelada partidaId={}", partidaId);

                throw new ResultadoException("No se puede registrar resultado de una partida cancelada", HttpStatus.CONFLICT);
            }

            return partidaDTO;

        } catch (ResultadoException ex) {
            throw ex;

        } catch (FeignException.NotFound ex) {
            logger.warn("PartidaId={} no encontrada en match-service", partidaId);

            throw new ResultadoException("La partida no existe", HttpStatus.NOT_FOUND);

        } catch (Exception ex) {
            logger.error("No se pudo validar partidaId={} desde match-service", partidaId);

            throw new ResultadoException("No se pudo validar la partida desde match-service", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private void validarGanadorPerteneceAPartida(ResultadoDTO resultadoDTO, PartidaDTO partidaDTO) {
        Long ganadorId = resultadoDTO.getGanadorId();

        boolean ganadorEsParticipanteA = ganadorId.equals(partidaDTO.getParticipanteAId());
        boolean ganadorEsParticipanteB = ganadorId.equals(partidaDTO.getParticipanteBId());

        if (!ganadorEsParticipanteA && !ganadorEsParticipanteB) {
            logger.warn("GanadorId={} no pertenece a la partidaId={}",
                    ganadorId, resultadoDTO.getPartidaId());

            throw new ResultadoException("El ganador debe ser uno de los participantes de la partida", HttpStatus.UNPROCESSABLE_CONTENT);
        }
    }

    private ResultadoDTO convertirADTO(Resultado resultado) {
        ResultadoDTO resultadoDTO = new ResultadoDTO();

        resultadoDTO.setId(resultado.getId());
        resultadoDTO.setPartidaId(resultado.getPartidaId());
        resultadoDTO.setGanadorId(resultado.getGanadorId());
        resultadoDTO.setPuntajeA(resultado.getPuntajeA());
        resultadoDTO.setPuntajeB(resultado.getPuntajeB());
        resultadoDTO.setEstadoValidacion(resultado.getEstadoValidacion());
        resultadoDTO.setFechaRegistro(resultado.getFechaRegistro());

        return resultadoDTO;
    }
}
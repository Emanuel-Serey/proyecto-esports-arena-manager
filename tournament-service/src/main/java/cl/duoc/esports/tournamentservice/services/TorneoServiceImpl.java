package cl.duoc.esports.tournamentservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cl.duoc.esports.tournamentservice.clients.JuegoClient;
import cl.duoc.esports.tournamentservice.dto.JuegoDTO;
import cl.duoc.esports.tournamentservice.dto.TorneoDTO;
import cl.duoc.esports.tournamentservice.exceptions.TorneoException;
import cl.duoc.esports.tournamentservice.models.Torneo;
import cl.duoc.esports.tournamentservice.repositories.TorneoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TorneoServiceImpl implements TorneoService {

    private static final Logger logger = LoggerFactory.getLogger(TorneoServiceImpl.class);

    @Autowired
    private TorneoRepository torneoRepository;

    @Autowired
    private JuegoClient juegoClient;

    @Transactional
    @Override
    public TorneoDTO crearTorneo(TorneoDTO torneoDTO) {

        logger.info("Creando torneo nombre={} para juegoId={}", torneoDTO.getNombre(), torneoDTO.getJuegoId());

        validarJuegoDisponible(torneoDTO.getJuegoId());

        if (torneoDTO.getFechaFin().isBefore(torneoDTO.getFechaInicio())) {
            logger.warn("Fecha de fin inválida para torneo nombre={}", torneoDTO.getNombre());
            throw new TorneoException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        Torneo torneo = new Torneo();

        torneo.setNombre(torneoDTO.getNombre());
        torneo.setJuegoId(torneoDTO.getJuegoId());
        torneo.setFechaInicio(torneoDTO.getFechaInicio());
        torneo.setFechaFin(torneoDTO.getFechaFin());
        torneo.setCupoMaximo(torneoDTO.getCupoMaximo());
        torneo.setModalidad(torneoDTO.getModalidad());
        torneo.setEstado("BORRADOR");

        Torneo torneoGuardado = torneoRepository.save(torneo);

        logger.info("Torneo creado correctamente id={}", torneoGuardado.getId());

        return convertirADTO(torneoGuardado);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TorneoDTO> listarTorneos() {
        return torneoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public TorneoDTO buscarTorneoPorId(Long id) {

        logger.info("Buscando torneo id={}", id);

        Torneo torneo = torneoRepository.findById(id).orElseThrow(() -> {
            logger.warn("Torneo no encontrado id={}", id);
            return new TorneoException("Torneo no encontrado");
        });

        return convertirADTO(torneo);
    }

    @Transactional
    @Override
    public TorneoDTO actualizarTorneo(Long id, TorneoDTO torneoDTO) {

        logger.info("Actualizando torneo id={}", id);

        Torneo torneo = torneoRepository.findById(id).orElseThrow(() -> {
            logger.warn("Torneo no encontrado para actualizar. id={}", id);
            return new TorneoException("Torneo no encontrado");
        });

        validarJuegoDisponible(torneoDTO.getJuegoId());

        if (torneoDTO.getFechaFin().isBefore(torneoDTO.getFechaInicio())) {
            logger.warn("Fecha de fin inválida al actualizar torneo id={}", id);
            throw new TorneoException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        torneo.setNombre(torneoDTO.getNombre());
        torneo.setJuegoId(torneoDTO.getJuegoId());
        torneo.setFechaInicio(torneoDTO.getFechaInicio());
        torneo.setFechaFin(torneoDTO.getFechaFin());
        torneo.setCupoMaximo(torneoDTO.getCupoMaximo());
        torneo.setModalidad(torneoDTO.getModalidad());

        Torneo torneoActualizado = torneoRepository.save(torneo);

        logger.info("Torneo actualizado correctamente id={}", id);

        return convertirADTO(torneoActualizado);
    }

    @Transactional
    @Override
    public void cancelarTorneo(Long id) {

        logger.info("Cancelando torneo id={}", id);

        Torneo torneo = torneoRepository.findById(id).orElseThrow(() -> {
            logger.warn("Torneo no encontrado para cancelar. id={}", id);
            return new TorneoException("Torneo no encontrado");
        });

        torneo.setEstado("CANCELADO");
        torneoRepository.save(torneo);

        logger.info("Torneo cancelado correctamente id={}", id);
    }

    @Transactional
    @Override
    public void cerrarTorneo(Long id) {

        logger.info("Cerrando torneo id={}", id);

        Torneo torneo = torneoRepository.findById(id).orElseThrow(() -> {
            logger.warn("Torneo no encontrado para cerrar. id={}", id);
            return new TorneoException("Torneo no encontrado");
        });

        torneo.setEstado("CERRADO");
        torneoRepository.save(torneo);

        logger.info("Torneo cerrado correctamente id={}", id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TorneoDTO> listarPorJuego(Long juegoId) {
        return torneoRepository.findByJuegoId(juegoId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<TorneoDTO> listarPorEstado(String estado) {
        return torneoRepository.findByEstado(estado)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<TorneoDTO> listarPorFecha(LocalDate fechaInicio) {
        return torneoRepository.findByFechaInicio(fechaInicio)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    private void validarJuegoDisponible(Long juegoId) {
        try {
            logger.info("Validando juegoId={} desde game-service", juegoId);

            JuegoDTO juegoDTO = juegoClient.buscarJuegoPorId(juegoId);

            if (!Boolean.TRUE.equals(juegoDTO.getEstado())) {
                logger.warn("JuegoId={} no está activo", juegoId);
                throw new TorneoException("El juego no está activo");
            }

        } catch (TorneoException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("No se pudo validar juegoId={} desde game-service", juegoId);
            throw new TorneoException("El juego no existe o no está disponible");
        }
    }

    private TorneoDTO convertirADTO(Torneo torneo) {
        TorneoDTO torneoDTO = new TorneoDTO();

        torneoDTO.setId(torneo.getId());
        torneoDTO.setNombre(torneo.getNombre());
        torneoDTO.setJuegoId(torneo.getJuegoId());
        torneoDTO.setFechaInicio(torneo.getFechaInicio());
        torneoDTO.setFechaFin(torneo.getFechaFin());
        torneoDTO.setCupoMaximo(torneo.getCupoMaximo());
        torneoDTO.setEstado(torneo.getEstado());
        torneoDTO.setModalidad(torneo.getModalidad());

        return torneoDTO;
    }
}
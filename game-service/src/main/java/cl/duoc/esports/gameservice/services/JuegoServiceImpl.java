package cl.duoc.esports.gameservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cl.duoc.esports.gameservice.dto.JuegoDTO;
import cl.duoc.esports.gameservice.exceptions.JuegoException;
import cl.duoc.esports.gameservice.models.Juego;
import cl.duoc.esports.gameservice.repositories.JuegoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JuegoServiceImpl implements JuegoService {

    private static final Logger logger = LoggerFactory.getLogger(JuegoServiceImpl.class);

    @Autowired
    private JuegoRepository juegoRepository;

    @Transactional
    @Override
    public JuegoDTO crearJuego(JuegoDTO juegoDTO) {

        logger.info("Creando juego con nombre={}", juegoDTO.getNombre());

        if (juegoRepository.existsByNombre(juegoDTO.getNombre())) {
            logger.warn("Intento de crear juego duplicado con nombre={}", juegoDTO.getNombre());
            throw new JuegoException("Ya existe un juego con ese nombre");
        }

        Juego juego = new Juego();
        juego.setNombre(juegoDTO.getNombre());
        juego.setGenero(juegoDTO.getGenero());
        juego.setModalidad(juegoDTO.getModalidad());
        juego.setJugadoresPorEquipo(juegoDTO.getJugadoresPorEquipo());
        juego.setEstado(true);

        Juego juegoGuardado = juegoRepository.save(juego);

        logger.info("Juego creado correctamente con id={}", juegoGuardado.getId());

        return convertirADTO(juegoGuardado);
    }

    @Transactional(readOnly = true)
    @Override
    public List<JuegoDTO> listarJuegos() {
        return juegoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<JuegoDTO> listarJuegosActivos() {
        return juegoRepository.findByEstadoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public JuegoDTO buscarJuegoPorId(Long id) {

        logger.info("Buscando juego id={}", id);

        Juego juego = juegoRepository.findById(id).orElseThrow(() -> {
            logger.warn("Juego no encontrado id={}", id);
            return new JuegoException("Juego no encontrado");
        });

        return convertirADTO(juego);
    }

    @Transactional
    @Override
    public JuegoDTO actualizarJuego(Long id, JuegoDTO juegoDTO) {

        logger.info("Actualizando juego id={}", id);

        Juego juego = juegoRepository.findById(id).orElseThrow(() -> {
            logger.warn("Juego no encontrado para actualizar. id={}", id);
            return new JuegoException("Juego no encontrado");
        });

        juego.setNombre(juegoDTO.getNombre());
        juego.setGenero(juegoDTO.getGenero());
        juego.setModalidad(juegoDTO.getModalidad());
        juego.setJugadoresPorEquipo(juegoDTO.getJugadoresPorEquipo());

        Juego juegoActualizado = juegoRepository.save(juego);

        logger.info("Juego actualizado correctamente id={}", id);

        return convertirADTO(juegoActualizado);
    }

    @Transactional
    @Override
    public void desactivarJuego(Long id) {

        logger.info("Desactivando juego id={}", id);

        Juego juego = juegoRepository.findById(id).orElseThrow(() -> {
            logger.warn("Juego no encontrado para desactivar. id={}", id);
            return new JuegoException("Juego no encontrado");
        });

        juego.setEstado(false);
        juegoRepository.save(juego);

        logger.info("Juego desactivado correctamente id={}", id);
    }

    private JuegoDTO convertirADTO(Juego juego) {
        JuegoDTO juegoDTO = new JuegoDTO();

        juegoDTO.setId(juego.getId());
        juegoDTO.setNombre(juego.getNombre());
        juegoDTO.setGenero(juego.getGenero());
        juegoDTO.setModalidad(juego.getModalidad());
        juegoDTO.setJugadoresPorEquipo(juego.getJugadoresPorEquipo());
        juegoDTO.setEstado(juego.getEstado());

        return juegoDTO;
    }
}
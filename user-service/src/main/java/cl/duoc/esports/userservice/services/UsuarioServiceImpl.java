package cl.duoc.esports.userservice.services;

import cl.duoc.esports.userservice.dto.UsuarioDTO;
import cl.duoc.esports.userservice.exceptions.UsuarioException;
import cl.duoc.esports.userservice.models.Usuario;
import cl.duoc.esports.userservice.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    @Override
    public UsuarioDTO crearUsuario(UsuarioDTO usuarioDTO) {

        logger.info("Creando usuario nickname={} rol={}",
                usuarioDTO.getNickname(), usuarioDTO.getRol());

        if (usuarioRepository.existsByNickname(usuarioDTO.getNickname())) {
            logger.warn("Intento de crear usuario con nickname duplicado={}",
                    usuarioDTO.getNickname());

            throw new UsuarioException("Ya existe un usuario con ese nickname", HttpStatus.CONFLICT);
        }

        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            logger.warn("Intento de crear usuario con email duplicado={}",
                    usuarioDTO.getEmail());

            throw new UsuarioException("Ya existe un usuario con ese email", HttpStatus.CONFLICT);
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setNickname(usuarioDTO.getNickname());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setRol(usuarioDTO.getRol().toUpperCase());
        usuario.setEstado("ACTIVO");
        usuario.setFechaRegistro(LocalDate.now());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        logger.info("Usuario creado correctamente id={} nickname={}",
                usuarioGuardado.getId(), usuarioGuardado.getNickname());

        return convertirADTO(usuarioGuardado);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UsuarioDTO> listarUsuarios() {

        logger.info("Listando usuarios");

        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public UsuarioDTO buscarUsuarioPorId(Long id) {

        logger.info("Buscando usuario id={}", id);

        Usuario usuario = obtenerUsuarioPorId(id);
        return convertirADTO(usuario);
    }

    @Transactional
    @Override
    public UsuarioDTO actualizarUsuario(Long id, UsuarioDTO usuarioDTO) {

        logger.info("Actualizando usuario id={}", id);

        Usuario usuario = obtenerUsuarioPorId(id);

        if (!usuario.getNickname().equalsIgnoreCase(usuarioDTO.getNickname())
                && usuarioRepository.existsByNickname(usuarioDTO.getNickname())) {

            logger.warn("Intento de actualizar usuario id={} con nickname duplicado={}",
                    id, usuarioDTO.getNickname());

            throw new UsuarioException("Ya existe un usuario con ese nickname", HttpStatus.CONFLICT);
        }

        if (!usuario.getEmail().equalsIgnoreCase(usuarioDTO.getEmail())
                && usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {

            logger.warn("Intento de actualizar usuario id={} con email duplicado={}",
                    id, usuarioDTO.getEmail());

            throw new UsuarioException("Ya existe un usuario con ese email", HttpStatus.CONFLICT);
        }

        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setNickname(usuarioDTO.getNickname());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setRol(usuarioDTO.getRol().toUpperCase());

        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        logger.info("Usuario actualizado correctamente id={}", id);

        return convertirADTO(usuarioActualizado);
    }

    @Transactional
    @Override
    public void desactivarUsuario(Long id) {

        logger.info("Desactivando usuario id={}", id);

        Usuario usuario = obtenerUsuarioPorId(id);

        usuario.setEstado("INACTIVO");
        usuarioRepository.save(usuario);

        logger.info("Usuario desactivado correctamente id={}", id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UsuarioDTO> listarPorRol(String rol) {

        logger.info("Listando usuarios por rol={}", rol);

        return usuarioRepository.findByRol(rol.toUpperCase())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UsuarioDTO> listarPorEstado(String estado) {

        logger.info("Listando usuarios por estado={}", estado);

        return usuarioRepository.findByEstado(estado.toUpperCase())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UsuarioDTO> buscarPorNickname(String nickname) {

        logger.info("Buscando usuarios por nickname={}", nickname);

        return usuarioRepository.findByNicknameContainingIgnoreCase(nickname)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public UsuarioDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        return convertirADTO(usuario);
    }

    private Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> {
            logger.warn("Usuario no encontrado id={}", id);
            return new UsuarioException("Usuario no encontrado", HttpStatus.NOT_FOUND);
        });
    }

    private UsuarioDTO convertirADTO(Usuario usuario) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();

        usuarioDTO.setId(usuario.getId());
        usuarioDTO.setNombre(usuario.getNombre());
        usuarioDTO.setNickname(usuario.getNickname());
        usuarioDTO.setEmail(usuario.getEmail());
        usuarioDTO.setRol(usuario.getRol());
        usuarioDTO.setEstado(usuario.getEstado());
        usuarioDTO.setFechaRegistro(usuario.getFechaRegistro());

        return usuarioDTO;
    }
}
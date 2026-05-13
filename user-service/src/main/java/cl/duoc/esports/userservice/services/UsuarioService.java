package cl.duoc.esports.userservice.services;

import cl.duoc.esports.userservice.dto.UsuarioDTO;

import java.util.List;

public interface UsuarioService {

    UsuarioDTO crearUsuario(UsuarioDTO usuarioDTO);

    List<UsuarioDTO> listarUsuarios();

    UsuarioDTO buscarUsuarioPorId(Long id);

    UsuarioDTO actualizarUsuario(Long id, UsuarioDTO usuarioDTO);

    void desactivarUsuario(Long id);

    List<UsuarioDTO> listarPorRol(String rol);

    List<UsuarioDTO> listarPorEstado(String estado);

    List<UsuarioDTO> buscarPorNickname(String nickname);
}
package cl.duoc.esports.userservice.assemblers;

import cl.duoc.esports.userservice.controllers.UsuarioControllerV2;
import cl.duoc.esports.userservice.dto.UsuarioDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UsuarioModelAssembler implements RepresentationModelAssembler<UsuarioDTO, EntityModel<UsuarioDTO>> {

    @Override
    public EntityModel<UsuarioDTO> toModel(UsuarioDTO usuarioDTO) {

        EntityModel<UsuarioDTO> usuarioModel = EntityModel.of(usuarioDTO);

        usuarioModel.add(linkTo(methodOn(UsuarioControllerV2.class)
                .buscarUsuarioPorId(usuarioDTO.getId()))
                .withSelfRel());

        usuarioModel.add(linkTo(methodOn(UsuarioControllerV2.class)
                .listarUsuarios())
                .withRel("usuarios"));

        if (usuarioDTO.getRol() != null) {
            usuarioModel.add(linkTo(methodOn(UsuarioControllerV2.class)
                    .listarPorRol(usuarioDTO.getRol()))
                    .withRel("usuarios-por-rol"));
        }

        if (usuarioDTO.getEstado() != null) {
            usuarioModel.add(linkTo(methodOn(UsuarioControllerV2.class)
                    .listarPorEstado(usuarioDTO.getEstado()))
                    .withRel("usuarios-por-estado"));
        }

        return usuarioModel;
    }
}
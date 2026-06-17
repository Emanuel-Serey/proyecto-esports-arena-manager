package cl.duoc.esports.sanctionservice.assemblers;

import cl.duoc.esports.sanctionservice.controllers.SancionControllerV2;
import cl.duoc.esports.sanctionservice.dto.SancionDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class SancionModelAssembler implements RepresentationModelAssembler<SancionDTO, EntityModel<SancionDTO>> {

    @Override
    public EntityModel<SancionDTO> toModel(SancionDTO sancionDTO) {

        EntityModel<SancionDTO> sancionModel = EntityModel.of(sancionDTO);

        sancionModel.add(linkTo(methodOn(SancionControllerV2.class)
                .buscarSancionPorId(sancionDTO.getId()))
                .withSelfRel());

        sancionModel.add(linkTo(methodOn(SancionControllerV2.class)
                .listarSanciones())
                .withRel("sanciones"));

        if (sancionDTO.getUsuarioId() != null) {
            sancionModel.add(linkTo(methodOn(SancionControllerV2.class)
                    .listarPorUsuario(sancionDTO.getUsuarioId()))
                    .withRel("sanciones-por-usuario"));

            sancionModel.add(linkTo(methodOn(SancionControllerV2.class)
                    .existeSancionActivaUsuario(sancionDTO.getUsuarioId()))
                    .withRel("sancion-activa-usuario"));
        }

        if (sancionDTO.getEquipoId() != null) {
            sancionModel.add(linkTo(methodOn(SancionControllerV2.class)
                    .listarPorEquipo(sancionDTO.getEquipoId()))
                    .withRel("sanciones-por-equipo"));

            sancionModel.add(linkTo(methodOn(SancionControllerV2.class)
                    .existeSancionActivaEquipo(sancionDTO.getEquipoId()))
                    .withRel("sancion-activa-equipo"));
        }

        if (sancionDTO.getEstado() != null) {
            sancionModel.add(linkTo(methodOn(SancionControllerV2.class)
                    .listarPorEstado(sancionDTO.getEstado()))
                    .withRel("sanciones-por-estado"));
        }

        sancionModel.add(linkTo(methodOn(SancionControllerV2.class)
                .cerrarSancion(sancionDTO.getId()))
                .withRel("cerrar-sancion"));

        return sancionModel;
    }
}
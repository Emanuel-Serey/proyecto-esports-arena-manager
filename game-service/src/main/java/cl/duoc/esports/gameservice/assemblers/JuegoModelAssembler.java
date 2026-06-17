package cl.duoc.esports.gameservice.assemblers;

import cl.duoc.esports.gameservice.controllers.JuegoControllerV2;
import cl.duoc.esports.gameservice.dto.JuegoDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class JuegoModelAssembler implements RepresentationModelAssembler<JuegoDTO, EntityModel<JuegoDTO>> {

    @Override
    public EntityModel<JuegoDTO> toModel(JuegoDTO juegoDTO) {

        EntityModel<JuegoDTO> juegoModel = EntityModel.of(juegoDTO);

        juegoModel.add(linkTo(methodOn(JuegoControllerV2.class)
                .buscarJuegoPorId(juegoDTO.getId()))
                .withSelfRel());

        juegoModel.add(linkTo(methodOn(JuegoControllerV2.class)
                .listarJuegos())
                .withRel("juegos"));

        juegoModel.add(linkTo(methodOn(JuegoControllerV2.class)
                .listarJuegosActivos())
                .withRel("juegos-activos"));

        return juegoModel;
    }
}
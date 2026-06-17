package cl.duoc.esports.teamservice.assemblers;

import cl.duoc.esports.teamservice.controllers.EquipoControllerV2;
import cl.duoc.esports.teamservice.dto.EquipoDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class EquipoModelAssembler implements RepresentationModelAssembler<EquipoDTO, EntityModel<EquipoDTO>> {

    @Override
    public EntityModel<EquipoDTO> toModel(EquipoDTO equipoDTO) {

        EntityModel<EquipoDTO> equipoModel = EntityModel.of(equipoDTO);

        equipoModel.add(linkTo(methodOn(EquipoControllerV2.class)
                .buscarEquipoPorId(equipoDTO.getId()))
                .withSelfRel());

        equipoModel.add(linkTo(methodOn(EquipoControllerV2.class)
                .listarEquipos())
                .withRel("equipos"));

        equipoModel.add(linkTo(methodOn(EquipoControllerV2.class)
                .listarMiembros(equipoDTO.getId()))
                .withRel("miembros"));

        if (equipoDTO.getJuegoPrincipalId() != null) {
            equipoModel.add(linkTo(methodOn(EquipoControllerV2.class)
                    .listarPorJuego(equipoDTO.getJuegoPrincipalId()))
                    .withRel("equipos-por-juego"));
        }

        if (equipoDTO.getCapitanId() != null) {
            equipoModel.add(linkTo(methodOn(EquipoControllerV2.class)
                    .listarPorCapitan(equipoDTO.getCapitanId()))
                    .withRel("equipos-por-capitan"));
        }

        if (equipoDTO.getEstado() != null) {
            equipoModel.add(linkTo(methodOn(EquipoControllerV2.class)
                    .listarPorEstado(equipoDTO.getEstado()))
                    .withRel("equipos-por-estado"));
        }

        return equipoModel;
    }
}

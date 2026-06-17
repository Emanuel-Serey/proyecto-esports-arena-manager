package cl.duoc.esports.teamservice.assemblers;

import cl.duoc.esports.teamservice.controllers.EquipoControllerV2;
import cl.duoc.esports.teamservice.dto.MiembroEquipoDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class MiembroEquipoModelAssembler {

    public EntityModel<MiembroEquipoDTO> toModel(MiembroEquipoDTO miembroDTO, Long equipoId) {

        EntityModel<MiembroEquipoDTO> miembroModel = EntityModel.of(miembroDTO);

        miembroModel.add(linkTo(methodOn(EquipoControllerV2.class)
                .buscarEquipoPorId(equipoId))
                .withRel("equipo"));

        miembroModel.add(linkTo(methodOn(EquipoControllerV2.class)
                .listarMiembros(equipoId))
                .withRel("miembros-del-equipo"));

        return miembroModel;
    }
}
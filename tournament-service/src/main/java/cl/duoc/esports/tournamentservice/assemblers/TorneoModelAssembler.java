package cl.duoc.esports.tournamentservice.assemblers;

import cl.duoc.esports.tournamentservice.controllers.TorneoControllerV2;
import cl.duoc.esports.tournamentservice.dto.TorneoDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class TorneoModelAssembler implements RepresentationModelAssembler<TorneoDTO, EntityModel<TorneoDTO>> {

    @Override
    public EntityModel<TorneoDTO> toModel(TorneoDTO torneoDTO) {

        EntityModel<TorneoDTO> torneoModel = EntityModel.of(torneoDTO);

        torneoModel.add(linkTo(methodOn(TorneoControllerV2.class)
                .buscarTorneoPorId(torneoDTO.getId()))
                .withSelfRel());

        torneoModel.add(linkTo(methodOn(TorneoControllerV2.class)
                .listarTorneos())
                .withRel("torneos"));

        if (torneoDTO.getJuegoId() != null) {
            torneoModel.add(linkTo(methodOn(TorneoControllerV2.class)
                    .listarPorJuego(torneoDTO.getJuegoId()))
                    .withRel("torneos-por-juego"));
        }

        if (torneoDTO.getFechaInicio() != null) {
            torneoModel.add(linkTo(methodOn(TorneoControllerV2.class)
                    .listarPorFecha(torneoDTO.getFechaInicio()))
                    .withRel("torneos-por-fecha"));
        }

        if (torneoDTO.getEstado() != null) {
            torneoModel.add(linkTo(methodOn(TorneoControllerV2.class)
                    .listarPorEstado(torneoDTO.getEstado()))
                    .withRel("torneos-por-estado"));
        }

        torneoModel.add(linkTo(methodOn(TorneoControllerV2.class)
                .cancelarTorneo(torneoDTO.getId()))
                .withRel("cancelar-torneo"));

        torneoModel.add(linkTo(methodOn(TorneoControllerV2.class)
                .cerrarTorneo(torneoDTO.getId()))
                .withRel("cerrar-torneo"));

        return torneoModel;
    }
}
package cl.duoc.esports.matchservice.assemblers;

import cl.duoc.esports.matchservice.controllers.PartidaControllerV2;
import cl.duoc.esports.matchservice.dto.PartidaDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class PartidaModelAssembler implements RepresentationModelAssembler<PartidaDTO, EntityModel<PartidaDTO>> {

    @Override
    public EntityModel<PartidaDTO> toModel(PartidaDTO partidaDTO) {

        EntityModel<PartidaDTO> partidaModel = EntityModel.of(partidaDTO);

        partidaModel.add(linkTo(methodOn(PartidaControllerV2.class)
                .buscarPartidaPorId(partidaDTO.getId()))
                .withSelfRel());

        partidaModel.add(linkTo(methodOn(PartidaControllerV2.class)
                .listarPartidas())
                .withRel("partidas"));

        if (partidaDTO.getTorneoId() != null) {
            partidaModel.add(linkTo(methodOn(PartidaControllerV2.class)
                    .listarPorTorneo(partidaDTO.getTorneoId()))
                    .withRel("partidas-por-torneo"));
        }

        if (partidaDTO.getRonda() != null) {
            partidaModel.add(linkTo(methodOn(PartidaControllerV2.class)
                    .listarPorRonda(partidaDTO.getRonda()))
                    .withRel("partidas-por-ronda"));
        }

        if (partidaDTO.getEstado() != null) {
            partidaModel.add(linkTo(methodOn(PartidaControllerV2.class)
                    .listarPorEstado(partidaDTO.getEstado()))
                    .withRel("partidas-por-estado"));
        }

        partidaModel.add(linkTo(methodOn(PartidaControllerV2.class)
                .cancelarPartida(partidaDTO.getId()))
                .withRel("cancelar-partida"));

        return partidaModel;
    }
}
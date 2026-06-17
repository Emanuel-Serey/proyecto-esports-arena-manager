package cl.duoc.esports.rankingservice.assemblers;

import cl.duoc.esports.rankingservice.controllers.RankingControllerV2;
import cl.duoc.esports.rankingservice.dto.RankingDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class RankingModelAssembler implements RepresentationModelAssembler<RankingDTO, EntityModel<RankingDTO>> {

    @Override
    public EntityModel<RankingDTO> toModel(RankingDTO rankingDTO) {

        EntityModel<RankingDTO> rankingModel = EntityModel.of(rankingDTO);

        rankingModel.add(linkTo(methodOn(RankingControllerV2.class)
                .buscarRankingPorId(rankingDTO.getId()))
                .withSelfRel());

        rankingModel.add(linkTo(methodOn(RankingControllerV2.class)
                .listarRankings())
                .withRel("rankings"));

        if (rankingDTO.getTorneoId() != null) {
            rankingModel.add(linkTo(methodOn(RankingControllerV2.class)
                    .listarRankingPorTorneo(rankingDTO.getTorneoId()))
                    .withRel("ranking-por-torneo"));

            rankingModel.add(linkTo(methodOn(RankingControllerV2.class)
                    .reiniciarRankingPorTorneo(rankingDTO.getTorneoId()))
                    .withRel("reiniciar-ranking-torneo"));
        }

        if (rankingDTO.getTorneoId() != null && rankingDTO.getParticipanteId() != null) {
            rankingModel.add(linkTo(methodOn(RankingControllerV2.class)
                    .buscarPosicionPorParticipante(
                            rankingDTO.getTorneoId(),
                            rankingDTO.getParticipanteId()))
                    .withRel("posicion-participante"));
        }

        return rankingModel;
    }
}
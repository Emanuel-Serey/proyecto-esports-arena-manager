package cl.duoc.esports.prizeservice.assemblers;

import cl.duoc.esports.prizeservice.controllers.PremioControllerV2;
import cl.duoc.esports.prizeservice.dto.PremioDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class PremioModelAssembler implements RepresentationModelAssembler<PremioDTO, EntityModel<PremioDTO>> {

    @Override
    public EntityModel<PremioDTO> toModel(PremioDTO premioDTO) {

        EntityModel<PremioDTO> premioModel = EntityModel.of(premioDTO);

        premioModel.add(linkTo(methodOn(PremioControllerV2.class)
                .buscarPremioPorId(premioDTO.getId()))
                .withSelfRel());

        premioModel.add(linkTo(methodOn(PremioControllerV2.class)
                .listarPremios())
                .withRel("premios"));

        if (premioDTO.getTorneoId() != null) {
            premioModel.add(linkTo(methodOn(PremioControllerV2.class)
                    .listarPremiosPorTorneo(premioDTO.getTorneoId()))
                    .withRel("premios-por-torneo"));
        }

        if (premioDTO.getParticipanteId() != null) {
            premioModel.add(linkTo(methodOn(PremioControllerV2.class)
                    .listarPremiosPorParticipante(premioDTO.getParticipanteId()))
                    .withRel("premios-por-participante"));
        }

        if (premioDTO.getEstadoEntrega() != null) {
            premioModel.add(linkTo(methodOn(PremioControllerV2.class)
                    .listarPremiosPorEstado(premioDTO.getEstadoEntrega()))
                    .withRel("premios-por-estado"));
        }

        premioModel.add(linkTo(methodOn(PremioControllerV2.class)
                .marcarComoEntregado(premioDTO.getId()))
                .withRel("entregar-premio"));

        premioModel.add(linkTo(methodOn(PremioControllerV2.class)
                .anularPremio(premioDTO.getId()))
                .withRel("anular-premio"));

        return premioModel;
    }
}
package cl.duoc.esports.resultservice.assemblers;

import cl.duoc.esports.resultservice.controllers.ResultadoControllerV2;
import cl.duoc.esports.resultservice.dto.ResultadoDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ResultadoModelAssembler implements RepresentationModelAssembler<ResultadoDTO, EntityModel<ResultadoDTO>> {

    @Override
    public EntityModel<ResultadoDTO> toModel(ResultadoDTO resultadoDTO) {

        EntityModel<ResultadoDTO> resultadoModel = EntityModel.of(resultadoDTO);

        resultadoModel.add(linkTo(methodOn(ResultadoControllerV2.class)
                .buscarResultadoPorId(resultadoDTO.getId()))
                .withSelfRel());

        resultadoModel.add(linkTo(methodOn(ResultadoControllerV2.class)
                .listarResultados())
                .withRel("resultados"));

        if (resultadoDTO.getPartidaId() != null) {
            resultadoModel.add(linkTo(methodOn(ResultadoControllerV2.class)
                    .listarPorPartida(resultadoDTO.getPartidaId()))
                    .withRel("resultados-por-partida"));
        }

        resultadoModel.add(linkTo(methodOn(ResultadoControllerV2.class)
                .validarResultado(resultadoDTO.getId()))
                .withRel("validar-resultado"));

        resultadoModel.add(linkTo(methodOn(ResultadoControllerV2.class)
                .anularResultado(resultadoDTO.getId(), "Justificación requerida"))
                .withRel("anular-resultado"));

        return resultadoModel;
    }
}
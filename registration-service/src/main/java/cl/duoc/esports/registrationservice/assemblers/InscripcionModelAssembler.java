package cl.duoc.esports.registrationservice.assemblers;

import cl.duoc.esports.registrationservice.controllers.InscripcionControllerV2;
import cl.duoc.esports.registrationservice.dto.InscripcionDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class InscripcionModelAssembler implements RepresentationModelAssembler<InscripcionDTO, EntityModel<InscripcionDTO>> {

    @Override
    public EntityModel<InscripcionDTO> toModel(InscripcionDTO inscripcionDTO) {

        EntityModel<InscripcionDTO> inscripcionModel = EntityModel.of(inscripcionDTO);

        inscripcionModel.add(linkTo(methodOn(InscripcionControllerV2.class)
                .buscarInscripcionPorId(inscripcionDTO.getId()))
                .withSelfRel());

        inscripcionModel.add(linkTo(methodOn(InscripcionControllerV2.class)
                .listarInscripciones())
                .withRel("inscripciones"));

        if (inscripcionDTO.getTorneoId() != null) {
            inscripcionModel.add(linkTo(methodOn(InscripcionControllerV2.class)
                    .listarPorTorneo(inscripcionDTO.getTorneoId()))
                    .withRel("inscripciones-por-torneo"));
        }

        if (inscripcionDTO.getEquipoId() != null) {
            inscripcionModel.add(linkTo(methodOn(InscripcionControllerV2.class)
                    .listarPorEquipo(inscripcionDTO.getEquipoId()))
                    .withRel("inscripciones-por-equipo"));
        }

        if (inscripcionDTO.getJugadorId() != null) {
            inscripcionModel.add(linkTo(methodOn(InscripcionControllerV2.class)
                    .listarPorJugador(inscripcionDTO.getJugadorId()))
                    .withRel("inscripciones-por-jugador"));
        }

        inscripcionModel.add(linkTo(methodOn(InscripcionControllerV2.class)
                .cancelarInscripcion(inscripcionDTO.getId()))
                .withRel("cancelar-inscripcion"));

        return inscripcionModel;
    }
}
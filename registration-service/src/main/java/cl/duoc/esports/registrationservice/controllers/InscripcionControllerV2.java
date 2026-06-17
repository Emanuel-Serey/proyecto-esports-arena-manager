package cl.duoc.esports.registrationservice.controllers;

import cl.duoc.esports.registrationservice.assemblers.InscripcionModelAssembler;
import cl.duoc.esports.registrationservice.dto.InscripcionDTO;
import cl.duoc.esports.registrationservice.services.InscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v2/inscripciones")
@Tag(name = "Inscripciones V2 - HATEOAS", description = "Endpoints de inscripciones con respuestas HATEOAS")
public class InscripcionControllerV2 {

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private InscripcionModelAssembler inscripcionModelAssembler;

    @PostMapping
    @Operation(summary = "Crear inscripción con HATEOAS")
    public ResponseEntity<EntityModel<InscripcionDTO>> crearInscripcion(@Valid @RequestBody InscripcionDTO inscripcionDTO) {
        InscripcionDTO nuevaInscripcion = inscripcionService.crearInscripcion(inscripcionDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inscripcionModelAssembler.toModel(nuevaInscripcion));
    }

    @GetMapping
    @Operation(summary = "Listar inscripciones con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<InscripcionDTO>>> listarInscripciones() {
        List<EntityModel<InscripcionDTO>> inscripciones = inscripcionService.listarInscripciones()
                .stream()
                .map(inscripcionModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<InscripcionDTO>> collectionModel = CollectionModel.of(
                inscripciones,
                linkTo(methodOn(InscripcionControllerV2.class).listarInscripciones()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar inscripción por ID con HATEOAS")
    public ResponseEntity<EntityModel<InscripcionDTO>> buscarInscripcionPorId(@PathVariable Long id) {
        InscripcionDTO inscripcionDTO = inscripcionService.buscarInscripcionPorId(id);

        return ResponseEntity.ok(inscripcionModelAssembler.toModel(inscripcionDTO));
    }

    @PutMapping("/{id}/estado/{estado}")
    @Operation(summary = "Actualizar estado de inscripción con HATEOAS")
    public ResponseEntity<EntityModel<InscripcionDTO>> actualizarEstado(
            @PathVariable Long id,
            @PathVariable String estado) {

        InscripcionDTO inscripcionActualizada = inscripcionService.actualizarEstado(id, estado);

        return ResponseEntity.ok(inscripcionModelAssembler.toModel(inscripcionActualizada));
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar inscripción")
    public ResponseEntity<Void> cancelarInscripcion(@PathVariable Long id) {
        inscripcionService.cancelarInscripcion(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/torneo/{torneoId}")
    @Operation(summary = "Listar inscripciones por torneo con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<InscripcionDTO>>> listarPorTorneo(
            @PathVariable Long torneoId) {

        List<EntityModel<InscripcionDTO>> inscripciones = inscripcionService.listarPorTorneo(torneoId)
                .stream()
                .map(inscripcionModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<InscripcionDTO>> collectionModel = CollectionModel.of(
                inscripciones,
                linkTo(methodOn(InscripcionControllerV2.class).listarPorTorneo(torneoId)).withSelfRel(),
                linkTo(methodOn(InscripcionControllerV2.class).listarInscripciones()).withRel("inscripciones")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/equipo/{equipoId}")
    @Operation(summary = "Listar inscripciones por equipo con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<InscripcionDTO>>> listarPorEquipo(
            @PathVariable Long equipoId) {

        List<EntityModel<InscripcionDTO>> inscripciones = inscripcionService.listarPorEquipo(equipoId)
                .stream()
                .map(inscripcionModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<InscripcionDTO>> collectionModel = CollectionModel.of(
                inscripciones,
                linkTo(methodOn(InscripcionControllerV2.class).listarPorEquipo(equipoId)).withSelfRel(),
                linkTo(methodOn(InscripcionControllerV2.class).listarInscripciones()).withRel("inscripciones")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/jugador/{jugadorId}")
    @Operation(summary = "Listar inscripciones por jugador con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<InscripcionDTO>>> listarPorJugador(
            @PathVariable Long jugadorId) {

        List<EntityModel<InscripcionDTO>> inscripciones = inscripcionService.listarPorJugador(jugadorId)
                .stream()
                .map(inscripcionModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<InscripcionDTO>> collectionModel = CollectionModel.of(
                inscripciones,
                linkTo(methodOn(InscripcionControllerV2.class).listarPorJugador(jugadorId)).withSelfRel(),
                linkTo(methodOn(InscripcionControllerV2.class).listarInscripciones()).withRel("inscripciones")
        );

        return ResponseEntity.ok(collectionModel);
    }
}
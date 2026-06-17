package cl.duoc.esports.tournamentservice.controllers;

import cl.duoc.esports.tournamentservice.assemblers.TorneoModelAssembler;
import cl.duoc.esports.tournamentservice.dto.TorneoDTO;
import cl.duoc.esports.tournamentservice.services.TorneoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v2/torneos")
@Tag(name = "Torneos V2 - HATEOAS", description = "Endpoints de torneos con respuestas HATEOAS")
public class TorneoControllerV2 {

    @Autowired
    private TorneoService torneoService;

    @Autowired
    private TorneoModelAssembler torneoModelAssembler;

    @PostMapping
    @Operation(summary = "Crear torneo con HATEOAS")
    public ResponseEntity<EntityModel<TorneoDTO>> crearTorneo(@Valid @RequestBody TorneoDTO torneoDTO) {
        TorneoDTO nuevoTorneo = torneoService.crearTorneo(torneoDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(torneoModelAssembler.toModel(nuevoTorneo));
    }

    @GetMapping
    @Operation(summary = "Listar torneos con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<TorneoDTO>>> listarTorneos() {
        List<EntityModel<TorneoDTO>> torneos = torneoService.listarTorneos()
                .stream()
                .map(torneoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<TorneoDTO>> collectionModel = CollectionModel.of(
                torneos,
                linkTo(methodOn(TorneoControllerV2.class).listarTorneos()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar torneo por ID con HATEOAS")
    public ResponseEntity<EntityModel<TorneoDTO>> buscarTorneoPorId(@PathVariable Long id) {
        TorneoDTO torneoDTO = torneoService.buscarTorneoPorId(id);

        return ResponseEntity.ok(torneoModelAssembler.toModel(torneoDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar torneo con HATEOAS")
    public ResponseEntity<EntityModel<TorneoDTO>> actualizarTorneo(
            @PathVariable Long id,
            @Valid @RequestBody TorneoDTO torneoDTO) {

        TorneoDTO torneoActualizado = torneoService.actualizarTorneo(id, torneoDTO);

        return ResponseEntity.ok(torneoModelAssembler.toModel(torneoActualizado));
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar torneo")
    public ResponseEntity<Void> cancelarTorneo(@PathVariable Long id) {
        torneoService.cancelarTorneo(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar torneo")
    public ResponseEntity<Void> cerrarTorneo(@PathVariable Long id) {
        torneoService.cerrarTorneo(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/juego/{juegoId}")
    @Operation(summary = "Listar torneos por juego con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<TorneoDTO>>> listarPorJuego(@PathVariable Long juegoId) {
        List<EntityModel<TorneoDTO>> torneos = torneoService.listarPorJuego(juegoId)
                .stream()
                .map(torneoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<TorneoDTO>> collectionModel = CollectionModel.of(
                torneos,
                linkTo(methodOn(TorneoControllerV2.class).listarPorJuego(juegoId)).withSelfRel(),
                linkTo(methodOn(TorneoControllerV2.class).listarTorneos()).withRel("torneos")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/fecha/{fecha}")
    @Operation(summary = "Listar torneos por fecha con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<TorneoDTO>>> listarPorFecha(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        List<EntityModel<TorneoDTO>> torneos = torneoService.listarPorFecha(fecha)
                .stream()
                .map(torneoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<TorneoDTO>> collectionModel = CollectionModel.of(
                torneos,
                linkTo(methodOn(TorneoControllerV2.class).listarPorFecha(fecha)).withSelfRel(),
                linkTo(methodOn(TorneoControllerV2.class).listarTorneos()).withRel("torneos")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar torneos por estado con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<TorneoDTO>>> listarPorEstado(@PathVariable String estado) {
        List<EntityModel<TorneoDTO>> torneos = torneoService.listarPorEstado(estado)
                .stream()
                .map(torneoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<TorneoDTO>> collectionModel = CollectionModel.of(
                torneos,
                linkTo(methodOn(TorneoControllerV2.class).listarPorEstado(estado)).withSelfRel(),
                linkTo(methodOn(TorneoControllerV2.class).listarTorneos()).withRel("torneos")
        );

        return ResponseEntity.ok(collectionModel);
    }
}
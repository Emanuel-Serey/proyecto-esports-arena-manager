package cl.duoc.esports.matchservice.controllers;

import cl.duoc.esports.matchservice.assemblers.PartidaModelAssembler;
import cl.duoc.esports.matchservice.dto.PartidaDTO;
import cl.duoc.esports.matchservice.services.PartidaService;
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
@RequestMapping("/api/v2/partidas")
@Tag(name = "Partidas V2 - HATEOAS", description = "Endpoints de partidas con respuestas HATEOAS")
public class PartidaControllerV2 {

    @Autowired
    private PartidaService partidaService;

    @Autowired
    private PartidaModelAssembler partidaModelAssembler;

    @PostMapping
    @Operation(summary = "Crear partida con HATEOAS")
    public ResponseEntity<EntityModel<PartidaDTO>> crearPartida(@Valid @RequestBody PartidaDTO partidaDTO) {
        PartidaDTO nuevaPartida = partidaService.crearPartida(partidaDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(partidaModelAssembler.toModel(nuevaPartida));
    }

    @GetMapping
    @Operation(summary = "Listar partidas con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<PartidaDTO>>> listarPartidas() {
        List<EntityModel<PartidaDTO>> partidas = partidaService.listarPartidas()
                .stream()
                .map(partidaModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<PartidaDTO>> collectionModel = CollectionModel.of(
                partidas,
                linkTo(methodOn(PartidaControllerV2.class).listarPartidas()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar partida por ID con HATEOAS")
    public ResponseEntity<EntityModel<PartidaDTO>> buscarPartidaPorId(@PathVariable Long id) {
        PartidaDTO partidaDTO = partidaService.buscarPartidaPorId(id);

        return ResponseEntity.ok(partidaModelAssembler.toModel(partidaDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar partida con HATEOAS")
    public ResponseEntity<EntityModel<PartidaDTO>> actualizarPartida(
            @PathVariable Long id,
            @Valid @RequestBody PartidaDTO partidaDTO) {

        PartidaDTO partidaActualizada = partidaService.actualizarPartida(id, partidaDTO);

        return ResponseEntity.ok(partidaModelAssembler.toModel(partidaActualizada));
    }

    @PutMapping("/{id}/estado/{estado}")
    @Operation(summary = "Actualizar estado de partida con HATEOAS")
    public ResponseEntity<EntityModel<PartidaDTO>> actualizarEstado(
            @PathVariable Long id,
            @PathVariable String estado) {

        PartidaDTO partidaActualizada = partidaService.actualizarEstado(id, estado);

        return ResponseEntity.ok(partidaModelAssembler.toModel(partidaActualizada));
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar partida")
    public ResponseEntity<Void> cancelarPartida(@PathVariable Long id) {
        partidaService.cancelarPartida(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/torneo/{torneoId}")
    @Operation(summary = "Listar partidas por torneo con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<PartidaDTO>>> listarPorTorneo(
            @PathVariable Long torneoId) {

        List<EntityModel<PartidaDTO>> partidas = partidaService.listarPorTorneo(torneoId)
                .stream()
                .map(partidaModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<PartidaDTO>> collectionModel = CollectionModel.of(
                partidas,
                linkTo(methodOn(PartidaControllerV2.class).listarPorTorneo(torneoId)).withSelfRel(),
                linkTo(methodOn(PartidaControllerV2.class).listarPartidas()).withRel("partidas")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/ronda/{ronda}")
    @Operation(summary = "Listar partidas por ronda con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<PartidaDTO>>> listarPorRonda(
            @PathVariable String ronda) {

        List<EntityModel<PartidaDTO>> partidas = partidaService.listarPorRonda(ronda)
                .stream()
                .map(partidaModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<PartidaDTO>> collectionModel = CollectionModel.of(
                partidas,
                linkTo(methodOn(PartidaControllerV2.class).listarPorRonda(ronda)).withSelfRel(),
                linkTo(methodOn(PartidaControllerV2.class).listarPartidas()).withRel("partidas")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar partidas por estado con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<PartidaDTO>>> listarPorEstado(
            @PathVariable String estado) {

        List<EntityModel<PartidaDTO>> partidas = partidaService.listarPorEstado(estado)
                .stream()
                .map(partidaModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<PartidaDTO>> collectionModel = CollectionModel.of(
                partidas,
                linkTo(methodOn(PartidaControllerV2.class).listarPorEstado(estado)).withSelfRel(),
                linkTo(methodOn(PartidaControllerV2.class).listarPartidas()).withRel("partidas")
        );

        return ResponseEntity.ok(collectionModel);
    }
}
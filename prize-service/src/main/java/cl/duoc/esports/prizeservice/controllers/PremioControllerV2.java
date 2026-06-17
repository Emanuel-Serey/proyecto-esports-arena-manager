package cl.duoc.esports.prizeservice.controllers;

import cl.duoc.esports.prizeservice.assemblers.PremioModelAssembler;
import cl.duoc.esports.prizeservice.dto.PremioDTO;
import cl.duoc.esports.prizeservice.services.PremioService;
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
@RequestMapping("/api/v2/premios")
@Tag(name = "Premios V2 - HATEOAS", description = "Endpoints de premios con respuestas HATEOAS")
public class PremioControllerV2 {

    @Autowired
    private PremioService premioService;

    @Autowired
    private PremioModelAssembler premioModelAssembler;

    @PostMapping
    @Operation(summary = "Asignar premio con HATEOAS")
    public ResponseEntity<EntityModel<PremioDTO>> asignarPremio(@Valid @RequestBody PremioDTO premioDTO) {
        PremioDTO nuevoPremio = premioService.asignarPremio(premioDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(premioModelAssembler.toModel(nuevoPremio));
    }

    @GetMapping
    @Operation(summary = "Listar premios con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<PremioDTO>>> listarPremios() {
        List<EntityModel<PremioDTO>> premios = premioService.listarPremios()
                .stream()
                .map(premioModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<PremioDTO>> collectionModel = CollectionModel.of(
                premios,
                linkTo(methodOn(PremioControllerV2.class).listarPremios()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar premio por ID con HATEOAS")
    public ResponseEntity<EntityModel<PremioDTO>> buscarPremioPorId(@PathVariable Long id) {
        PremioDTO premioDTO = premioService.buscarPremioPorId(id);

        return ResponseEntity.ok(premioModelAssembler.toModel(premioDTO));
    }

    @GetMapping("/torneo/{torneoId}")
    @Operation(summary = "Listar premios por torneo con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<PremioDTO>>> listarPremiosPorTorneo(
            @PathVariable Long torneoId) {

        List<EntityModel<PremioDTO>> premios = premioService.listarPremiosPorTorneo(torneoId)
                .stream()
                .map(premioModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<PremioDTO>> collectionModel = CollectionModel.of(
                premios,
                linkTo(methodOn(PremioControllerV2.class).listarPremiosPorTorneo(torneoId)).withSelfRel(),
                linkTo(methodOn(PremioControllerV2.class).listarPremios()).withRel("premios")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/participante/{participanteId}")
    @Operation(summary = "Listar premios por participante con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<PremioDTO>>> listarPremiosPorParticipante(
            @PathVariable Long participanteId) {

        List<EntityModel<PremioDTO>> premios = premioService.listarPremiosPorParticipante(participanteId)
                .stream()
                .map(premioModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<PremioDTO>> collectionModel = CollectionModel.of(
                premios,
                linkTo(methodOn(PremioControllerV2.class).listarPremiosPorParticipante(participanteId)).withSelfRel(),
                linkTo(methodOn(PremioControllerV2.class).listarPremios()).withRel("premios")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/estado/{estadoEntrega}")
    @Operation(summary = "Listar premios por estado de entrega con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<PremioDTO>>> listarPremiosPorEstado(
            @PathVariable String estadoEntrega) {

        List<EntityModel<PremioDTO>> premios = premioService.listarPremiosPorEstado(estadoEntrega)
                .stream()
                .map(premioModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<PremioDTO>> collectionModel = CollectionModel.of(
                premios,
                linkTo(methodOn(PremioControllerV2.class).listarPremiosPorEstado(estadoEntrega)).withSelfRel(),
                linkTo(methodOn(PremioControllerV2.class).listarPremios()).withRel("premios")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @PutMapping("/{id}/entregar")
    @Operation(summary = "Marcar premio como entregado con HATEOAS")
    public ResponseEntity<EntityModel<PremioDTO>> marcarComoEntregado(@PathVariable Long id) {
        PremioDTO premioEntregado = premioService.marcarComoEntregado(id);

        return ResponseEntity.ok(premioModelAssembler.toModel(premioEntregado));
    }

    @PutMapping("/{id}/anular")
    @Operation(summary = "Anular premio")
    public ResponseEntity<Void> anularPremio(@PathVariable Long id) {
        premioService.anularPremio(id);

        return ResponseEntity.noContent().build();
    }
}
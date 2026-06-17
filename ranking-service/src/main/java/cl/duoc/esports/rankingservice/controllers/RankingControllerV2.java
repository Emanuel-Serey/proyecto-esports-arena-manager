package cl.duoc.esports.rankingservice.controllers;

import cl.duoc.esports.rankingservice.assemblers.RankingModelAssembler;
import cl.duoc.esports.rankingservice.dto.RankingDTO;
import cl.duoc.esports.rankingservice.services.RankingService;
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
@RequestMapping("/api/v2/rankings")
@Tag(name = "Rankings V2 - HATEOAS", description = "Endpoints de rankings con respuestas HATEOAS")
public class RankingControllerV2 {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private RankingModelAssembler rankingModelAssembler;

    @PostMapping
    @Operation(summary = "Crear registro de ranking con HATEOAS")
    public ResponseEntity<EntityModel<RankingDTO>> crearRegistroRanking(
            @Valid @RequestBody RankingDTO rankingDTO) {

        RankingDTO nuevoRanking = rankingService.crearRegistroRanking(rankingDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(rankingModelAssembler.toModel(nuevoRanking));
    }

    @GetMapping
    @Operation(summary = "Listar rankings con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<RankingDTO>>> listarRankings() {
        List<EntityModel<RankingDTO>> rankings = rankingService.listarRankings()
                .stream()
                .map(rankingModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<RankingDTO>> collectionModel = CollectionModel.of(
                rankings,
                linkTo(methodOn(RankingControllerV2.class).listarRankings()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ranking por ID con HATEOAS")
    public ResponseEntity<EntityModel<RankingDTO>> buscarRankingPorId(
            @PathVariable Long id) {

        RankingDTO rankingDTO = rankingService.buscarRankingPorId(id);

        return ResponseEntity.ok(rankingModelAssembler.toModel(rankingDTO));
    }

    @GetMapping("/torneo/{torneoId}")
    @Operation(summary = "Listar ranking por torneo con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<RankingDTO>>> listarRankingPorTorneo(
            @PathVariable Long torneoId) {

        List<EntityModel<RankingDTO>> rankings = rankingService.listarRankingPorTorneo(torneoId)
                .stream()
                .map(rankingModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<RankingDTO>> collectionModel = CollectionModel.of(
                rankings,
                linkTo(methodOn(RankingControllerV2.class).listarRankingPorTorneo(torneoId)).withSelfRel(),
                linkTo(methodOn(RankingControllerV2.class).listarRankings()).withRel("rankings")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/torneo/{torneoId}/participante/{participanteId}")
    @Operation(summary = "Buscar posición por participante con HATEOAS")
    public ResponseEntity<EntityModel<RankingDTO>> buscarPosicionPorParticipante(
            @PathVariable Long torneoId,
            @PathVariable Long participanteId) {

        RankingDTO rankingDTO = rankingService.buscarPosicionPorParticipante(torneoId, participanteId);

        return ResponseEntity.ok(rankingModelAssembler.toModel(rankingDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ranking con HATEOAS")
    public ResponseEntity<EntityModel<RankingDTO>> actualizarRanking(
            @PathVariable Long id,
            @Valid @RequestBody RankingDTO rankingDTO) {

        RankingDTO rankingActualizado = rankingService.actualizarRanking(id, rankingDTO);

        return ResponseEntity.ok(rankingModelAssembler.toModel(rankingActualizado));
    }

    @PutMapping("/resultado/{resultadoId}")
    @Operation(summary = "Actualizar ranking por resultado con HATEOAS")
    public ResponseEntity<EntityModel<RankingDTO>> actualizarRankingPorResultado(
            @PathVariable Long resultadoId,
            @RequestParam Long torneoId,
            @RequestParam Long participanteAId,
            @RequestParam Long participanteBId) {

        RankingDTO rankingActualizado = rankingService.actualizarRankingPorResultado(
                resultadoId,
                torneoId,
                participanteAId,
                participanteBId
        );

        return ResponseEntity.ok(rankingModelAssembler.toModel(rankingActualizado));
    }

    @DeleteMapping("/torneo/{torneoId}/reiniciar")
    @Operation(summary = "Reiniciar ranking por torneo")
    public ResponseEntity<Void> reiniciarRankingPorTorneo(
            @PathVariable Long torneoId) {

        rankingService.reiniciarRankingPorTorneo(torneoId);

        return ResponseEntity.noContent().build();
    }
}
package cl.duoc.esports.resultservice.controllers;

import cl.duoc.esports.resultservice.assemblers.ResultadoModelAssembler;
import cl.duoc.esports.resultservice.dto.ResultadoDTO;
import cl.duoc.esports.resultservice.services.ResultadoService;
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
@RequestMapping("/api/v2/resultados")
@Tag(name = "Resultados V2 - HATEOAS", description = "Endpoints de resultados con respuestas HATEOAS")
public class ResultadoControllerV2 {

    @Autowired
    private ResultadoService resultadoService;

    @Autowired
    private ResultadoModelAssembler resultadoModelAssembler;

    @PostMapping
    @Operation(summary = "Crear resultado con HATEOAS")
    public ResponseEntity<EntityModel<ResultadoDTO>> crearResultado(@Valid @RequestBody ResultadoDTO resultadoDTO) {
        ResultadoDTO nuevoResultado = resultadoService.crearResultado(resultadoDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resultadoModelAssembler.toModel(nuevoResultado));
    }

    @GetMapping
    @Operation(summary = "Listar resultados con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<ResultadoDTO>>> listarResultados() {
        List<EntityModel<ResultadoDTO>> resultados = resultadoService.listarResultados()
                .stream()
                .map(resultadoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ResultadoDTO>> collectionModel = CollectionModel.of(
                resultados,
                linkTo(methodOn(ResultadoControllerV2.class).listarResultados()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar resultado por ID con HATEOAS")
    public ResponseEntity<EntityModel<ResultadoDTO>> buscarResultadoPorId(@PathVariable Long id) {
        ResultadoDTO resultadoDTO = resultadoService.buscarResultadoPorId(id);

        return ResponseEntity.ok(resultadoModelAssembler.toModel(resultadoDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar resultado con HATEOAS")
    public ResponseEntity<EntityModel<ResultadoDTO>> actualizarResultado(
            @PathVariable Long id,
            @Valid @RequestBody ResultadoDTO resultadoDTO) {

        ResultadoDTO resultadoActualizado = resultadoService.actualizarResultado(id, resultadoDTO);

        return ResponseEntity.ok(resultadoModelAssembler.toModel(resultadoActualizado));
    }

    @PutMapping("/{id}/validar")
    @Operation(summary = "Validar resultado con HATEOAS")
    public ResponseEntity<EntityModel<ResultadoDTO>> validarResultado(@PathVariable Long id) {
        ResultadoDTO resultadoValidado = resultadoService.validarResultado(id);

        return ResponseEntity.ok(resultadoModelAssembler.toModel(resultadoValidado));
    }

    @PutMapping("/{id}/anular")
    @Operation(summary = "Anular resultado")
    public ResponseEntity<Void> anularResultado(
            @PathVariable Long id,
            @RequestParam String justificacion) {

        resultadoService.anularResultado(id, justificacion);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/partida/{partidaId}")
    @Operation(summary = "Listar resultados por partida con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<ResultadoDTO>>> listarPorPartida(
            @PathVariable Long partidaId) {

        List<EntityModel<ResultadoDTO>> resultados = resultadoService.listarPorPartida(partidaId)
                .stream()
                .map(resultadoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ResultadoDTO>> collectionModel = CollectionModel.of(
                resultados,
                linkTo(methodOn(ResultadoControllerV2.class).listarPorPartida(partidaId)).withSelfRel(),
                linkTo(methodOn(ResultadoControllerV2.class).listarResultados()).withRel("resultados")
        );

        return ResponseEntity.ok(collectionModel);
    }
}
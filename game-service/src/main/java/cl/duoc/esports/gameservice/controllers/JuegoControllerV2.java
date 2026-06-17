package cl.duoc.esports.gameservice.controllers;

import cl.duoc.esports.gameservice.assemblers.JuegoModelAssembler;
import cl.duoc.esports.gameservice.dto.JuegoDTO;
import cl.duoc.esports.gameservice.services.JuegoService;
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
@RequestMapping("/api/v2/juegos")
@Tag(name = "Juegos V2 - HATEOAS", description = "Endpoints de juegos con respuestas HATEOAS")
public class JuegoControllerV2 {

    @Autowired
    private JuegoService juegoService;

    @Autowired
    private JuegoModelAssembler juegoModelAssembler;

    @PostMapping
    @Operation(summary = "Crear juego con HATEOAS")
    public ResponseEntity<EntityModel<JuegoDTO>> crearJuego(@Valid @RequestBody JuegoDTO juegoDTO) {
        JuegoDTO nuevoJuego = juegoService.crearJuego(juegoDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(juegoModelAssembler.toModel(nuevoJuego));
    }

    @GetMapping
    @Operation(summary = "Listar juegos con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<JuegoDTO>>> listarJuegos() {
        List<EntityModel<JuegoDTO>> juegos = juegoService.listarJuegos()
                .stream()
                .map(juegoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<JuegoDTO>> collectionModel = CollectionModel.of(
                juegos,
                linkTo(methodOn(JuegoControllerV2.class).listarJuegos()).withSelfRel(),
                linkTo(methodOn(JuegoControllerV2.class).listarJuegosActivos()).withRel("juegos-activos")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar juego por ID con HATEOAS")
    public ResponseEntity<EntityModel<JuegoDTO>> buscarJuegoPorId(@PathVariable Long id) {
        JuegoDTO juegoDTO = juegoService.buscarJuegoPorId(id);

        return ResponseEntity.ok(juegoModelAssembler.toModel(juegoDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar juego con HATEOAS")
    public ResponseEntity<EntityModel<JuegoDTO>> actualizarJuego(
            @PathVariable Long id,
            @Valid @RequestBody JuegoDTO juegoDTO) {

        JuegoDTO juegoActualizado = juegoService.actualizarJuego(id, juegoDTO);

        return ResponseEntity.ok(juegoModelAssembler.toModel(juegoActualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar juego")
    public ResponseEntity<Void> desactivarJuego(@PathVariable Long id) {
        juegoService.desactivarJuego(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar juegos activos con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<JuegoDTO>>> listarJuegosActivos() {
        List<EntityModel<JuegoDTO>> juegos = juegoService.listarJuegosActivos()
                .stream()
                .map(juegoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<JuegoDTO>> collectionModel = CollectionModel.of(
                juegos,
                linkTo(methodOn(JuegoControllerV2.class).listarJuegosActivos()).withSelfRel(),
                linkTo(methodOn(JuegoControllerV2.class).listarJuegos()).withRel("juegos")
        );

        return ResponseEntity.ok(collectionModel);
    }
}
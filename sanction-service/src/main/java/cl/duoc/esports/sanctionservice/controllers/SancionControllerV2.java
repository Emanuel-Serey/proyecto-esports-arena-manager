package cl.duoc.esports.sanctionservice.controllers;

import cl.duoc.esports.sanctionservice.assemblers.SancionModelAssembler;
import cl.duoc.esports.sanctionservice.dto.SancionDTO;
import cl.duoc.esports.sanctionservice.services.SancionService;
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
@RequestMapping("/api/v2/sanciones")
@Tag(name = "Sanciones V2 - HATEOAS", description = "Endpoints de sanciones con respuestas HATEOAS")
public class SancionControllerV2 {

    @Autowired
    private SancionService sancionService;

    @Autowired
    private SancionModelAssembler sancionModelAssembler;

    @PostMapping
    @Operation(summary = "Crear sanción con HATEOAS")
    public ResponseEntity<EntityModel<SancionDTO>> crearSancion(@Valid @RequestBody SancionDTO sancionDTO) {
        SancionDTO nuevaSancion = sancionService.crearSancion(sancionDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(sancionModelAssembler.toModel(nuevaSancion));
    }

    @GetMapping
    @Operation(summary = "Listar sanciones con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<SancionDTO>>> listarSanciones() {
        List<EntityModel<SancionDTO>> sanciones = sancionService.listarSanciones()
                .stream()
                .map(sancionModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<SancionDTO>> collectionModel = CollectionModel.of(
                sanciones,
                linkTo(methodOn(SancionControllerV2.class).listarSanciones()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sanción por ID con HATEOAS")
    public ResponseEntity<EntityModel<SancionDTO>> buscarSancionPorId(@PathVariable Long id) {
        SancionDTO sancionDTO = sancionService.buscarSancionPorId(id);

        return ResponseEntity.ok(sancionModelAssembler.toModel(sancionDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar sanción con HATEOAS")
    public ResponseEntity<EntityModel<SancionDTO>> actualizarSancion(
            @PathVariable Long id,
            @Valid @RequestBody SancionDTO sancionDTO) {

        SancionDTO sancionActualizada = sancionService.actualizarSancion(id, sancionDTO);

        return ResponseEntity.ok(sancionModelAssembler.toModel(sancionActualizada));
    }

    @PutMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar sanción")
    public ResponseEntity<Void> cerrarSancion(@PathVariable Long id) {
        sancionService.cerrarSancion(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar sanciones por usuario con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<SancionDTO>>> listarPorUsuario(
            @PathVariable Long usuarioId) {

        List<EntityModel<SancionDTO>> sanciones = sancionService.listarPorUsuario(usuarioId)
                .stream()
                .map(sancionModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<SancionDTO>> collectionModel = CollectionModel.of(
                sanciones,
                linkTo(methodOn(SancionControllerV2.class).listarPorUsuario(usuarioId)).withSelfRel(),
                linkTo(methodOn(SancionControllerV2.class).listarSanciones()).withRel("sanciones"),
                linkTo(methodOn(SancionControllerV2.class).existeSancionActivaUsuario(usuarioId)).withRel("sancion-activa-usuario")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/equipo/{equipoId}")
    @Operation(summary = "Listar sanciones por equipo con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<SancionDTO>>> listarPorEquipo(
            @PathVariable Long equipoId) {

        List<EntityModel<SancionDTO>> sanciones = sancionService.listarPorEquipo(equipoId)
                .stream()
                .map(sancionModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<SancionDTO>> collectionModel = CollectionModel.of(
                sanciones,
                linkTo(methodOn(SancionControllerV2.class).listarPorEquipo(equipoId)).withSelfRel(),
                linkTo(methodOn(SancionControllerV2.class).listarSanciones()).withRel("sanciones"),
                linkTo(methodOn(SancionControllerV2.class).existeSancionActivaEquipo(equipoId)).withRel("sancion-activa-equipo")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar sanciones por estado con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<SancionDTO>>> listarPorEstado(
            @PathVariable String estado) {

        List<EntityModel<SancionDTO>> sanciones = sancionService.listarPorEstado(estado)
                .stream()
                .map(sancionModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<SancionDTO>> collectionModel = CollectionModel.of(
                sanciones,
                linkTo(methodOn(SancionControllerV2.class).listarPorEstado(estado)).withSelfRel(),
                linkTo(methodOn(SancionControllerV2.class).listarSanciones()).withRel("sanciones")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/usuario/{usuarioId}/activa")
    @Operation(summary = "Verificar sanción activa de usuario con HATEOAS")
    public ResponseEntity<EntityModel<Boolean>> existeSancionActivaUsuario(
            @PathVariable Long usuarioId) {

        Boolean existe = sancionService.existeSancionActivaUsuario(usuarioId);

        EntityModel<Boolean> model = EntityModel.of(
                existe,
                linkTo(methodOn(SancionControllerV2.class).existeSancionActivaUsuario(usuarioId)).withSelfRel(),
                linkTo(methodOn(SancionControllerV2.class).listarPorUsuario(usuarioId)).withRel("sanciones-por-usuario")
        );

        return ResponseEntity.ok(model);
    }

    @GetMapping("/equipo/{equipoId}/activa")
    @Operation(summary = "Verificar sanción activa de equipo con HATEOAS")
    public ResponseEntity<EntityModel<Boolean>> existeSancionActivaEquipo(
            @PathVariable Long equipoId) {

        Boolean existe = sancionService.existeSancionActivaEquipo(equipoId);

        EntityModel<Boolean> model = EntityModel.of(
                existe,
                linkTo(methodOn(SancionControllerV2.class).existeSancionActivaEquipo(equipoId)).withSelfRel(),
                linkTo(methodOn(SancionControllerV2.class).listarPorEquipo(equipoId)).withRel("sanciones-por-equipo")
        );

        return ResponseEntity.ok(model);
    }
}
package cl.duoc.esports.teamservice.controllers;

import cl.duoc.esports.teamservice.assemblers.EquipoModelAssembler;
import cl.duoc.esports.teamservice.assemblers.MiembroEquipoModelAssembler;
import cl.duoc.esports.teamservice.dto.EquipoDTO;
import cl.duoc.esports.teamservice.dto.MiembroEquipoDTO;
import cl.duoc.esports.teamservice.services.EquipoService;
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
@RequestMapping("/api/v2/equipos")
@Tag(name = "Equipos V2 - HATEOAS", description = "Endpoints de equipos con respuestas HATEOAS")
public class EquipoControllerV2 {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private EquipoModelAssembler equipoModelAssembler;

    @Autowired
    private MiembroEquipoModelAssembler miembroEquipoModelAssembler;

    @PostMapping
    @Operation(summary = "Crear equipo con HATEOAS")
    public ResponseEntity<EntityModel<EquipoDTO>> crearEquipo(@Valid @RequestBody EquipoDTO equipoDTO) {
        EquipoDTO nuevoEquipo = equipoService.crearEquipo(equipoDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(equipoModelAssembler.toModel(nuevoEquipo));
    }

    @GetMapping
    @Operation(summary = "Listar equipos con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<EquipoDTO>>> listarEquipos() {
        List<EntityModel<EquipoDTO>> equipos = equipoService.listarEquipos()
                .stream()
                .map(equipoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<EquipoDTO>> collectionModel = CollectionModel.of(
                equipos,
                linkTo(methodOn(EquipoControllerV2.class).listarEquipos()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar equipo por ID con HATEOAS")
    public ResponseEntity<EntityModel<EquipoDTO>> buscarEquipoPorId(@PathVariable Long id) {
        EquipoDTO equipoDTO = equipoService.buscarEquipoPorId(id);

        return ResponseEntity.ok(equipoModelAssembler.toModel(equipoDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar equipo con HATEOAS")
    public ResponseEntity<EntityModel<EquipoDTO>> actualizarEquipo(
            @PathVariable Long id,
            @Valid @RequestBody EquipoDTO equipoDTO) {

        EquipoDTO equipoActualizado = equipoService.actualizarEquipo(id, equipoDTO);

        return ResponseEntity.ok(equipoModelAssembler.toModel(equipoActualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar equipo")
    public ResponseEntity<Void> desactivarEquipo(@PathVariable Long id) {
        equipoService.desactivarEquipo(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/miembros")
    @Operation(summary = "Agregar miembro a equipo con HATEOAS")
    public ResponseEntity<EntityModel<MiembroEquipoDTO>> agregarMiembro(
            @PathVariable Long id,
            @Valid @RequestBody MiembroEquipoDTO miembroDTO) {

        MiembroEquipoDTO nuevoMiembro = equipoService.agregarMiembro(id, miembroDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(miembroEquipoModelAssembler.toModel(nuevoMiembro, id));
    }

    @DeleteMapping("/{id}/miembros/{miembroId}")
    @Operation(summary = "Eliminar miembro de equipo")
    public ResponseEntity<Void> eliminarMiembro(
            @PathVariable Long id,
            @PathVariable Long miembroId) {

        equipoService.eliminarMiembro(id, miembroId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{equipoId}/miembros")
    @Operation(summary = "Listar miembros de equipo con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<MiembroEquipoDTO>>> listarMiembros(
            @PathVariable Long equipoId) {

        List<EntityModel<MiembroEquipoDTO>> miembros = equipoService.listarMiembros(equipoId)
                .stream()
                .map(miembro -> miembroEquipoModelAssembler.toModel(miembro, equipoId))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<MiembroEquipoDTO>> collectionModel = CollectionModel.of(
                miembros,
                linkTo(methodOn(EquipoControllerV2.class).listarMiembros(equipoId)).withSelfRel(),
                linkTo(methodOn(EquipoControllerV2.class).buscarEquipoPorId(equipoId)).withRel("equipo")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/juego/{juegoPrincipalId}")
    @Operation(summary = "Listar equipos por juego principal con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<EquipoDTO>>> listarPorJuego(
            @PathVariable Long juegoPrincipalId) {

        List<EntityModel<EquipoDTO>> equipos = equipoService.listarPorJuegoPrincipal(juegoPrincipalId)
                .stream()
                .map(equipoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<EquipoDTO>> collectionModel = CollectionModel.of(
                equipos,
                linkTo(methodOn(EquipoControllerV2.class).listarPorJuego(juegoPrincipalId)).withSelfRel(),
                linkTo(methodOn(EquipoControllerV2.class).listarEquipos()).withRel("equipos")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/capitan/{capitanId}")
    @Operation(summary = "Listar equipos por capitán con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<EquipoDTO>>> listarPorCapitan(
            @PathVariable Long capitanId) {

        List<EntityModel<EquipoDTO>> equipos = equipoService.listarPorCapitan(capitanId)
                .stream()
                .map(equipoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<EquipoDTO>> collectionModel = CollectionModel.of(
                equipos,
                linkTo(methodOn(EquipoControllerV2.class).listarPorCapitan(capitanId)).withSelfRel(),
                linkTo(methodOn(EquipoControllerV2.class).listarEquipos()).withRel("equipos")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar equipos por estado con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<EquipoDTO>>> listarPorEstado(
            @PathVariable String estado) {

        List<EntityModel<EquipoDTO>> equipos = equipoService.listarPorEstado(estado)
                .stream()
                .map(equipoModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<EquipoDTO>> collectionModel = CollectionModel.of(
                equipos,
                linkTo(methodOn(EquipoControllerV2.class).listarPorEstado(estado)).withSelfRel(),
                linkTo(methodOn(EquipoControllerV2.class).listarEquipos()).withRel("equipos")
        );

        return ResponseEntity.ok(collectionModel);
    }
}
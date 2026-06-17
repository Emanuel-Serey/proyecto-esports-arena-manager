package cl.duoc.esports.userservice.controllers;

import cl.duoc.esports.userservice.assemblers.UsuarioModelAssembler;
import cl.duoc.esports.userservice.dto.UsuarioDTO;
import cl.duoc.esports.userservice.services.UsuarioService;
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
@RequestMapping("/api/v2/usuarios")
@Tag(name = "Usuarios V2 - HATEOAS", description = "Endpoints de usuarios con respuestas HATEOAS")
public class UsuarioControllerV2 {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioModelAssembler usuarioModelAssembler;

    @PostMapping
    @Operation(summary = "Crear usuario con HATEOAS")
    public ResponseEntity<EntityModel<UsuarioDTO>> crearUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO nuevoUsuario = usuarioService.crearUsuario(usuarioDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioModelAssembler.toModel(nuevoUsuario));
    }

    @GetMapping
    @Operation(summary = "Listar usuarios con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<UsuarioDTO>>> listarUsuarios() {
        List<EntityModel<UsuarioDTO>> usuarios = usuarioService.listarUsuarios()
                .stream()
                .map(usuarioModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<UsuarioDTO>> collectionModel = CollectionModel.of(
                usuarios,
                linkTo(methodOn(UsuarioControllerV2.class).listarUsuarios()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por ID con HATEOAS")
    public ResponseEntity<EntityModel<UsuarioDTO>> buscarUsuarioPorId(@PathVariable Long id) {
        UsuarioDTO usuarioDTO = usuarioService.buscarUsuarioPorId(id);

        return ResponseEntity.ok(usuarioModelAssembler.toModel(usuarioDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario con HATEOAS")
    public ResponseEntity<EntityModel<UsuarioDTO>> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioDTO usuarioDTO) {

        UsuarioDTO usuarioActualizado = usuarioService.actualizarUsuario(id, usuarioDTO);

        return ResponseEntity.ok(usuarioModelAssembler.toModel(usuarioActualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar usuario")
    public ResponseEntity<Void> desactivarUsuario(@PathVariable Long id) {
        usuarioService.desactivarUsuario(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rol/{rol}")
    @Operation(summary = "Listar usuarios por rol con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<UsuarioDTO>>> listarPorRol(@PathVariable String rol) {
        List<EntityModel<UsuarioDTO>> usuarios = usuarioService.listarPorRol(rol)
                .stream()
                .map(usuarioModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<UsuarioDTO>> collectionModel = CollectionModel.of(
                usuarios,
                linkTo(methodOn(UsuarioControllerV2.class).listarPorRol(rol)).withSelfRel(),
                linkTo(methodOn(UsuarioControllerV2.class).listarUsuarios()).withRel("usuarios")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar usuarios por estado con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<UsuarioDTO>>> listarPorEstado(@PathVariable String estado) {
        List<EntityModel<UsuarioDTO>> usuarios = usuarioService.listarPorEstado(estado)
                .stream()
                .map(usuarioModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<UsuarioDTO>> collectionModel = CollectionModel.of(
                usuarios,
                linkTo(methodOn(UsuarioControllerV2.class).listarPorEstado(estado)).withSelfRel(),
                linkTo(methodOn(UsuarioControllerV2.class).listarUsuarios()).withRel("usuarios")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/nickname/{nickname}")
    @Operation(summary = "Buscar usuarios por nickname con HATEOAS")
    public ResponseEntity<CollectionModel<EntityModel<UsuarioDTO>>> buscarPorNickname(@PathVariable String nickname) {
        List<EntityModel<UsuarioDTO>> usuarios = usuarioService.buscarPorNickname(nickname)
                .stream()
                .map(usuarioModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<UsuarioDTO>> collectionModel = CollectionModel.of(
                usuarios,
                linkTo(methodOn(UsuarioControllerV2.class).buscarPorNickname(nickname)).withSelfRel(),
                linkTo(methodOn(UsuarioControllerV2.class).listarUsuarios()).withRel("usuarios")
        );

        return ResponseEntity.ok(collectionModel);
    }
}
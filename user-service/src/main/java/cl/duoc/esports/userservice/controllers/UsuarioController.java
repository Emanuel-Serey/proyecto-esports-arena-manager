package cl.duoc.esports.userservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.duoc.esports.userservice.dto.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import cl.duoc.esports.userservice.dto.UsuarioDTO;
import cl.duoc.esports.userservice.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Operaciones relacionadas con la gestión de usuarios del sistema")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Crear usuario", description = "Registra un nuevo usuario en el sistema eSports Arena Manager")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"nombre\":\"El nombre es obligatorio\",\"email\":\"El email debe ser válido\"}"))),
            @ApiResponse(responseCode = "409", description = "Nickname o email ya registrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El nickname ya está registrado\"}")))
    })
    public ResponseEntity<UsuarioDTO> crearUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO nuevoUsuario = usuarioService.crearUsuario(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    @GetMapping
    @Operation(
            summary = "Listar usuarios",
            description = "Obtiene el listado completo de usuarios registrados en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de usuarios obtenido correctamente")
    })
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por ID", description = "Obtiene la información de un usuario específico mediante su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Usuario no encontrado\"}")))
    })
    public ResponseEntity<UsuarioDTO> buscarUsuarioPorId(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(usuarioService.buscarUsuarioPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"nombre\":\"El nombre es obligatorio\",\"email\":\"El email debe ser válido\"}"))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Usuario no encontrado\"}"))),
            @ApiResponse(responseCode = "409", description = "Nickname o email ya registrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El email ya está registrado\"}")))
    })
    public ResponseEntity<UsuarioDTO> actualizarUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UsuarioDTO usuarioDTO) {

        UsuarioDTO usuarioActualizado = usuarioService.actualizarUsuario(id, usuarioDTO);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar usuario", description = "Desactiva lógicamente un usuario del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario desactivado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Usuario no encontrado\"}")))
    })
    public ResponseEntity<Void> desactivarUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id) {

        usuarioService.desactivarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rol/{rol}")
    @Operation(
            summary = "Listar usuarios por rol",
            description = "Obtiene usuarios filtrados por rol"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuarios filtrados por rol correctamente")
    })
    public ResponseEntity<List<UsuarioDTO>> listarPorRol(
            @Parameter(description = "Rol del usuario", example = "ADMIN")
            @PathVariable String rol) {

        return ResponseEntity.ok(usuarioService.listarPorRol(rol));
    }

    @GetMapping("/estado/{estado}")
    @Operation(
            summary = "Listar usuarios por estado",
            description = "Obtiene usuarios filtrados por estado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuarios filtrados por estado correctamente")
    })
    public ResponseEntity<List<UsuarioDTO>> listarPorEstado(
            @Parameter(description = "Estado del usuario", example = "ACTIVO")
            @PathVariable String estado) {

        return ResponseEntity.ok(usuarioService.listarPorEstado(estado));
    }

    @GetMapping("/nickname/{nickname}")
    @Operation(
            summary = "Buscar usuarios por nickname",
            description = "Obtiene usuarios que coincidan con el nickname indicado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente")
    })
    public ResponseEntity<List<UsuarioDTO>> buscarPorNickname(
            @Parameter(description = "Nickname del usuario", example = "gamer123")
            @PathVariable String nickname) {

        return ResponseEntity.ok(usuarioService.buscarPorNickname(nickname));
    }
}

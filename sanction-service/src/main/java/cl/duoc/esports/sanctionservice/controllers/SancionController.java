package cl.duoc.esports.sanctionservice.controllers;

import cl.duoc.esports.sanctionservice.dto.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.duoc.esports.sanctionservice.dto.SancionDTO;
import cl.duoc.esports.sanctionservice.services.SancionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sanciones")
@Tag(name = "Sanciones", description = "Operaciones relacionadas con la gestión de sanciones")
public class SancionController {

    @Autowired
    private SancionService sancionService;

    @PostMapping
    @Operation(summary = "Crear sanción", description = "Registra una nueva sanción para un usuario o equipo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sanción creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"motivo\":\"El motivo de la sanción es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Usuario o equipo no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El usuario no existe\"}"))),
            @ApiResponse(responseCode = "409", description = "Usuario o equipo inactivo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El usuario no está activo\"}"))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"La fecha de fin no puede ser anterior a la fecha de inicio\"}"))),
            @ApiResponse(responseCode = "503", description = "Servicio externo no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar el usuario desde user-service\"}")))
    })
    public ResponseEntity<SancionDTO> crearSancion(@Valid @RequestBody SancionDTO sancionDTO) {
        SancionDTO nuevaSancion = sancionService.crearSancion(sancionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaSancion);
    }

    @GetMapping
    @Operation(summary = "Listar sanciones", description = "Obtiene el listado completo de sanciones registradas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de sanciones obtenido correctamente")
    })
    public ResponseEntity<List<SancionDTO>> listarSanciones() {
        return ResponseEntity.ok(sancionService.listarSanciones());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sanción por ID", description = "Obtiene una sanción específica mediante su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sanción encontrada correctamente"),
            @ApiResponse(responseCode = "404", description = "Sanción no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Sanción no encontrada\"}")))
    })
    public ResponseEntity<SancionDTO> buscarSancionPorId(
            @Parameter(description = "ID de la sanción", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(sancionService.buscarSancionPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar sanción", description = "Actualiza los datos de una sanción existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sanción actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"motivo\":\"El motivo de la sanción es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Sanción, usuario o equipo no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Sanción no encontrada\"}"))),
            @ApiResponse(responseCode = "409", description = "Usuario o equipo inactivo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El equipo no está activo\"}"))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Una sanción no puede tener usuarioId y equipoId al mismo tiempo\"}"))),
            @ApiResponse(responseCode = "503", description = "Servicio externo no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar el equipo desde team-service\"}")))
    })
    public ResponseEntity<SancionDTO> actualizarSancion(
            @PathVariable Long id,
            @Valid @RequestBody SancionDTO sancionDTO) {

        SancionDTO sancionActualizada = sancionService.actualizarSancion(id, sancionDTO);
        return ResponseEntity.ok(sancionActualizada);
    }

    @PutMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar sanción", description = "Cambia el estado de una sanción a CERRADA")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sanción cerrada correctamente"),
            @ApiResponse(responseCode = "404", description = "Sanción no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Sanción no encontrada\"}")))
    })
    public ResponseEntity<Void> cerrarSancion(
            @Parameter(description = "ID de la sanción", example = "1")
            @PathVariable Long id) {

        sancionService.cerrarSancion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar sanciones por usuario", description = "Obtiene las sanciones asociadas a un usuario específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sanciones filtradas por usuario correctamente")
    })
    public ResponseEntity<List<SancionDTO>> listarPorUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long usuarioId) {

        return ResponseEntity.ok(sancionService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/equipo/{equipoId}")
    @Operation(summary = "Listar sanciones por equipo", description = "Obtiene las sanciones asociadas a un equipo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sanciones filtradas por equipo correctamente")
    })
    public ResponseEntity<List<SancionDTO>> listarPorEquipo(
            @Parameter(description = "ID del equipo", example = "1")
            @PathVariable Long equipoId) {

        return ResponseEntity.ok(sancionService.listarPorEquipo(equipoId));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar sanciones por estado", description = "Obtiene sanciones filtradas por estado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sanciones filtradas por estado correctamente")
    })
    public ResponseEntity<List<SancionDTO>> listarPorEstado(
            @Parameter(description = "Estado de la sanción", example = "ACTIVA")
            @PathVariable String estado) {

        return ResponseEntity.ok(sancionService.listarPorEstado(estado));
    }

    @GetMapping("/usuario/{usuarioId}/activa")
    @Operation(summary = "Verificar sanción activa de usuario", description = "Indica si un usuario posee una sanción activa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificación realizada correctamente")
    })
    public ResponseEntity<Boolean> existeSancionActivaUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long usuarioId) {

        return ResponseEntity.ok(sancionService.existeSancionActivaUsuario(usuarioId));
    }

    @GetMapping("/equipo/{equipoId}/activa")
    @Operation(summary = "Verificar sanción activa de equipo", description = "Indica si un equipo posee una sanción activa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificación realizada correctamente")
    })
    public ResponseEntity<Boolean> existeSancionActivaEquipo(
            @Parameter(description = "ID del equipo", example = "1")
            @PathVariable Long equipoId) {

        return ResponseEntity.ok(sancionService.existeSancionActivaEquipo(equipoId));
    }
}
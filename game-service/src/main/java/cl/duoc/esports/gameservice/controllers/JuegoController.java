package cl.duoc.esports.gameservice.controllers;

import cl.duoc.esports.gameservice.dto.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.duoc.esports.gameservice.dto.JuegoDTO;
import cl.duoc.esports.gameservice.services.JuegoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/juegos")
@Tag(name = "Juegos", description = "Operaciones relacionadas con la gestión de juegos")
public class JuegoController {

    @Autowired
    private JuegoService juegoService;

    @PostMapping
    @Operation(summary = "Crear juego", description = "Registra un nuevo juego en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Juego creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"nombre\":\"El nombre del juego es obligatorio\"}"))),
            @ApiResponse(responseCode = "409", description = "El juego ya existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El juego ya está registrado\"}")))
    })
    public ResponseEntity<JuegoDTO> crearJuego(@Valid @RequestBody JuegoDTO juegoDTO) {
        JuegoDTO nuevoJuego = juegoService.crearJuego(juegoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoJuego);
    }

    @GetMapping
    @Operation(summary = "Listar juegos", description = "Obtiene el listado completo de juegos registrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de juegos obtenido correctamente")
    })
    public ResponseEntity<List<JuegoDTO>> listarJuegos() {
        return ResponseEntity.ok(juegoService.listarJuegos());
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar juegos activos", description = "Obtiene el listado de juegos que se encuentran activos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de juegos activos obtenido correctamente")
    })
    public ResponseEntity<List<JuegoDTO>> listarJuegosActivos() {
        return ResponseEntity.ok(juegoService.listarJuegosActivos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar juego por ID", description = "Obtiene la información de un juego específico mediante su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Juego encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Juego no encontrado\"}")))
    })
    public ResponseEntity<JuegoDTO> buscarJuegoPorId(
            @Parameter(description = "ID del juego", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(juegoService.buscarJuegoPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar juego", description = "Actualiza los datos de un juego existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Juego actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"nombre\":\"El nombre del juego es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Juego no encontrado\"}"))),
            @ApiResponse(responseCode = "409", description = "El juego ya existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El juego ya está registrado\"}")))
    })
    public ResponseEntity<JuegoDTO> actualizarJuego(
            @Parameter(description = "ID del juego", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody JuegoDTO juegoDTO) {

        JuegoDTO juegoActualizado = juegoService.actualizarJuego(id, juegoDTO);
        return ResponseEntity.ok(juegoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar juego", description = "Desactiva lógicamente un juego del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Juego desactivado correctamente"),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Juego no encontrado\"}")))
    })
    public ResponseEntity<Void> desactivarJuego(
            @Parameter(description = "ID del juego", example = "1")
            @PathVariable Long id) {

        juegoService.desactivarJuego(id);
        return ResponseEntity.noContent().build();
    }
}

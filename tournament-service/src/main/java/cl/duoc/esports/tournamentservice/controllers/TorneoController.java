package cl.duoc.esports.tournamentservice.controllers;

import cl.duoc.esports.tournamentservice.dto.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.duoc.esports.tournamentservice.dto.TorneoDTO;
import cl.duoc.esports.tournamentservice.services.TorneoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/torneos")
@Tag(name = "Torneos", description = "Operaciones relacionadas con la gestión de torneos")
public class TorneoController {

    @Autowired
    private TorneoService torneoService;

    @PostMapping
    @Operation(summary = "Crear torneo", description = "Registra un nuevo torneo asociado a un juego existente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Torneo creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"nombre\":\"El nombre del torneo es obligatorio\",\"juegoId\":\"El ID del juego es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El juego no existe\"}"))),
            @ApiResponse(responseCode = "409", description = "El juego no está activo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El juego no está activo\"}"))),
            @ApiResponse(responseCode = "422", description = "Fechas inválidas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"La fecha de fin no puede ser anterior a la fecha de inicio\"}"))),
            @ApiResponse(responseCode = "503", description = "No se pudo validar el juego desde game-service",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar el juego desde game-service\"}")))
    })
    public ResponseEntity<TorneoDTO> crearTorneo(@Valid @RequestBody TorneoDTO torneoDTO) {
        TorneoDTO nuevoTorneo = torneoService.crearTorneo(torneoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTorneo);
    }

    @GetMapping
    @Operation(summary = "Listar torneos", description = "Obtiene el listado completo de torneos registrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de torneos obtenido correctamente")
    })
    public ResponseEntity<List<TorneoDTO>> listarTorneos() {
        return ResponseEntity.ok(torneoService.listarTorneos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar torneo por ID", description = "Obtiene la información de un torneo específico mediante su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Torneo encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Torneo no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Torneo no encontrado\"}")))
    })
    public ResponseEntity<TorneoDTO> buscarTorneoPorId(
            @Parameter(description = "ID del torneo", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(torneoService.buscarTorneoPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar torneo", description = "Actualiza los datos de un torneo existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Torneo actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"nombre\":\"El nombre del torneo es obligatorio\",\"juegoId\":\"El ID del juego es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Torneo o juego no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Torneo no encontrado\"}"))),
            @ApiResponse(responseCode = "409", description = "El juego no está activo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El juego no está activo\"}"))),
            @ApiResponse(responseCode = "422", description = "Fechas inválidas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"La fecha de fin no puede ser anterior a la fecha de inicio\"}"))),
            @ApiResponse(responseCode = "503", description = "No se pudo validar el juego desde game-service",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar el juego desde game-service\"}")))
    })
    public ResponseEntity<TorneoDTO> actualizarTorneo(
            @Parameter(description = "ID del torneo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody TorneoDTO torneoDTO) {

        TorneoDTO torneoActualizado = torneoService.actualizarTorneo(id, torneoDTO);
        return ResponseEntity.ok(torneoActualizado);
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar torneo", description = "Cambia el estado de un torneo a CANCELADO")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Torneo cancelado correctamente"),
            @ApiResponse(responseCode = "404", description = "Torneo no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Torneo no encontrado\"}")))
    })
    public ResponseEntity<Void> cancelarTorneo(
            @Parameter(description = "ID del torneo", example = "1")
            @PathVariable Long id) {

        torneoService.cancelarTorneo(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar torneo", description = "Cambia el estado de un torneo a CERRADO")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Torneo cerrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Torneo no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Torneo no encontrado\"}")))
    })
    public ResponseEntity<Void> cerrarTorneo(
            @Parameter(description = "ID del torneo", example = "1")
            @PathVariable Long id) {

        torneoService.cerrarTorneo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/juego/{juegoId}")
    @Operation(summary = "Listar torneos por juego", description = "Obtiene torneos asociados a un juego específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Torneos filtrados por juego correctamente")
    })
    public ResponseEntity<List<TorneoDTO>> listarPorJuego(
            @Parameter(description = "ID del juego", example = "1")
            @PathVariable Long juegoId) {

        return ResponseEntity.ok(torneoService.listarPorJuego(juegoId));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar torneos por estado", description = "Obtiene torneos filtrados por estado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Torneos filtrados por estado correctamente")
    })
    public ResponseEntity<List<TorneoDTO>> listarPorEstado(
            @Parameter(description = "Estado del torneo", example = "ABIERTO")
            @PathVariable String estado) {

        return ResponseEntity.ok(torneoService.listarPorEstado(estado));
    }

    @GetMapping("/fecha/{fecha}")
    @Operation(summary = "Listar torneos por fecha", description = "Obtiene torneos filtrados por fecha de inicio")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Torneos filtrados por fecha correctamente")
    })
    public ResponseEntity<List<TorneoDTO>> listarPorFecha(
            @Parameter(description = "Fecha de inicio del torneo", example = "2026-06-16")
            @PathVariable LocalDate fecha) {

        return ResponseEntity.ok(torneoService.listarPorFecha(fecha));
    }
}
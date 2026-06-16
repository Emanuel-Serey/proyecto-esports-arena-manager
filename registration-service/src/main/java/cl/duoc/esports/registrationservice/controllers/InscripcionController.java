package cl.duoc.esports.registrationservice.controllers;

import cl.duoc.esports.registrationservice.dto.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.duoc.esports.registrationservice.dto.InscripcionDTO;
import cl.duoc.esports.registrationservice.services.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
@Tag(name = "Inscripciones", description = "Operaciones relacionadas con la gestión de inscripciones")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @PostMapping
    @Operation(summary = "Crear inscripción", description = "Registra una nueva inscripción validando torneo, participante, cupos y sanciones")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inscripción creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"tipoParticipante\":\"El tipo de participante es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Torneo, equipo o jugador no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El torneo no existe\"}"))),
            @ApiResponse(responseCode = "409", description = "Conflicto con torneo, cupos, sanción o inscripción duplicada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No quedan cupos disponibles para este torneo\"}"))),
            @ApiResponse(responseCode = "503", description = "Servicio externo no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar el torneo desde tournament-service\"}")))
    })
    public ResponseEntity<InscripcionDTO> crearInscripcion(@Valid @RequestBody InscripcionDTO inscripcionDTO) {
        InscripcionDTO nuevaInscripcion = inscripcionService.crearInscripcion(inscripcionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaInscripcion);
    }

    @GetMapping
    @Operation(summary = "Listar inscripciones", description = "Obtiene el listado completo de inscripciones registradas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de inscripciones obtenido correctamente")
    })
    public ResponseEntity<List<InscripcionDTO>> listarInscripciones() {
        return ResponseEntity.ok(inscripcionService.listarInscripciones());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar inscripción por ID", description = "Obtiene una inscripción específica mediante su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripción encontrada correctamente"),
            @ApiResponse(responseCode = "404", description = "Inscripción no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Inscripción no encontrada\"}")))
    })
    public ResponseEntity<InscripcionDTO> buscarInscripcionPorId(
            @Parameter(description = "ID de la inscripción", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(inscripcionService.buscarInscripcionPorId(id));
    }

    @PutMapping("/{id}/estado/{estado}")
    @Operation(summary = "Actualizar estado de inscripción", description = "Actualiza el estado de una inscripción existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de inscripción actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Inscripción no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Inscripción no encontrada\"}")))
    })
    public ResponseEntity<InscripcionDTO> actualizarEstado(
            @Parameter(description = "ID de la inscripción", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado de la inscripción", example = "ACEPTADA")
            @PathVariable String estado) {

        InscripcionDTO inscripcionActualizada = inscripcionService.actualizarEstado(id, estado);
        return ResponseEntity.ok(inscripcionActualizada);
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar inscripción", description = "Cambia el estado de una inscripción a CANCELADA")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Inscripción cancelada correctamente"),
            @ApiResponse(responseCode = "404", description = "Inscripción no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Inscripción no encontrada\"}")))
    })
    public ResponseEntity<Void> cancelarInscripcion(
            @Parameter(description = "ID de la inscripción", example = "1")
            @PathVariable Long id) {

        inscripcionService.cancelarInscripcion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/torneo/{torneoId}")
    @Operation(summary = "Listar inscripciones por torneo", description = "Obtiene las inscripciones asociadas a un torneo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripciones filtradas por torneo correctamente")
    })
    public ResponseEntity<List<InscripcionDTO>> listarPorTorneo(
            @Parameter(description = "ID del torneo", example = "1")
            @PathVariable Long torneoId) {

        return ResponseEntity.ok(inscripcionService.listarPorTorneo(torneoId));
    }

    @GetMapping("/equipo/{equipoId}")
    @Operation(summary = "Listar inscripciones por equipo", description = "Obtiene las inscripciones asociadas a un equipo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripciones filtradas por equipo correctamente")
    })
    public ResponseEntity<List<InscripcionDTO>> listarPorEquipo(
            @Parameter(description = "ID del equipo", example = "1")
            @PathVariable Long equipoId) {

        return ResponseEntity.ok(inscripcionService.listarPorEquipo(equipoId));
    }

    @GetMapping("/jugador/{jugadorId}")
    @Operation(summary = "Listar inscripciones por jugador", description = "Obtiene las inscripciones asociadas a un jugador específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripciones filtradas por jugador correctamente")
    })
    public ResponseEntity<List<InscripcionDTO>> listarPorJugador(
            @Parameter(description = "ID del jugador", example = "1")
            @PathVariable Long jugadorId) {

        return ResponseEntity.ok(inscripcionService.listarPorJugador(jugadorId));
    }
}
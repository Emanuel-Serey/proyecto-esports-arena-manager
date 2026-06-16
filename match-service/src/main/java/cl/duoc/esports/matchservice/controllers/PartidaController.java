package cl.duoc.esports.matchservice.controllers;

import cl.duoc.esports.matchservice.dto.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.duoc.esports.matchservice.dto.PartidaDTO;
import cl.duoc.esports.matchservice.services.PartidaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partidas")
@Tag(name = "Partidas", description = "Operaciones relacionadas con la gestión de partidas")
public class PartidaController {

    @Autowired
    private PartidaService partidaService;

    @PostMapping
    @Operation(summary = "Crear partida", description = "Registra una nueva partida validando torneo y participantes inscritos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Partida creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"torneoId\":\"El torneo es obligatorio\",\"participanteAId\":\"El participante A es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Torneo o participante no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El torneo no existe\"}"))),
            @ApiResponse(responseCode = "409", description = "Conflicto con torneo, inscripción o partida duplicada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Ya existe una partida para estos participantes en esta ronda\"}"))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Los participantes no pueden ser iguales\"}"))),
            @ApiResponse(responseCode = "503", description = "Servicio externo no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar el torneo desde tournament-service\"}")))
    })
    public ResponseEntity<PartidaDTO> crearPartida(@Valid @RequestBody PartidaDTO partidaDTO) {
        PartidaDTO nuevaPartida = partidaService.crearPartida(partidaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPartida);
    }

    @GetMapping
    @Operation(summary = "Listar partidas", description = "Obtiene el listado completo de partidas registradas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de partidas obtenido correctamente")
    })
    public ResponseEntity<List<PartidaDTO>> listarPartidas() {
        return ResponseEntity.ok(partidaService.listarPartidas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar partida por ID", description = "Obtiene una partida específica mediante su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partida encontrada correctamente"),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Partida no encontrada\"}")))
    })
    public ResponseEntity<PartidaDTO> buscarPartidaPorId(
            @Parameter(description = "ID de la partida", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(partidaService.buscarPartidaPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar partida", description = "Actualiza los datos de una partida existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partida actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"torneoId\":\"El torneo es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Partida, torneo o participante no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Partida no encontrada\"}"))),
            @ApiResponse(responseCode = "409", description = "Conflicto con torneo, inscripción o partida duplicada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se puede crear una partida en un torneo cerrado\"}"))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Los participantes no pueden ser iguales\"}"))),
            @ApiResponse(responseCode = "503", description = "Servicio externo no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar el participante desde registration-service\"}")))
    })
    public ResponseEntity<PartidaDTO> actualizarPartida(
            @PathVariable Long id,
            @Valid @RequestBody PartidaDTO partidaDTO) {

        PartidaDTO partidaActualizada = partidaService.actualizarPartida(id, partidaDTO);
        return ResponseEntity.ok(partidaActualizada);
    }

    @PutMapping("/{id}/estado/{estado}")
    @Operation(summary = "Actualizar estado de partida", description = "Actualiza el estado de una partida existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de partida actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Partida no encontrada\"}")))
    })
    public ResponseEntity<PartidaDTO> actualizarEstado(
            @Parameter(description = "ID de la partida", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado de la partida", example = "FINALIZADA")
            @PathVariable String estado) {

        PartidaDTO partidaActualizada = partidaService.actualizarEstado(id, estado);
        return ResponseEntity.ok(partidaActualizada);
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar partida", description = "Cambia el estado de una partida a CANCELADA")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Partida cancelada correctamente"),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Partida no encontrada\"}")))
    })
    public ResponseEntity<Void> cancelarPartida(
            @Parameter(description = "ID de la partida", example = "1")
            @PathVariable Long id) {

        partidaService.cancelarPartida(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/torneo/{torneoId}")
    @Operation(summary = "Listar partidas por torneo", description = "Obtiene partidas asociadas a un torneo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partidas filtradas por torneo correctamente")
    })
    public ResponseEntity<List<PartidaDTO>> listarPorTorneo(
            @Parameter(description = "ID del torneo", example = "1")
            @PathVariable Long torneoId) {

        return ResponseEntity.ok(partidaService.listarPorTorneo(torneoId));
    }

    @GetMapping("/ronda/{ronda}")
    @Operation(summary = "Listar partidas por ronda", description = "Obtiene partidas filtradas por ronda")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partidas filtradas por ronda correctamente")
    })
    public ResponseEntity<List<PartidaDTO>> listarPorRonda(
            @Parameter(description = "Ronda de la partida", example = "SEMIFINAL")
            @PathVariable String ronda) {

        return ResponseEntity.ok(partidaService.listarPorRonda(ronda));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar partidas por estado", description = "Obtiene partidas filtradas por estado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partidas filtradas por estado correctamente")
    })
    public ResponseEntity<List<PartidaDTO>> listarPorEstado(
            @Parameter(description = "Estado de la partida", example = "PROGRAMADA")
            @PathVariable String estado) {

        return ResponseEntity.ok(partidaService.listarPorEstado(estado));
    }
}
package cl.duoc.esports.rankingservice.controllers;

import cl.duoc.esports.rankingservice.dto.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.duoc.esports.rankingservice.dto.RankingDTO;
import cl.duoc.esports.rankingservice.services.RankingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@Tag(name = "Rankings", description = "Operaciones relacionadas con la gestión de rankings")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @PostMapping
    @Operation(summary = "Crear registro de ranking", description = "Registra un participante en el ranking de un torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro de ranking creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"torneoId\":\"El torneo es obligatorio\",\"participanteId\":\"El participante es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Torneo o participante no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El participante no existe o no está inscrito\"}"))),
            @ApiResponse(responseCode = "409", description = "Participante duplicado, torneo cancelado o inscripción cancelada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El participante ya existe en el ranking de este torneo\"}"))),
            @ApiResponse(responseCode = "503", description = "Servicio externo no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar el participante desde registration-service\"}")))
    })
    public ResponseEntity<RankingDTO> crearRegistroRanking(@Valid @RequestBody RankingDTO rankingDTO) {
        RankingDTO nuevoRanking = rankingService.crearRegistroRanking(rankingDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoRanking);
    }

    @GetMapping
    @Operation(summary = "Listar rankings", description = "Obtiene el listado completo de registros de ranking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de rankings obtenido correctamente")
    })
    public ResponseEntity<List<RankingDTO>> listarRankings() {
        return ResponseEntity.ok(rankingService.listarRankings());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ranking por ID", description = "Obtiene un registro de ranking específico mediante su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro de ranking encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Registro de ranking no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Registro de ranking no encontrado\"}")))
    })
    public ResponseEntity<RankingDTO> buscarRankingPorId(
            @Parameter(description = "ID del ranking", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(rankingService.buscarRankingPorId(id));
    }

    @GetMapping("/torneo/{torneoId}")
    @Operation(summary = "Listar ranking por torneo", description = "Obtiene el ranking completo asociado a un torneo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking del torneo obtenido correctamente")
    })
    public ResponseEntity<List<RankingDTO>> listarRankingPorTorneo(
            @Parameter(description = "ID del torneo", example = "1")
            @PathVariable Long torneoId) {

        return ResponseEntity.ok(rankingService.listarRankingPorTorneo(torneoId));
    }

    @GetMapping("/torneo/{torneoId}/participante/{participanteId}")
    @Operation(summary = "Buscar posición por participante", description = "Obtiene la posición de un participante dentro del ranking de un torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posición del participante encontrada correctamente"),
            @ApiResponse(responseCode = "404", description = "Participante no existe en el ranking del torneo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El participante no existe en el ranking de este torneo\"}")))
    })
    public ResponseEntity<RankingDTO> buscarPosicionPorParticipante(
            @Parameter(description = "ID del torneo", example = "1")
            @PathVariable Long torneoId,
            @Parameter(description = "ID del participante", example = "2")
            @PathVariable Long participanteId) {

        return ResponseEntity.ok(
                rankingService.buscarPosicionPorParticipante(torneoId, participanteId)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ranking", description = "Actualiza manualmente los datos de un registro de ranking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"puntos\":\"Los puntos no pueden ser negativos\"}"))),
            @ApiResponse(responseCode = "404", description = "Registro de ranking no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Registro de ranking no encontrado\"}")))
    })
    public ResponseEntity<RankingDTO> actualizarRanking(
            @Parameter(description = "ID del ranking", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody RankingDTO rankingDTO) {

        RankingDTO rankingActualizado = rankingService.actualizarRanking(id, rankingDTO);
        return ResponseEntity.ok(rankingActualizado);
    }

    @PutMapping("/resultado/{resultadoId}")
    @Operation(summary = "Actualizar ranking por resultado", description = "Actualiza el ranking de un torneo a partir de un resultado validado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking actualizado correctamente por resultado"),
            @ApiResponse(responseCode = "404", description = "Resultado, torneo o participante no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El resultado no existe\"}"))),
            @ApiResponse(responseCode = "409", description = "Resultado no validado, torneo cancelado o inscripción cancelada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Solo resultados validados pueden actualizar el ranking\"}"))),
            @ApiResponse(responseCode = "422", description = "Ganador no corresponde a los participantes indicados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El ganador no corresponde a los participantes indicados\"}"))),
            @ApiResponse(responseCode = "503", description = "Servicio externo no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar el resultado desde result-service\"}")))
    })
    public ResponseEntity<RankingDTO> actualizarRankingPorResultado(
            @Parameter(description = "ID del resultado", example = "1")
            @PathVariable Long resultadoId,
            @Parameter(description = "ID del torneo", example = "1")
            @RequestParam Long torneoId,
            @Parameter(description = "ID del participante A", example = "2")
            @RequestParam Long participanteAId,
            @Parameter(description = "ID del participante B", example = "3")
            @RequestParam Long participanteBId) {

        RankingDTO rankingActualizado = rankingService.actualizarRankingPorResultado(
                resultadoId,
                torneoId,
                participanteAId,
                participanteBId
        );

        return ResponseEntity.ok(rankingActualizado);
    }

    @DeleteMapping("/torneo/{torneoId}/reiniciar")
    @Operation(summary = "Reiniciar ranking por torneo", description = "Elimina los registros de ranking asociados a un torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ranking reiniciado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existen registros de ranking para el torneo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No existen registros de ranking para este torneo\"}")))
    })
    public ResponseEntity<Void> reiniciarRankingPorTorneo(
            @Parameter(description = "ID del torneo", example = "1")
            @PathVariable Long torneoId) {

        rankingService.reiniciarRankingPorTorneo(torneoId);
        return ResponseEntity.noContent().build();
    }
}
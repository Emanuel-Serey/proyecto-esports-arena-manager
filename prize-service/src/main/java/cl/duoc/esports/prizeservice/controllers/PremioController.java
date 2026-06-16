package cl.duoc.esports.prizeservice.controllers;

import cl.duoc.esports.prizeservice.dto.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.duoc.esports.prizeservice.dto.PremioDTO;
import cl.duoc.esports.prizeservice.services.PremioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/premios")
@Tag(name = "Premios", description = "Operaciones relacionadas con la gestión de premios")
public class PremioController {

    @Autowired
    private PremioService premioService;

    @PostMapping
    @Operation(summary = "Asignar premio", description = "Asigna un premio a un participante validando torneo, ranking y posición")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Premio asignado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"torneoId\":\"El torneo es obligatorio\",\"participanteId\":\"El participante es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Torneo o participante no encontrado en ranking",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El participante no existe en el ranking del torneo\"}"))),
            @ApiResponse(responseCode = "409", description = "Premio duplicado o torneo cancelado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El premio ya fue asignado a este participante en esta posición\"}"))),
            @ApiResponse(responseCode = "422", description = "Posición no coincide con ranking",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"La posición indicada no coincide con la posición del participante en el ranking\"}"))),
            @ApiResponse(responseCode = "503", description = "Servicio externo no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar el ranking del participante desde ranking-service\"}")))
    })
    public ResponseEntity<PremioDTO> asignarPremio(@Valid @RequestBody PremioDTO premioDTO) {
        PremioDTO nuevoPremio = premioService.asignarPremio(premioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPremio);
    }

    @GetMapping
    @Operation(summary = "Listar premios", description = "Obtiene el listado completo de premios registrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de premios obtenido correctamente")
    })
    public ResponseEntity<List<PremioDTO>> listarPremios() {
        return ResponseEntity.ok(premioService.listarPremios());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar premio por ID", description = "Obtiene un premio específico mediante su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Premio encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Premio no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Premio no encontrado\"}")))
    })
    public ResponseEntity<PremioDTO> buscarPremioPorId(
            @Parameter(description = "ID del premio", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(premioService.buscarPremioPorId(id));
    }

    @GetMapping("/torneo/{torneoId}")
    @Operation(summary = "Listar premios por torneo", description = "Obtiene premios asociados a un torneo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Premios filtrados por torneo correctamente")
    })
    public ResponseEntity<List<PremioDTO>> listarPremiosPorTorneo(
            @Parameter(description = "ID del torneo", example = "1")
            @PathVariable Long torneoId) {

        return ResponseEntity.ok(premioService.listarPremiosPorTorneo(torneoId));
    }

    @GetMapping("/participante/{participanteId}")
    @Operation(summary = "Listar premios por participante", description = "Obtiene premios asociados a un participante específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Premios filtrados por participante correctamente")
    })
    public ResponseEntity<List<PremioDTO>> listarPremiosPorParticipante(
            @Parameter(description = "ID del participante", example = "1")
            @PathVariable Long participanteId) {

        return ResponseEntity.ok(premioService.listarPremiosPorParticipante(participanteId));
    }

    @GetMapping("/estado/{estadoEntrega}")
    @Operation(summary = "Listar premios por estado de entrega", description = "Obtiene premios filtrados por estado de entrega")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Premios filtrados por estado de entrega correctamente")
    })
    public ResponseEntity<List<PremioDTO>> listarPremiosPorEstado(
            @Parameter(description = "Estado de entrega del premio", example = "PENDIENTE")
            @PathVariable String estadoEntrega) {

        return ResponseEntity.ok(premioService.listarPremiosPorEstado(estadoEntrega));
    }

    @PutMapping("/{id}/entregar")
    @Operation(summary = "Marcar premio como entregado", description = "Cambia el estado de entrega de un premio a ENTREGADO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Premio marcado como entregado correctamente"),
            @ApiResponse(responseCode = "404", description = "Premio no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Premio no encontrado\"}"))),
            @ApiResponse(responseCode = "409", description = "Premio anulado no puede ser entregado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se puede entregar un premio anulado\"}")))
    })
    public ResponseEntity<PremioDTO> marcarComoEntregado(
            @Parameter(description = "ID del premio", example = "1")
            @PathVariable Long id) {

        PremioDTO premioEntregado = premioService.marcarComoEntregado(id);
        return ResponseEntity.ok(premioEntregado);
    }

    @PutMapping("/{id}/anular")
    @Operation(summary = "Anular premio", description = "Cambia el estado de entrega de un premio a ANULADO")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Premio anulado correctamente"),
            @ApiResponse(responseCode = "404", description = "Premio no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Premio no encontrado\"}"))),
            @ApiResponse(responseCode = "409", description = "Premio entregado no puede ser anulado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se puede anular un premio ya entregado\"}")))
    })
    public ResponseEntity<Void> anularPremio(
            @Parameter(description = "ID del premio", example = "1")
            @PathVariable Long id) {

        premioService.anularPremio(id);
        return ResponseEntity.noContent().build();
    }
}

package cl.duoc.esports.resultservice.controllers;

import cl.duoc.esports.resultservice.dto.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.duoc.esports.resultservice.dto.ResultadoDTO;
import cl.duoc.esports.resultservice.services.ResultadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resultados")
@Tag(name = "Resultados", description = "Operaciones relacionadas con la gestión de resultados")
public class ResultadoController {

    @Autowired
    private ResultadoService resultadoService;

    @PostMapping
    @Operation(summary = "Crear resultado", description = "Registra el resultado de una partida validando partida y ganador")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Resultado creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"partidaId\":\"La partida es obligatoria\",\"ganadorId\":\"El ganador es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Partida no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"La partida no existe\"}"))),
            @ApiResponse(responseCode = "409", description = "Resultado duplicado o partida no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"La partida ya tiene un resultado registrado\"}"))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El ganador debe ser uno de los participantes de la partida\"}"))),
            @ApiResponse(responseCode = "503", description = "Servicio externo no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar la partida desde match-service\"}")))
    })
    public ResponseEntity<ResultadoDTO> crearResultado(@Valid @RequestBody ResultadoDTO resultadoDTO) {
        ResultadoDTO nuevoResultado = resultadoService.crearResultado(resultadoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoResultado);
    }

    @GetMapping
    @Operation(summary = "Listar resultados", description = "Obtiene el listado completo de resultados registrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de resultados obtenido correctamente")
    })
    public ResponseEntity<List<ResultadoDTO>> listarResultados() {
        return ResponseEntity.ok(resultadoService.listarResultados());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar resultado por ID", description = "Obtiene un resultado específico mediante su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Resultado no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Resultado no encontrado\"}")))
    })
    public ResponseEntity<ResultadoDTO> buscarResultadoPorId(
            @Parameter(description = "ID del resultado", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(resultadoService.buscarResultadoPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar resultado", description = "Actualiza los datos de un resultado existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"ganadorId\":\"El ganador es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Resultado o partida no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Resultado no encontrado\"}"))),
            @ApiResponse(responseCode = "409", description = "Resultado validado, anulado o partida no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se puede modificar un resultado validado\"}"))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El ganador debe ser uno de los participantes de la partida\"}"))),
            @ApiResponse(responseCode = "503", description = "Servicio externo no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar la partida desde match-service\"}")))
    })
    public ResponseEntity<ResultadoDTO> actualizarResultado(
            @Parameter(description = "ID del resultado", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ResultadoDTO resultadoDTO) {

        ResultadoDTO resultadoActualizado = resultadoService.actualizarResultado(id, resultadoDTO);
        return ResponseEntity.ok(resultadoActualizado);
    }

    @PutMapping("/{id}/validar")
    @Operation(summary = "Validar resultado", description = "Cambia el estado de validación de un resultado a VALIDADO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado validado correctamente"),
            @ApiResponse(responseCode = "404", description = "Resultado no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Resultado no encontrado\"}"))),
            @ApiResponse(responseCode = "409", description = "Resultado anulado no puede ser validado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se puede validar un resultado anulado\"}")))
    })
    public ResponseEntity<ResultadoDTO> validarResultado(
            @Parameter(description = "ID del resultado", example = "1")
            @PathVariable Long id) {

        ResultadoDTO resultadoValidado = resultadoService.validarResultado(id);
        return ResponseEntity.ok(resultadoValidado);
    }

    @PutMapping("/{id}/anular")
    @Operation(summary = "Anular resultado", description = "Anula un resultado indicando una justificación")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Resultado anulado correctamente"),
            @ApiResponse(responseCode = "400", description = "Justificación inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Debe indicar una justificación para anular el resultado\"}"))),
            @ApiResponse(responseCode = "404", description = "Resultado no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Resultado no encontrado\"}")))
    })
    public ResponseEntity<Void> anularResultado(
            @Parameter(description = "ID del resultado", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Justificación de la anulación", example = "Error en el registro del resultado")
            @RequestParam String justificacion) {

        resultadoService.anularResultado(id, justificacion);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/partida/{partidaId}")
    @Operation(summary = "Listar resultados por partida", description = "Obtiene los resultados asociados a una partida específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados filtrados por partida correctamente")
    })
    public ResponseEntity<List<ResultadoDTO>> listarPorPartida(
            @Parameter(description = "ID de la partida", example = "1")
            @PathVariable Long partidaId) {

        return ResponseEntity.ok(resultadoService.listarPorPartida(partidaId));
    }
}

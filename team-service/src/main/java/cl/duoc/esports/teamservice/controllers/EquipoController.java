package cl.duoc.esports.teamservice.controllers;

import cl.duoc.esports.teamservice.dto.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.duoc.esports.teamservice.dto.EquipoDTO;
import cl.duoc.esports.teamservice.dto.MiembroEquipoDTO;
import cl.duoc.esports.teamservice.services.EquipoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipos")
@Tag(name = "Equipos", description = "Operaciones relacionadas con la gestión de equipos")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @PostMapping
    @Operation(summary = "Crear equipo", description = "Registra un nuevo equipo validando juego, capitán y miembros")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipo creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"nombre\":\"El nombre del equipo es obligatorio\",\"capitanId\":\"El capitán es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Juego o usuario no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El usuario no existe\"}"))),
            @ApiResponse(responseCode = "409", description = "Conflicto con equipo, juego o usuario",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El usuario ya pertenece a otro equipo activo\"}"))),
            @ApiResponse(responseCode = "422", description = "Regla de negocio inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El capitán debe estar incluido en la lista de miembros\"}"))),
            @ApiResponse(responseCode = "503", description = "Servicio externo no disponible",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar el usuario desde user-service\"}")))
    })
    public ResponseEntity<EquipoDTO> crearEquipo(@Valid @RequestBody EquipoDTO equipoDTO) {
        EquipoDTO nuevoEquipo = equipoService.crearEquipo(equipoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEquipo);
    }

    @GetMapping
    @Operation(summary = "Listar equipos", description = "Obtiene el listado completo de equipos registrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de equipos obtenido correctamente")
    })
    public ResponseEntity<List<EquipoDTO>> listarEquipos() {
        return ResponseEntity.ok(equipoService.listarEquipos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar equipo por ID", description = "Obtiene la información de un equipo específico mediante su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipo encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Equipo no encontrado\"}")))
    })
    public ResponseEntity<EquipoDTO> buscarEquipoPorId(
            @Parameter(description = "ID del equipo", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(equipoService.buscarEquipoPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar equipo", description = "Actualiza los datos de un equipo existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipo actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"nombre\":\"El nombre del equipo es obligatorio\"}"))),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Equipo no encontrado\"}"))),
            @ApiResponse(responseCode = "409", description = "Nombre de equipo ya registrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El nombre del equipo ya está registrado\"}")))
    })
    public ResponseEntity<EquipoDTO> actualizarEquipo(
            @Parameter(description = "ID del equipo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody EquipoDTO equipoDTO) {

        EquipoDTO equipoActualizado = equipoService.actualizarEquipo(id, equipoDTO);
        return ResponseEntity.ok(equipoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar equipo", description = "Desactiva lógicamente un equipo del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Equipo desactivado correctamente"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Equipo no encontrado\"}")))
    })
    public ResponseEntity<Void> desactivarEquipo(
            @Parameter(description = "ID del equipo", example = "1")
            @PathVariable Long id) {

        equipoService.desactivarEquipo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar equipos por estado", description = "Obtiene equipos filtrados por estado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipos filtrados por estado correctamente")
    })
    public ResponseEntity<List<EquipoDTO>> listarPorEstado(
            @Parameter(description = "Estado del equipo", example = "ACTIVO")
            @PathVariable String estado) {

        return ResponseEntity.ok(equipoService.listarPorEstado(estado));
    }

    @GetMapping("/juego/{juegoPrincipalId}")
    @Operation(summary = "Listar equipos por juego", description = "Obtiene equipos asociados a un juego principal específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipos filtrados por juego correctamente")
    })
    public ResponseEntity<List<EquipoDTO>> listarPorJuego(
            @Parameter(description = "ID del juego principal", example = "1")
            @PathVariable Long juegoPrincipalId) {

        return ResponseEntity.ok(equipoService.listarPorJuegoPrincipal(juegoPrincipalId));
    }

    @GetMapping("/capitan/{capitanId}")
    @Operation(summary = "Listar equipos por capitán", description = "Obtiene equipos asociados a un capitán específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipos filtrados por capitán correctamente")
    })
    public ResponseEntity<List<EquipoDTO>> listarPorCapitan(
            @Parameter(description = "ID del capitán", example = "1")
            @PathVariable Long capitanId) {

        return ResponseEntity.ok(equipoService.listarPorCapitan(capitanId));
    }

    @PostMapping("/{id}/miembros")
    @Operation(summary = "Agregar miembro", description = "Agrega un nuevo miembro a un equipo existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Miembro agregado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud"),
            @ApiResponse(responseCode = "404", description = "Equipo o usuario no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El usuario no existe\"}"))),
            @ApiResponse(responseCode = "409", description = "Usuario inactivo o ya pertenece a un equipo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"El usuario ya pertenece a otro equipo activo\"}"))),
            @ApiResponse(responseCode = "500", description = "Error interno al agregar miembro",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo agregar el miembro al equipo\"}"))),
            @ApiResponse(responseCode = "503", description = "No se pudo validar el usuario desde user-service",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se pudo validar el usuario desde user-service\"}")))
    })
    public ResponseEntity<MiembroEquipoDTO> agregarMiembro(
            @Parameter(description = "ID del equipo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody MiembroEquipoDTO miembroDTO) {

        return ResponseEntity.ok(equipoService.agregarMiembro(id, miembroDTO));
    }

    @GetMapping("/{equipoId}/miembros")
    @Operation(summary = "Listar miembros del equipo", description = "Obtiene todos los miembros asociados a un equipo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Miembros del equipo obtenidos correctamente"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Equipo no encontrado\"}")))
    })
    public ResponseEntity<List<MiembroEquipoDTO>> listarMiembros(
            @Parameter(description = "ID del equipo", example = "1")
            @PathVariable Long equipoId) {

        return ResponseEntity.ok(equipoService.listarMiembros(equipoId));
    }

    @DeleteMapping("/{id}/miembros/{miembroId}")
    @Operation(summary = "Eliminar miembro", description = "Elimina un miembro de un equipo existente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Miembro eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Equipo o miembro no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"Miembro no encontrado\"}"))),
            @ApiResponse(responseCode = "422", description = "No se puede eliminar al capitán del equipo",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = "{\"error\":\"No se puede eliminar al capitán del equipo\"}")))
    })
    public ResponseEntity<Void> eliminarMiembro(
            @Parameter(description = "ID del equipo", example = "1")
            @PathVariable Long id,
            @Parameter(description = "ID del miembro", example = "2")
            @PathVariable Long miembroId) {

        equipoService.eliminarMiembro(id, miembroId);
        return ResponseEntity.noContent().build();
    }
}
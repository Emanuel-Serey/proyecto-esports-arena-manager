package cl.duoc.esports.registrationservice.controllers;

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
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @PostMapping
    public ResponseEntity<InscripcionDTO> crearInscripcion(@Valid @RequestBody InscripcionDTO inscripcionDTO) {
        InscripcionDTO nuevaInscripcion = inscripcionService.crearInscripcion(inscripcionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaInscripcion);
    }

    @GetMapping
    public ResponseEntity<List<InscripcionDTO>> listarInscripciones() {
        return ResponseEntity.ok(inscripcionService.listarInscripciones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InscripcionDTO> buscarInscripcionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(inscripcionService.buscarInscripcionPorId(id));
    }

    @PutMapping("/{id}/estado/{estado}")
    public ResponseEntity<InscripcionDTO> actualizarEstado(
            @PathVariable Long id,
            @PathVariable String estado) {

        InscripcionDTO inscripcionActualizada = inscripcionService.actualizarEstado(id, estado);
        return ResponseEntity.ok(inscripcionActualizada);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarInscripcion(@PathVariable Long id) {
        inscripcionService.cancelarInscripcion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<InscripcionDTO>> listarPorTorneo(@PathVariable Long torneoId) {
        return ResponseEntity.ok(inscripcionService.listarPorTorneo(torneoId));
    }

    @GetMapping("/equipo/{equipoId}")
    public ResponseEntity<List<InscripcionDTO>> listarPorEquipo(@PathVariable Long equipoId) {
        return ResponseEntity.ok(inscripcionService.listarPorEquipo(equipoId));
    }

    @GetMapping("/jugador/{jugadorId}")
    public ResponseEntity<List<InscripcionDTO>> listarPorJugador(@PathVariable Long jugadorId) {
        return ResponseEntity.ok(inscripcionService.listarPorJugador(jugadorId));
    }
}
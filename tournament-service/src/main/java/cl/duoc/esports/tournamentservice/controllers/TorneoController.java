package cl.duoc.esports.tournamentservice.controllers;

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
public class TorneoController {

    @Autowired
    private TorneoService torneoService;

    @PostMapping
    public ResponseEntity<TorneoDTO> crearTorneo(@Valid @RequestBody TorneoDTO torneoDTO) {
        TorneoDTO nuevoTorneo = torneoService.crearTorneo(torneoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTorneo);
    }

    @GetMapping
    public ResponseEntity<List<TorneoDTO>> listarTorneos() {
        return ResponseEntity.ok(torneoService.listarTorneos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TorneoDTO> buscarTorneoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.buscarTorneoPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TorneoDTO> actualizarTorneo(
            @PathVariable Long id,
            @Valid @RequestBody TorneoDTO torneoDTO) {

        TorneoDTO torneoActualizado = torneoService.actualizarTorneo(id, torneoDTO);
        return ResponseEntity.ok(torneoActualizado);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarTorneo(@PathVariable Long id) {
        torneoService.cancelarTorneo(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/cerrar")
    public ResponseEntity<Void> cerrarTorneo(@PathVariable Long id) {
        torneoService.cerrarTorneo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/juego/{juegoId}")
    public ResponseEntity<List<TorneoDTO>> listarPorJuego(@PathVariable Long juegoId) {
        return ResponseEntity.ok(torneoService.listarPorJuego(juegoId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<TorneoDTO>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(torneoService.listarPorEstado(estado));
    }

    @GetMapping("/fecha/{fechaInicio}")
    public ResponseEntity<List<TorneoDTO>> listarPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio) {

        return ResponseEntity.ok(torneoService.listarPorFecha(fechaInicio));
    }
}
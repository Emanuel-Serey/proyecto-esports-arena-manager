package cl.duoc.esports.matchservice.controllers;

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
public class PartidaController {

    @Autowired
    private PartidaService partidaService;

    @PostMapping
    public ResponseEntity<PartidaDTO> crearPartida(@Valid @RequestBody PartidaDTO partidaDTO) {
        PartidaDTO nuevaPartida = partidaService.crearPartida(partidaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPartida);
    }

    @GetMapping
    public ResponseEntity<List<PartidaDTO>> listarPartidas() {
        return ResponseEntity.ok(partidaService.listarPartidas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartidaDTO> buscarPartidaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(partidaService.buscarPartidaPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartidaDTO> actualizarPartida(
            @PathVariable Long id,
            @Valid @RequestBody PartidaDTO partidaDTO) {

        PartidaDTO partidaActualizada = partidaService.actualizarPartida(id, partidaDTO);
        return ResponseEntity.ok(partidaActualizada);
    }

    @PutMapping("/{id}/estado/{estado}")
    public ResponseEntity<PartidaDTO> actualizarEstado(
            @PathVariable Long id,
            @PathVariable String estado) {

        PartidaDTO partidaActualizada = partidaService.actualizarEstado(id, estado);
        return ResponseEntity.ok(partidaActualizada);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarPartida(@PathVariable Long id) {
        partidaService.cancelarPartida(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<PartidaDTO>> listarPorTorneo(@PathVariable Long torneoId) {
        return ResponseEntity.ok(partidaService.listarPorTorneo(torneoId));
    }

    @GetMapping("/ronda/{ronda}")
    public ResponseEntity<List<PartidaDTO>> listarPorRonda(@PathVariable String ronda) {
        return ResponseEntity.ok(partidaService.listarPorRonda(ronda));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PartidaDTO>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(partidaService.listarPorEstado(estado));
    }
}
package cl.duoc.esports.prizeservice.controllers;

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
public class PremioController {

    @Autowired
    private PremioService premioService;

    @PostMapping
    public ResponseEntity<PremioDTO> asignarPremio(@Valid @RequestBody PremioDTO premioDTO) {
        PremioDTO nuevoPremio = premioService.asignarPremio(premioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPremio);
    }

    @GetMapping
    public ResponseEntity<List<PremioDTO>> listarPremios() {
        return ResponseEntity.ok(premioService.listarPremios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PremioDTO> buscarPremioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(premioService.buscarPremioPorId(id));
    }

    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<PremioDTO>> listarPremiosPorTorneo(@PathVariable Long torneoId) {
        return ResponseEntity.ok(premioService.listarPremiosPorTorneo(torneoId));
    }

    @GetMapping("/participante/{participanteId}")
    public ResponseEntity<List<PremioDTO>> listarPremiosPorParticipante(@PathVariable Long participanteId) {
        return ResponseEntity.ok(premioService.listarPremiosPorParticipante(participanteId));
    }

    @GetMapping("/estado/{estadoEntrega}")
    public ResponseEntity<List<PremioDTO>> listarPremiosPorEstado(@PathVariable String estadoEntrega) {
        return ResponseEntity.ok(premioService.listarPremiosPorEstado(estadoEntrega));
    }

    @PutMapping("/{id}/entregar")
    public ResponseEntity<PremioDTO> marcarComoEntregado(@PathVariable Long id) {
        PremioDTO premioEntregado = premioService.marcarComoEntregado(id);
        return ResponseEntity.ok(premioEntregado);
    }

    @PutMapping("/{id}/anular")
    public ResponseEntity<Void> anularPremio(@PathVariable Long id) {
        premioService.anularPremio(id);
        return ResponseEntity.noContent().build();
    }
}

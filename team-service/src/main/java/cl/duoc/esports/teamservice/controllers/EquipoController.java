package cl.duoc.esports.teamservice.controllers;

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
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @PostMapping
    public ResponseEntity<EquipoDTO> crearEquipo(@Valid @RequestBody EquipoDTO equipoDTO) {
        EquipoDTO nuevoEquipo = equipoService.crearEquipo(equipoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEquipo);
    }

    @GetMapping
    public ResponseEntity<List<EquipoDTO>> listarEquipos() {
        return ResponseEntity.ok(equipoService.listarEquipos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipoDTO> buscarEquipoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(equipoService.buscarEquipoPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipoDTO> actualizarEquipo(
            @PathVariable Long id,
            @Valid @RequestBody EquipoDTO equipoDTO) {

        EquipoDTO equipoActualizado = equipoService.actualizarEquipo(id, equipoDTO);
        return ResponseEntity.ok(equipoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarEquipo(@PathVariable Long id) {
        equipoService.desactivarEquipo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<EquipoDTO>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(equipoService.listarPorEstado(estado));
    }

    @GetMapping("/juego/{juegoPrincipalId}")
    public ResponseEntity<List<EquipoDTO>> listarPorJuego(@PathVariable Long juegoPrincipalId) {
        return ResponseEntity.ok(equipoService.listarPorJuegoPrincipal(juegoPrincipalId));
    }

    @GetMapping("/capitan/{capitanId}")
    public ResponseEntity<List<EquipoDTO>> listarPorCapitan(@PathVariable Long capitanId) {
        return ResponseEntity.ok(equipoService.listarPorCapitan(capitanId));
    }

    @PostMapping("/{equipoId}/miembros")
    public ResponseEntity<MiembroEquipoDTO> agregarMiembro(
            @PathVariable Long equipoId,
            @Valid @RequestBody MiembroEquipoDTO miembroEquipoDTO) {

        MiembroEquipoDTO nuevoMiembro = equipoService.agregarMiembro(equipoId, miembroEquipoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMiembro);
    }

    @GetMapping("/{equipoId}/miembros")
    public ResponseEntity<List<MiembroEquipoDTO>> listarMiembros(@PathVariable Long equipoId) {
        return ResponseEntity.ok(equipoService.listarMiembros(equipoId));
    }

    @DeleteMapping("/{equipoId}/miembros/{miembroId}")
    public ResponseEntity<Void> eliminarMiembro(
            @PathVariable Long equipoId,
            @PathVariable Long miembroId) {

        equipoService.eliminarMiembro(equipoId, miembroId);
        return ResponseEntity.noContent().build();
    }
}
package cl.duoc.esports.sanctionservice.controllers;

import cl.duoc.esports.sanctionservice.dto.SancionDTO;
import cl.duoc.esports.sanctionservice.services.SancionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sanciones")
public class SancionController {

    @Autowired
    private SancionService sancionService;

    @PostMapping
    public ResponseEntity<SancionDTO> crearSancion(@Valid @RequestBody SancionDTO sancionDTO) {
        SancionDTO nuevaSancion = sancionService.crearSancion(sancionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaSancion);
    }

    @GetMapping
    public ResponseEntity<List<SancionDTO>> listarSanciones() {
        return ResponseEntity.ok(sancionService.listarSanciones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SancionDTO> buscarSancionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sancionService.buscarSancionPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SancionDTO> actualizarSancion(
            @PathVariable Long id,
            @Valid @RequestBody SancionDTO sancionDTO) {

        SancionDTO sancionActualizada = sancionService.actualizarSancion(id, sancionDTO);
        return ResponseEntity.ok(sancionActualizada);
    }

    @PutMapping("/{id}/cerrar")
    public ResponseEntity<Void> cerrarSancion(@PathVariable Long id) {
        sancionService.cerrarSancion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<SancionDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(sancionService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/equipo/{equipoId}")
    public ResponseEntity<List<SancionDTO>> listarPorEquipo(@PathVariable Long equipoId) {
        return ResponseEntity.ok(sancionService.listarPorEquipo(equipoId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SancionDTO>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(sancionService.listarPorEstado(estado));
    }

    @GetMapping("/usuario/{usuarioId}/activa")
    public ResponseEntity<Boolean> existeSancionActivaUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(sancionService.existeSancionActivaUsuario(usuarioId));
    }

    @GetMapping("/equipo/{equipoId}/activa")
    public ResponseEntity<Boolean> existeSancionActivaEquipo(@PathVariable Long equipoId) {
        return ResponseEntity.ok(sancionService.existeSancionActivaEquipo(equipoId));
    }
}
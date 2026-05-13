package cl.duoc.esports.gameservice.controllers;

import cl.duoc.esports.gameservice.dto.JuegoDTO;
import cl.duoc.esports.gameservice.services.JuegoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/juegos")
public class JuegoController {

    @Autowired
    private JuegoService juegoService;

    @PostMapping
    public ResponseEntity<JuegoDTO> crearJuego(@Valid @RequestBody JuegoDTO juegoDTO) {
        JuegoDTO nuevoJuego = juegoService.crearJuego(juegoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoJuego);
    }

    @GetMapping
    public ResponseEntity<List<JuegoDTO>> listarJuegos() {
        return ResponseEntity.ok(juegoService.listarJuegos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<JuegoDTO>> listarJuegosActivos() {
        return ResponseEntity.ok(juegoService.listarJuegosActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JuegoDTO> buscarJuegoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(juegoService.buscarJuegoPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JuegoDTO> actualizarJuego(
            @PathVariable Long id,
            @Valid @RequestBody JuegoDTO juegoDTO) {

        JuegoDTO juegoActualizado = juegoService.actualizarJuego(id, juegoDTO);
        return ResponseEntity.ok(juegoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarJuego(@PathVariable Long id) {
        juegoService.desactivarJuego(id);
        return ResponseEntity.noContent().build();
    }
}

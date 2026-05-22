package cl.duoc.esports.resultservice.controllers;

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
public class ResultadoController {

    @Autowired
    private ResultadoService resultadoService;

    @PostMapping
    public ResponseEntity<ResultadoDTO> crearResultado(@Valid @RequestBody ResultadoDTO resultadoDTO) {
        ResultadoDTO nuevoResultado = resultadoService.crearResultado(resultadoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoResultado);
    }

    @GetMapping
    public ResponseEntity<List<ResultadoDTO>> listarResultados() {
        return ResponseEntity.ok(resultadoService.listarResultados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResultadoDTO> buscarResultadoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(resultadoService.buscarResultadoPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResultadoDTO> actualizarResultado(
            @PathVariable Long id,
            @Valid @RequestBody ResultadoDTO resultadoDTO) {

        ResultadoDTO resultadoActualizado = resultadoService.actualizarResultado(id, resultadoDTO);
        return ResponseEntity.ok(resultadoActualizado);
    }

    @PutMapping("/{id}/validar")
    public ResponseEntity<ResultadoDTO> validarResultado(@PathVariable Long id) {
        ResultadoDTO resultadoValidado = resultadoService.validarResultado(id);
        return ResponseEntity.ok(resultadoValidado);
    }

    @PutMapping("/{id}/anular")
    public ResponseEntity<Void> anularResultado(
            @PathVariable Long id,
            @RequestParam String justificacion) {

        resultadoService.anularResultado(id, justificacion);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/partida/{partidaId}")
    public ResponseEntity<List<ResultadoDTO>> listarPorPartida(@PathVariable Long partidaId) {
        return ResponseEntity.ok(resultadoService.listarPorPartida(partidaId));
    }
}

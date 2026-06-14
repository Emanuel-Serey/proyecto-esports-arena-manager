package cl.duoc.esports.rankingservice.controllers;

import cl.duoc.esports.rankingservice.exceptions.RankingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(RankingException.class)
    public ResponseEntity<Map<String, String>> handleRankingException(RankingException ex) {
        Map<String, String> error = new HashMap<>();

        error.put("error", ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {

        Map<String, String> error = new HashMap<>();

        error.put("error", "No se puede duplicar un participante en el ranking del mismo torneo");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}

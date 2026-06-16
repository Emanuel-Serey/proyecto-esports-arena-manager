package cl.duoc.esports.tournamentservice.controllers;

import cl.duoc.esports.tournamentservice.dto.ErrorResponseDTO;
import cl.duoc.esports.tournamentservice.exceptions.TorneoException;
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

    @ExceptionHandler(TorneoException.class)
    public ResponseEntity<ErrorResponseDTO> handleTorneoException(TorneoException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(error);
    }
}
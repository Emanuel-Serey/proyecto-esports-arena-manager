package cl.duoc.esports.teamservice.controllers;

import cl.duoc.esports.teamservice.dto.ErrorResponseDTO;
import cl.duoc.esports.teamservice.exceptions.EquipoException;
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

    @ExceptionHandler(EquipoException.class)
    public ResponseEntity<ErrorResponseDTO> handleEquipoException(EquipoException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(error);
    }
}
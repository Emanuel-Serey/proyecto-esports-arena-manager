package cl.duoc.esports.registrationservice.exceptions;

import org.springframework.http.HttpStatus;

public class InscripcionException extends RuntimeException {

    private final HttpStatus status;

    public InscripcionException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public InscripcionException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
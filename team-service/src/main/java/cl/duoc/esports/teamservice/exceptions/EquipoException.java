package cl.duoc.esports.teamservice.exceptions;

import org.springframework.http.HttpStatus;

public class EquipoException extends RuntimeException {

    private final HttpStatus status;

    public EquipoException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public EquipoException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
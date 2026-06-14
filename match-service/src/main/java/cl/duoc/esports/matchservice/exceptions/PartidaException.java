package cl.duoc.esports.matchservice.exceptions;

import org.springframework.http.HttpStatus;

public class PartidaException extends RuntimeException {

    private final HttpStatus status;

    public PartidaException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public PartidaException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
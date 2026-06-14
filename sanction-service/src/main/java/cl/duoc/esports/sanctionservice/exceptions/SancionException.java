package cl.duoc.esports.sanctionservice.exceptions;

import org.springframework.http.HttpStatus;

public class SancionException extends RuntimeException {

    private final HttpStatus status;

    public SancionException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public SancionException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
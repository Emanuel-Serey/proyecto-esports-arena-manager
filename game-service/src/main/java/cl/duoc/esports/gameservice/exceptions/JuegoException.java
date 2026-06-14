package cl.duoc.esports.gameservice.exceptions;

import org.springframework.http.HttpStatus;

public class JuegoException extends RuntimeException {

    private final HttpStatus status;

    public JuegoException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public JuegoException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
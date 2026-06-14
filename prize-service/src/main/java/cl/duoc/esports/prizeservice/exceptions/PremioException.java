package cl.duoc.esports.prizeservice.exceptions;

import org.springframework.http.HttpStatus;

public class PremioException extends RuntimeException {

    private final HttpStatus status;

    public PremioException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public PremioException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
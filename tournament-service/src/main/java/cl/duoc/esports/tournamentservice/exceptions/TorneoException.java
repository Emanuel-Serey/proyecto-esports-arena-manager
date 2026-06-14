package cl.duoc.esports.tournamentservice.exceptions;

import org.springframework.http.HttpStatus;

public class TorneoException extends RuntimeException {

    private final HttpStatus status;

    public TorneoException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public TorneoException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
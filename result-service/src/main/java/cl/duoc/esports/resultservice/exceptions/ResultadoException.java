package cl.duoc.esports.resultservice.exceptions;

import org.springframework.http.HttpStatus;

public class ResultadoException extends RuntimeException {

    private final HttpStatus status;

    public ResultadoException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public ResultadoException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
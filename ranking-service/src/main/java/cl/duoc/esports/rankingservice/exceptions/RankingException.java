package cl.duoc.esports.rankingservice.exceptions;

import org.springframework.http.HttpStatus;

public class RankingException extends RuntimeException {

    private final HttpStatus status;

    public RankingException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public RankingException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
package it.unical.ea.Travel.Exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String messageKey;

    public ApiException(HttpStatus status, String messageKey) {
        super(messageKey);
        this.status = status;
        this.messageKey = messageKey;
    }
}

package it.unical.ea.Travel.Exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends ApiException {
    public UnauthorizedAccessException(String messageKey) {
        super(HttpStatus.FORBIDDEN, messageKey);
    }
}

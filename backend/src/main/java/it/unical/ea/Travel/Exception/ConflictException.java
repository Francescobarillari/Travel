package it.unical.ea.Travel.Exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {
    public ConflictException(String messageKey) {
        super(HttpStatus.CONFLICT, messageKey);
    }
}

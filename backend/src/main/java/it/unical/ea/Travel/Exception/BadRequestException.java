package it.unical.ea.Travel.Exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {
    public BadRequestException(String messageKey) {
        super(HttpStatus.BAD_REQUEST, messageKey);
    }
}

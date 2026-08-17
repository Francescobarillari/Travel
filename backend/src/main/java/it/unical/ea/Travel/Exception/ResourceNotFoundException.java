package it.unical.ea.Travel.Exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String messageKey) {
        super(HttpStatus.NOT_FOUND, messageKey);
    }
}

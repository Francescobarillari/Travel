package it.unical.ea.Travel.Services.keycloak;

import it.unical.ea.Travel.Exception.ConflictException;

public class KeycloakUserAlreadyExistsException extends ConflictException {

    public KeycloakUserAlreadyExistsException(String messageKey) {
        super(messageKey != null && !messageKey.isBlank() ? messageKey : "auth.signup.emailAlreadyExists");
    }
}

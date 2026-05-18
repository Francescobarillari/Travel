package it.unical.ea.Travel.Services.keycloak;

public class KeycloakUserAlreadyExistsException extends RuntimeException {

    public KeycloakUserAlreadyExistsException(String message) {
        super(message);
    }
}

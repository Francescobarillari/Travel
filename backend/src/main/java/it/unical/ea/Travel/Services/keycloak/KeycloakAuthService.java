package it.unical.ea.Travel.Services.keycloak;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import it.unical.ea.dtos.authDto.LoginRequest;

@Service
public class KeycloakAuthService {

    private final RestClient restClient;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    public KeycloakAuthService(
            RestClient.Builder restClientBuilder,
            @Value("${keycloak.server-url}") String serverUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.app-client-id}") String clientId,
            @Value("${keycloak.app-client-secret}") String clientSecret) {
        this.restClient = restClientBuilder.baseUrl(serverUrl).build();
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String login(LoginRequest request) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", request.getEmail());
        body.add("password", request.getPassword());

        try {
            Map<?, ?> response = restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            Object accessToken = response == null ? null : response.get("access_token");
            if (accessToken instanceof String token) {
                return token;
            }
            throw new IllegalStateException("Keycloak non ha restituito access_token");
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest exception) {
            throw new BadCredentialsException("Credenziali Keycloak non valide", exception);
        }
    }
}

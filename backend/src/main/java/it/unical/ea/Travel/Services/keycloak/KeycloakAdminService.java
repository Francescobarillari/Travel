package it.unical.ea.Travel.Services.keycloak;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import it.unical.ea.dtos.authDto.SignupRequest;
import it.unical.ea.enums.UserType;

@Service
public class KeycloakAdminService {

    private final RestClient restClient;
    private final String realm;
    private final String adminClientId;
    private final String adminClientSecret;
    private final String appClientId;
    private final String defaultClientRole;

    public KeycloakAdminService(
            RestClient.Builder restClientBuilder,
            @Value("${keycloak.server-url}") String serverUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.admin-client-id}") String adminClientId,
            @Value("${keycloak.admin-client-secret}") String adminClientSecret,
            @Value("${keycloak.app-client-id}") String appClientId,
            @Value("${keycloak.default-client-role}") String defaultClientRole) {
        this.restClient = restClientBuilder.baseUrl(serverUrl).build();
        this.realm = realm;
        this.adminClientId = adminClientId;
        this.adminClientSecret = adminClientSecret;
        this.appClientId = appClientId;
        this.defaultClientRole = defaultClientRole;
    }

    public String createUser(SignupRequest request) {
        String token = getAdminAccessToken();

        String firstName = "";
        String lastName = "";
        if (request.getUserType() == UserType.VIAGGIATORE) {
            firstName = request.getFirstName() != null ? request.getFirstName() : "";
            lastName = request.getLastName() != null ? request.getLastName() : "";
        } else if (request.getUserType() == UserType.SOCIETA) {
            firstName = "Società";
            lastName = request.getCompanyName() != null ? request.getCompanyName() : "";
        }

        Map<String, Object> userRepresentation = Map.of(
                "username", request.getEmail(),
                "email", request.getEmail(),
                "firstName", firstName,
                "lastName", lastName,
                "enabled", true,
                "emailVerified", false,
                "requiredActions", List.of(),
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", request.getPassword(),
                        "temporary", false)));

        try {
            ResponseEntity<Void> response = restClient.post()
                    .uri("/admin/realms/{realm}/users", realm)
                    .header("Authorization", "Bearer " + token)
                    .body(userRepresentation)
                    .retrieve()
                    .toBodilessEntity();

            String userId = extractUserId(response.getHeaders().getLocation());
            assignDefaultClientRole(token, userId);
            return userId;
        } catch (HttpClientErrorException.Conflict exception) {
            throw new KeycloakUserAlreadyExistsException("Utente gia presente in Keycloak");
        }
    }

    public void deleteUser(String keycloakUserId) {
        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            return;
        }

        try {
            restClient.delete()
                    .uri("/admin/realms/{realm}/users/{userId}", realm, keycloakUserId)
                    .header("Authorization", "Bearer " + getAdminAccessToken())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ignored) {
            // Best-effort rollback: il chiamante sta gia gestendo il fallimento DB.
        }
    }

    public void disableUser(String keycloakUserId) {
        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            return; // oppure lancia eccezione, a seconda delle tue policy
        }
        String token = getAdminAccessToken();
        Map<String, Object> payload = Map.of("enabled", false);
        try {
            restClient.put()
                    .uri("/admin/realms/{realm}/users/{userId}", realm, keycloakUserId)
                    .header("Authorization", "Bearer " + token)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la disabilitazione dell'utente Keycloak con ID: " + keycloakUserId, e);
        }
    }

    public void updateUserPassword(String email, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return;
        }

        String token = getAdminAccessToken();

        try {
            // 1. Cerca l'utente per email
            List<?> users = restClient.get()
                    .uri("/admin/realms/{realm}/users?email={email}", realm, email)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(List.class);

            if (users == null || users.isEmpty()) {
                throw new IllegalStateException("Utente non trovato in Keycloak: " + email);
            }

            // Sicurezza extra: verifica corrispondenza esatta dell'email per evitare fuzzy
            // matching
            Map<?, ?> matchingUser = null;
            for (Object obj : users) {
                if (obj instanceof Map<?, ?> uMap) {
                    if (email.equalsIgnoreCase((String) uMap.get("email"))) {
                        matchingUser = uMap;
                        break;
                    }
                }
            }

            if (matchingUser == null) {
                throw new IllegalStateException(
                        "Nessun utente con email corrispondente esatta trovato in Keycloak: " + email);
            }

            String userId = (String) matchingUser.get("id");
            if (userId == null || userId.isBlank()) {
                throw new IllegalStateException("ID utente Keycloak nullo o non valido");
            }

            // 2. Esegui il reset della password in Keycloak
            Map<String, Object> credential = Map.of(
                    "type", "password",
                    "value", newPassword,
                    "temporary", false);

            restClient.put()
                    .uri("/admin/realms/{realm}/users/{userId}/reset-password", realm, userId)
                    .header("Authorization", "Bearer " + token)
                    .body(credential)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'aggiornamento della password in Keycloak", e);
        }
    }

    private String getAdminAccessToken() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", adminClientId);
        body.add("client_secret", adminClientSecret);

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
        throw new IllegalStateException("Keycloak admin token non disponibile");
    }

    private void assignDefaultClientRole(String token, String userId) {
        String clientUuid = getClientUuid(token);
        Map<?, ?> role = restClient.get()
                .uri("/admin/realms/{realm}/clients/{clientUuid}/roles/{role}", realm, clientUuid, defaultClientRole)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

        restClient.post()
                .uri("/admin/realms/{realm}/users/{userId}/role-mappings/clients/{clientUuid}", realm, userId,
                        clientUuid)
                .header("Authorization", "Bearer " + token)
                .body(List.of(role))
                .retrieve()
                .toBodilessEntity();
    }

    private String getClientUuid(String token) {
        List<?> clients = restClient.get()
                .uri("/admin/realms/{realm}/clients?clientId={clientId}", realm, appClientId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(List.class);

        if (clients == null || clients.isEmpty() || !(clients.getFirst() instanceof Map<?, ?> client)) {
            throw new IllegalStateException("Client Keycloak non trovato: " + appClientId);
        }

        Object id = client.get("id");
        if (id instanceof String clientUuid) {
            return clientUuid;
        }
        throw new IllegalStateException("Client Keycloak senza id: " + appClientId);
    }

    private String extractUserId(URI location) {
        if (location == null) {
            throw new IllegalStateException("Keycloak non ha restituito Location per il nuovo utente");
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}

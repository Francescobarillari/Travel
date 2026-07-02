package it.unical.ea.Travel.Services.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import java.util.Map;

/**
 * Servizio per verificare i token CAPTCHA tramite l'API di Google reCAPTCHA.
 */
@Service
public class CaptchaService {

    private final RestClient restClient;
    private final String secretKey;

    public CaptchaService(
            RestClient.Builder restClientBuilder,
            @Value("${google.recaptcha.secret-key:6Le1E0EtAAAAALWoQm7pXWN7ITD8tNFFtoLb5bc9}") String secretKey) {
        this.restClient = restClientBuilder.baseUrl("https://www.google.com").build();
        this.secretKey = secretKey;
    }

    /**
     * Verifica il token CAPTCHA inviato dal client.
     * @param token il token CAPTCHA.
     * @return true se il token è valido, false altrimenti.
     */
    public boolean verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            Map<?, ?> response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/recaptcha/api/siteverify")
                            .queryParam("secret", secretKey)
                            .queryParam("response", token)
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (response != null) {
                Object success = response.get("success");
                if (success instanceof Boolean) {
                    return (Boolean) success;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}

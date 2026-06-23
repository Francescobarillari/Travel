package it.unical.ea.Travel.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configurazione di sicurezza per il profilo "dev".
 * Permette tutte le richieste senza autenticazione,
 * utile per testare gli endpoint tramite Swagger senza token.
 *
 * Attivare con: --spring.profiles.active=dev
 */
@Configuration
@EnableWebSecurity
@Profile("dev")
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // Disabilita OAuth2 resource server in dev per evitare errori se Keycloak non è raggiungibile
            .oauth2ResourceServer(oauth2 -> oauth2.disable());

        return http.build();
    }
}

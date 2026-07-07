package it.unical.ea.Travel.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configurazione principale della sicurezza dell'applicazione.
 * L'applicazione valida i Bearer token emessi da Keycloak tramite OAuth2 Resource Server.
 */
@Configuration
@EnableWebSecurity
@Profile("!dev")
public class SecurityConfig {

    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(RateLimitFilter rateLimitFilter) {
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()

                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/itinerary/*/image").permitAll()
                .requestMatchers(HttpMethod.GET, "/activity/images/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/user/*/avatar").permitAll()
                .requestMatchers("/api/basic").hasRole("BASIC")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()              
            )
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        addRoles(authorities, jwt.getClaimAsStringList("roles"));

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            Object clientAccess = resourceAccess.get("ae-client");
            if (clientAccess instanceof Map<?, ?> clientAccessMap) {
                Object roles = clientAccessMap.get("roles");
                if (roles instanceof Collection<?> roleCollection) {
                    addRoles(authorities, roleCollection.stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .toList());
                }
            }
        }

        return authorities;
    }

    private void addRoles(List<GrantedAuthority> authorities, Collection<String> roles) {
        if (roles == null) {
            return;
        }

        roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
    }
}

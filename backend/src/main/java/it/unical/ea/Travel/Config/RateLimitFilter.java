package it.unical.ea.Travel.Config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    /** Numero massimo di richieste consentite nella finestra temporale */
    private static final int MAX_REQUESTS = 5;

    /** Periodo di refill: ogni REFILL_PERIOD tutti i token vengono ripristinati */
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    private static final String LOGIN_PATH = "/api/auth/login";

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!LOGIN_PATH.equals(request.getRequestURI()) || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createBucket());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long retryAfterSeconds = Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds() + 1;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setHeader("X-Rate-Limit-Remaining", "0");
            response.getWriter().write(
                    "{\"error\":\"Troppi tentativi di login. Riprova tra " + retryAfterSeconds + " secondi.\"}");
        }
    }

    /**
     * Crea un nuovo bucket con la bandwidth configurata.
     * Intervallo-based refill: ogni REFILL_PERIOD il bucket torna a MAX_REQUESTS token.
     */
    private Bucket createBucket() {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(MAX_REQUESTS)
                .refillIntervallyAligned(MAX_REQUESTS, REFILL_PERIOD, java.time.Instant.now())
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    /**
     * Risolve l'IP reale del client, considerando eventuali proxy/load balancer.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

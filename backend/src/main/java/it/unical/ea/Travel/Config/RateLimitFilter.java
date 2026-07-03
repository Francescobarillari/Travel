package it.unical.ea.Travel.Config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 5;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    private static final int UPLOAD_MAX_REQUESTS = 20;
    private static final Duration UPLOAD_REFILL_PERIOD = Duration.ofHours(1);

    private final Cache<String, Bucket> loginBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(10000)
            .build();

    private final Cache<String, Bucket> uploadBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(2))
            .maximumSize(10000)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isLogin = "/api/auth/login".equals(path) && "POST".equalsIgnoreCase(request.getMethod());
        boolean isUpload = "/api/auth/upload-document".equals(path) && "POST".equalsIgnoreCase(request.getMethod());

        if (!isLogin && !isUpload) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        Bucket bucket;
        if (isLogin) {
            bucket = loginBuckets.get(clientIp, k -> createBucket(MAX_REQUESTS, REFILL_PERIOD));
        } else {
            bucket = uploadBuckets.get(clientIp, k -> createBucket(UPLOAD_MAX_REQUESTS, UPLOAD_REFILL_PERIOD));
        }

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
                    "{\"error\":\"Troppe richieste. Riprova tra " + retryAfterSeconds + " secondi.\"}");
        }
    }

    private Bucket createBucket(int capacity, Duration refillPeriod) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervallyAligned(capacity, refillPeriod, java.time.Instant.now())
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        // Usa solo l'indirizzo remoto effettivo (proxy o client), ignorando X-Forwarded-For per 
        // evitare IP Spoofing, a meno che non ci sia una configurazione esplicita per proxy trusted.
        return request.getRemoteAddr();
    }
}

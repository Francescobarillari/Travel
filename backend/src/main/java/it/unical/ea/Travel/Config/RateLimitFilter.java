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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_LOGIN_REQUESTS = 5;
    private static final Duration LOGIN_REFILL_PERIOD = Duration.ofMinutes(1);
    private static final String LOGIN_PATH = "/api/auth/login";

    private static final int MAX_UPLOAD_REQUESTS = 3;
    private static final Duration UPLOAD_REFILL_PERIOD = Duration.ofMinutes(1);

    @Value("${app.security.trust-proxy:false}")
    private boolean trustProxy;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("security-audit");

    // Caffeine cache handles automatic expiration, preventing memory leaks
    private final Cache<String, Bucket> loginBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(10000)
            .build();

    private final Cache<String, Bucket> uploadBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(10000)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        boolean isLogin = LOGIN_PATH.equals(uri) && "POST".equalsIgnoreCase(request.getMethod());
        boolean isUpload = isUploadRequest(request, uri);

        if (!isLogin && !isUpload) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);

        if (isLogin) {
            Bucket bucket = loginBuckets.get(clientIp, k -> createBucket(MAX_LOGIN_REQUESTS, LOGIN_REFILL_PERIOD));
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                long retryAfterSeconds = Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds() + 1;
                logger.warn("RATE_LIMIT_BLOCKED - Login rate limit reached for IP: {}. URI: {}", clientIp, request.getRequestURI());
                sendErrorResponse(response, retryAfterSeconds, "Troppi tentativi di login. Riprova tra " + retryAfterSeconds + " secondi.");
            }
        } else {
            Bucket bucket = uploadBuckets.get(clientIp, k -> createBucket(MAX_UPLOAD_REQUESTS, UPLOAD_REFILL_PERIOD));
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                long retryAfterSeconds = Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds() + 1;
                logger.warn("RATE_LIMIT_BLOCKED - Upload rate limit reached for IP: {}. URI: {}", clientIp, request.getRequestURI());
                sendErrorResponse(response, retryAfterSeconds, "Troppi tentativi di upload. Riprova tra " + retryAfterSeconds + " secondi.");
            }
        }
    }

    private boolean isUploadRequest(HttpServletRequest request, String uri) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        return uri.matches("^/user/[^/]+/avatar$") ||
               uri.matches("^/itinerary/[^/]+/image$") ||
               "/api/auth/upload-document".equals(uri) ||
               uri.matches("^/activity/[^/]+/images$");
    }

    private Bucket createBucket(int maxRequests, Duration refillPeriod) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(maxRequests)
                .refillIntervallyAligned(maxRequests, refillPeriod, java.time.Instant.now())
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    private void sendErrorResponse(HttpServletResponse response, long retryAfterSeconds, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setHeader("X-Rate-Limit-Remaining", "0");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (trustProxy) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}

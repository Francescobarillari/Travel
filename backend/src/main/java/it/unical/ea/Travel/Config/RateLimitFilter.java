package it.unical.ea.Travel.Config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_LOGIN_REQUESTS = 5;
    private static final Duration LOGIN_REFILL_PERIOD = Duration.ofMinutes(1);
    private static final String LOGIN_PATH = "/api/auth/login";

    private static final int MAX_UPLOAD_REQUESTS = 3;
    private static final Duration UPLOAD_REFILL_PERIOD = Duration.ofMinutes(1);

    @Value("${app.security.trust-proxy:false}")
    private boolean trustProxy;

    private static class CachedBucket {
        private final Bucket bucket;
        private long lastAccessTime;

        public CachedBucket(Bucket bucket) {
            this.bucket = bucket;
            this.lastAccessTime = System.currentTimeMillis();
        }

        public Bucket getBucket() {
            this.lastAccessTime = System.currentTimeMillis();
            return bucket;
        }

        public boolean isExpired(long idleTimeoutMs) {
            return System.currentTimeMillis() - lastAccessTime > idleTimeoutMs;
        }
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("security-audit");

    private final ConcurrentHashMap<String, CachedBucket> loginBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CachedBucket> uploadBuckets = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "rate-limit-cleaner");
        thread.setDaemon(true);
        return thread;
    });

    @PostConstruct
    public void init() {
        scheduler.scheduleWithFixedDelay(this::cleanupBuckets, 10, 10, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void destroyScheduler() {
        scheduler.shutdown();
    }

    private void cleanupBuckets() {
        long idleTimeoutMs = 10 * 60 * 1000; // 10 minutes
        loginBuckets.entrySet().removeIf(entry -> entry.getValue().isExpired(idleTimeoutMs));
        uploadBuckets.entrySet().removeIf(entry -> entry.getValue().isExpired(idleTimeoutMs));
    }

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
            CachedBucket cachedBucket = loginBuckets.computeIfAbsent(clientIp, k -> new CachedBucket(createBucket(MAX_LOGIN_REQUESTS, LOGIN_REFILL_PERIOD)));
            ConsumptionProbe probe = cachedBucket.getBucket().tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                long retryAfterSeconds = Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds() + 1;
                logger.warn("RATE_LIMIT_BLOCKED - Login rate limit reached for IP: {}. URI: {}", clientIp, request.getRequestURI());
                sendErrorResponse(response, retryAfterSeconds, "Troppi tentativi di login. Riprova tra " + retryAfterSeconds + " secondi.");
            }
        } else {
            CachedBucket cachedBucket = uploadBuckets.computeIfAbsent(clientIp, k -> new CachedBucket(createBucket(MAX_UPLOAD_REQUESTS, UPLOAD_REFILL_PERIOD)));
            ConsumptionProbe probe = cachedBucket.getBucket().tryConsumeAndReturnRemaining(1);

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

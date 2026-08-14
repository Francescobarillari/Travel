package it.unical.ea.Travel.Services.auth;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unical.ea.Travel.Exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpService {

    private static final int MAX_ATTEMPTS = 5;

    private final Cache<String, String> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(5000)
            .build();

    private final Cache<String, Integer> attemptCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(5000)
            .build();

    private final SecureRandom random = new SecureRandom();

    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpCache.put(email, otp);
        attemptCache.invalidate(email);
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        Integer attempts = attemptCache.getIfPresent(email);
        int currentAttempts = (attempts != null) ? attempts : 0;

        if (currentAttempts >= MAX_ATTEMPTS) {
            otpCache.invalidate(email);
            attemptCache.invalidate(email);
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "auth.forgot-password.maxAttemptsExceeded");
        }

        String cachedOtp = otpCache.getIfPresent(email);
        if (cachedOtp == null) {
            return false;
        }

        if (cachedOtp.equals(otp)) {
            otpCache.invalidate(email);
            attemptCache.invalidate(email);
            return true;
        }

        currentAttempts++;
        attemptCache.put(email, currentAttempts);

        if (currentAttempts >= MAX_ATTEMPTS) {
            otpCache.invalidate(email);
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "auth.forgot-password.maxAttemptsExceeded");
        }

        return false;
    }
}

package it.unical.ea.Travel.Services.auth;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpService {

    private final Cache<String, String> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(5000)
            .build();

    private final SecureRandom random = new SecureRandom();

    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpCache.put(email, otp);
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        String cachedOtp = otpCache.getIfPresent(email);
        if (cachedOtp != null && cachedOtp.equals(otp)) {
            otpCache.invalidate(email);
            return true;
        }
        return false;
    }
}

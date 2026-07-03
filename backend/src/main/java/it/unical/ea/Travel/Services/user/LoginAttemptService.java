package it.unical.ea.Travel.Services.user;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servizio per tracciare i tentativi di login falliti in memoria.
 * I tentativi falliti scadono dopo 10 minuti di inattività per lo specifico utente.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long EXPIRE_TIME_MS = 10 * 60 * 1000; // 10 minuti

    private static class Attempt {
        private int count;
        private long lastModified;

        public Attempt(int count) {
            this.count = count;
            this.lastModified = System.currentTimeMillis();
        }

        public void increment() {
            this.count++;
            this.lastModified = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - lastModified > EXPIRE_TIME_MS;
        }
    }

    private final ConcurrentHashMap<String, Attempt> attemptsCache = new ConcurrentHashMap<>();

    public void loginFailed(String email) {
        if (email == null) return;
        attemptsCache.compute(email.trim().toLowerCase(), (key, val) -> {
            if (val == null || val.isExpired()) {
                return new Attempt(1);
            } else {
                val.increment();
                return val;
            }
        });
    }

    public void loginSucceeded(String email) {
        if (email == null) return;
        attemptsCache.remove(email.trim().toLowerCase());
    }

    public boolean isCaptchaRequired(String email) {
        if (email == null) return false;
        Attempt val = attemptsCache.get(email.trim().toLowerCase());
        if (val == null) {
            return false;
        }
        if (val.isExpired()) {
            attemptsCache.remove(email.trim().toLowerCase());
            return false;
        }
        return val.count >= MAX_ATTEMPTS;
    }
}

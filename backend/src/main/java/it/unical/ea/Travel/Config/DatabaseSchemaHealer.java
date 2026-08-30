package it.unical.ea.Travel.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class DatabaseSchemaHealer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            log.info("Verifica e allineamento vincoli schema PostgreSQL...");
            jdbcTemplate.execute("ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;");
            log.info("Vincoli schema PostgreSQL allineati con successo.");
        } catch (Exception e) {
            log.warn("Nota: Impossibile aggiornare i vincoli di schema: {}", e.getMessage());
        }
    }
}

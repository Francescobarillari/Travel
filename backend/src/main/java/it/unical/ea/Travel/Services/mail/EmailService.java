package it.unical.ea.Travel.Services.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@travel.com}")
    private String fromEmail;

    public void sendWelcomeEmail(String toEmail, String name, boolean isCompany) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail != null && !fromEmail.isBlank() ? fromEmail : "noreply@travel.com");
            helper.setTo(toEmail);
            helper.setSubject("Benvenuto su Travel!");
            
            String content;
            if (isCompany) {
                content = "<h1>Benvenuto su Travel!</h1>" +
                          "<p>Ciao <strong>" + name + "</strong>,</p>" +
                          "<p>Grazie per esserti registrato come Società sulla nostra piattaforma.</p>" +
                          "<p>La tua richiesta è in attesa di approvazione da parte di un amministratore. Ti invieremo un'ulteriore conferma non appena l'account sarà attivo.</p>" +
                          "<br><p>Il team di Travel</p>";
            } else {
                content = "<h1>Benvenuto su Travel!</h1>" +
                          "<p>Ciao <strong>" + name + "</strong>,</p>" +
                          "<p>Siamo felici di averti a bordo! Il tuo account è ora attivo ed è pronto per essere utilizzato.</p>" +
                          "<p>Inizia subito a esplorare nuovi itinerari e attività.</p>" +
                          "<br><p>Il team di Travel</p>";
            }
            
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Errore durante l'invio della mail di benvenuto", e);
        }
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail != null && !fromEmail.isBlank() ? fromEmail : "noreply@travel.com");
            helper.setTo(toEmail);
            helper.setSubject("Recupero Password - Codice OTP");
            
            String content = "<h1>Recupero Password</h1>" +
                             "<p>Hai richiesto il recupero della password per il tuo account su Travel.</p>" +
                             "<p>Il tuo codice OTP è: <strong style='font-size: 20px; color: #007bff;'>" + otpCode + "</strong></p>" +
                             "<p>Questo codice è valido per 10 minuti. Non condividerlo con nessuno.</p>" +
                             "<br><p>Il team di Travel</p>";
            
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Errore durante l'invio della mail OTP", e);
        }
    }
}

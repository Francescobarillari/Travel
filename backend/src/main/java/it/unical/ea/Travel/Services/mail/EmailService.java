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
            helper.setFrom(fromEmail != null && !fromEmail.isBlank() ? fromEmail : "noreply@derive.com");
            helper.setTo(toEmail);
            helper.setSubject("Benvenuto su Dèrive!");
            
            String content;
            if (isCompany) {
                content = "<h1>Benvenuto su Dèrive!</h1>" +
                          "<p>Ciao <strong>" + name + "</strong>,</p>" +
                          "<p>Grazie per esserti registrato come Agenzia sulla nostra piattaforma.</p>" +
                          "<p>Ti confermiamo che il tuo indirizzo email è stato verificato con successo.</p>" +
                          "<p><strong>Importante:</strong> La tua richiesta è ora in attesa di approvazione da parte di un amministratore. Il tuo account deve essere espressamente approvato dall'admin prima che tu possa effettuare l'accesso ed utilizzare le funzionalità dell'applicazione. Ti invieremo un'ulteriore conferma via email non appena l'account sarà attivo.</p>" +
                          "<br><p>Il team di Dèrive</p>";
            } else {
                content = "<h1>Benvenuto su Dèrive!</h1>" +
                          "<p>Ciao <strong>" + name + "</strong>,</p>" +
                          "<p>Siamo felici di averti a bordo! Il tuo account è ora attivo ed è pronto per essere utilizzato.</p>" +
                          "<p>Inizia subito a esplorare nuovi itinerari e attività.</p>" +
                          "<br><p>Il team di Dèrive</p>";
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

    public void sendCompanyApprovedEmail(String toEmail, String companyName) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail != null && !fromEmail.isBlank() ? fromEmail : "noreply@derive.com");
            helper.setTo(toEmail);
            helper.setSubject("Account Approvato - Benvenuto su Dèrive!");
            
            String content = "<h1>Account Approvato!</h1>" +
                             "<p>Ciao <strong>" + companyName + "</strong>,</p>" +
                             "<p>Siamo felici di comunicarti che la tua richiesta di registrazione come Agenzia su Dèrive è stata <strong>approvata</strong> da un amministratore.</p>" +
                             "<p>Da questo momento puoi effettuare l'accesso nell'applicazione e iniziare a pubblicare i tuoi itinerari e attività.</p>" +
                             "<br><p>Il team di Dèrive</p>";
            
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Errore durante l'invio della mail di approvazione agenzia", e);
        }
    }

    public void sendCompanyRejectedEmail(String toEmail, String companyName) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail != null && !fromEmail.isBlank() ? fromEmail : "noreply@derive.com");
            helper.setTo(toEmail);
            helper.setSubject("Registrazione non approvata - Dèrive");
            
            String content = "<h1>Registrazione non approvata</h1>" +
                             "<p>Ciao <strong>" + companyName + "</strong>,</p>" +
                             "<p>Ti informiamo che, in seguito alla verifica dei documenti caricati, la tua richiesta di registrazione come Agenzia su Dèrive è stata <strong>rifiutata</strong>.</p>" +
                             "<p>Se ritieni che ci sia stato un errore o se desideri ricevere maggiori chiarimenti, puoi contattare il nostro supporto rispondendo a questa email.</p>" +
                             "<br><p>Il team di Dèrive</p>";
            
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Errore durante l'invio della mail di rifiuto agenzia", e);
        }
    }
}

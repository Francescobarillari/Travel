package it.unical.ea.Travel.Controllers.admin;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Mappers.activity.ActivityMapper;
import it.unical.ea.Travel.Mappers.user.UserMapper;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
import it.unical.ea.Travel.Repositories.activity.ActivityTemplateRepository;
import it.unical.ea.Travel.Repositories.itinerary.ItineraryRepository;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Services.audit.AuditLogService;
import it.unical.ea.Travel.Services.activity.ActivityService;
import it.unical.ea.Travel.Services.keycloak.KeycloakAdminService;
import it.unical.ea.dtos.activity.ActivityDto;
import it.unical.ea.dtos.user.UserDTO;
import it.unical.ea.enums.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import it.unical.ea.Travel.Services.mail.EmailService;
import it.unical.ea.Travel.Services.storage.FileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Dashboard e moderazione amministratore")
public class AdminController {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ActivityTemplateRepository activityTemplateRepository;
    private final ItineraryRepository itineraryRepository;
    private final ActivityService activityService;
    private final KeycloakAdminService keycloakAdminService;
    private final FileStorageService fileStorageService;
    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;
    private final AuditLogService auditLogService;

    @Operation(summary = "Ottieni agenzie in attesa di approvazione")
    @GetMapping("/companies/pending")
    public List<UserDTO> getPendingCompanies() {
        List<User> pending = userRepository.findByUserTypeAndApproved(UserType.SOCIETA, false);
        return pending.stream().map(userMapper::toDTO).toList();
    }

    @Operation(summary = "Approva un'agenzia")
    @PostMapping("/companies/{id}/approve")
    public ResponseEntity<Void> approveCompany(@PathVariable String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
        user.setApproved(true);
        userRepository.save(user);
        
        // Invia notifica di approvazione
        notificationService.createNotification(
            user,
            "Account Approvato",
            "La tua agenzia è stata approvata dall'amministratore. Ora puoi iniziare a pubblicare offerte!",
            it.unical.ea.enums.NotificationType.APPROVAZIONE_SOCIETA
        );

        auditLogService.log("APPROVE_COMPANY", "User", id, "Approved company user with email: " + user.getEmail());
        
        // Invia email di conferma approvazione all'agenzia
        try {
            emailService.sendCompanyApprovedEmail(user.getEmail(), user.getCompanyName());
        } catch (Exception e) {
            System.err.println("Errore nell'invio della mail di approvazione: " + e.getMessage());
        }
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Rifiuta ed elimina un'agenzia")
    @PostMapping("/companies/{id}/reject")
    public ResponseEntity<Void> rejectCompany(@PathVariable String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
        
        String email = user.getEmail();
        String companyName = user.getCompanyName();
        String keycloakId = user.getKeycloakId();
        
        // Invia email di notifica rifiuto all'agenzia prima di eliminare il record dal DB
        try {
            emailService.sendCompanyRejectedEmail(email, companyName);
        } catch (Exception e) {
            System.err.println("Errore nell'invio della mail di rifiuto: " + e.getMessage());
        }
        
        // Elimina l'utente da Keycloak
        keycloakAdminService.deleteUser(keycloakId);
        
        // Elimina l'utente dal database locale
        userRepository.delete(user);
        auditLogService.log("REJECT_COMPANY", "User", id, "Rejected and deleted company user with email: " + email);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Ottieni attività in attesa di approvazione")
    @GetMapping("/activities/pending")
    public List<ActivityDto> getPendingActivities() {
        List<Activity> pending = activityRepository.findByTemplateApproved(false);
        
        // Raggruppa le sessioni per ID del template
        java.util.Map<UUID, java.util.List<Activity>> sessionsByTemplate = new java.util.HashMap<>();
        for (Activity act : pending) {
            if (act.getTemplate() != null) {
                sessionsByTemplate.computeIfAbsent(act.getTemplate().getId(), k -> new java.util.ArrayList<>()).add(act);
            }
        }
        
        List<ActivityDto> dtos = new java.util.ArrayList<>();
        for (java.util.List<Activity> sessions : sessionsByTemplate.values()) {
            if (sessions.isEmpty()) continue;
            
            // Trova la sessione con la data di inizio minima e quella con la data di fine massima
            Activity minAct = sessions.get(0);
            Activity maxAct = sessions.get(0);
            for (Activity act : sessions) {
                if (act.getStartTime().isBefore(minAct.getStartTime())) {
                    minAct = act;
                }
                if (act.getEndTime().isAfter(maxAct.getEndTime())) {
                    maxAct = act;
                }
            }
            
            // Crea il DTO basandosi sulla sessione minima
            ActivityDto dto = activityMapper.toDTO(minAct);
            // Imposta l'intervallo completo della ricorrenza
            dto.setStartTime(minAct.getStartTime());
            dto.setEndTime(maxAct.getEndTime());
            dto.setCurrentParticipants(activityService.calculateCurrentParticipants(minAct));
            dtos.add(dto);
        }
        return dtos;
    }

    @Operation(summary = "Approva un'attività")
    @PostMapping("/activities/{id}/approve")
    public ResponseEntity<Void> approveActivity(@PathVariable String id, @RequestParam(required = false, defaultValue = "true") Boolean approved) {
        UUID uuid = UUID.fromString(id);
        Activity activity = activityRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Attività non trovata"));

        activity.getTemplate().setApproved(approved);
        activityTemplateRepository.save(activity.getTemplate());
        auditLogService.log("APPROVE_ACTIVITY", "Activity", id, "Admin ha " + (approved ? "approvato" : "rifiutato") + " l'attività: " + activity.getTemplate().getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Rifiuta o cancella un'attività")
    @DeleteMapping("/activities/{id}")
    public ResponseEntity<Void> rejectActivity(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        Activity activity = activityRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Attività non trovata"));

        activityRepository.delete(activity);
        auditLogService.log("DELETE_ACTIVITY_ADMIN", "Activity", id, "Admin ha eliminato l'attività: " + activity.getTemplate().getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Scarica un documento di registrazione dell'agenzia", description = "Restituisce l'immagine inline. Endpoint protetto dell'admin.")
    @GetMapping("/documents/{filename}")
    public ResponseEntity<Resource> getDocument(@PathVariable String filename) throws IOException {
        Resource resource = fileStorageService.load("companies/documents/" + filename);

        String contentType = Files.probeContentType(Path.of(resource.getFilename()));
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Ottieni tutte le agenzie")
    @GetMapping("/companies")
    public List<UserDTO> getAllCompanies() {
        List<User> companies = userRepository.findByUserType(UserType.SOCIETA);
        return companies.stream().map(userMapper::toDTO).toList();
    }

    @Operation(summary = "Blocca un'agenzia")
    @PostMapping("/companies/{id}/block")
    public ResponseEntity<Void> blockCompany(@PathVariable String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
        user.setBlocked(true);
        userRepository.save(user);
        auditLogService.log("BLOCK_COMPANY", "User", id, "Blocked company user: " + user.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Sblocca un'agenzia")
    @PostMapping("/companies/{id}/unblock")
    public ResponseEntity<Void> unblockCompany(@PathVariable String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
        user.setBlocked(false);
        userRepository.save(user);
        auditLogService.log("UNBLOCK_COMPANY", "User", id, "Unblocked company user: " + user.getEmail());
        return ResponseEntity.ok().build();
    }
}

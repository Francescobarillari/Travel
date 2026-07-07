package it.unical.ea.Travel.Controllers.admin;

import it.unical.ea.Travel.Entities.activity.Activity;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Mappers.activity.ActivityMapper;
import it.unical.ea.Travel.Mappers.user.UserMapper;
import it.unical.ea.Travel.Repositories.activity.ActivityRepository;
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
        auditLogService.log("APPROVE_COMPANY", "User", id, "Approved company user with email: " + user.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Rifiuta ed elimina un'agenzia")
    @PostMapping("/companies/{id}/reject")
    public ResponseEntity<Void> rejectCompany(@PathVariable String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
        
        // Elimina l'utente da Keycloak
        keycloakAdminService.deleteUser(user.getKeycloakId());
        
        // Elimina l'utente dal database locale
        userRepository.delete(user);
        auditLogService.log("REJECT_COMPANY", "User", id, "Rejected and deleted company user with email: " + user.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Ottieni attività in attesa di approvazione")
    @GetMapping("/activities/pending")
    public List<ActivityDto> getPendingActivities() {
        List<Activity> pending = activityRepository.findByApproved(false);
        List<ActivityDto> dtos = activityMapper.toDTOList(pending);
        for (int i = 0; i < pending.size(); i++) {
            dtos.get(i).setCurrentParticipants(activityService.calculateCurrentParticipants(pending.get(i)));
        }
        return dtos;
    }

    @Operation(summary = "Approva un'attività")
    @PostMapping("/activities/{id}/approve")
    public ResponseEntity<Void> approveActivity(@PathVariable String id) {
        Activity activity = activityRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));
        activity.setApproved(true);
        activityRepository.save(activity);
        auditLogService.log("APPROVE_ACTIVITY", "Activity", id, "Approved activity: " + activity.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Rifiuta o cancella un'attività")
    @DeleteMapping("/activities/{id}")
    public ResponseEntity<Void> rejectActivity(@PathVariable String id) {
        Activity activity = activityRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "activity.notFound"));
        activityRepository.delete(activity);
        auditLogService.log("REJECT_ACTIVITY", "Activity", id, "Rejected and deleted activity: " + activity.getName());
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

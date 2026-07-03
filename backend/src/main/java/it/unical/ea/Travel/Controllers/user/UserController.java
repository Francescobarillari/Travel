package it.unical.ea.Travel.Controllers.user;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import it.unical.ea.dtos.authDto.SignupRequest;
import it.unical.ea.enums.UserType;

import it.unical.ea.dtos.user.UserDTO;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Mappers.user.UserMapper;
import it.unical.ea.Travel.Services.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User", description = "Gestione degli utenti")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Crea un nuovo utente")
    @PostMapping
    public UserDTO saveUser(@Valid @RequestBody SignupRequest request) {
        User user = userService.saveUser(request);
        return toDTO(user);
    }

    @Operation(summary = "Ottieni un utente per ID")
    @GetMapping("/{stringId}")
    public UserDTO getUser(@Parameter(description = "ID dell'utente", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId){
        User user = userService.getUser(stringId);
        return toDTO(user);
    }

    @Operation(summary = "Ottieni tutti gli utenti")
    @GetMapping
    public List<UserDTO> getUsers() {
        return userService.getUsers().stream()
                .map(this::toDTO)
                .toList();
    }

    @Operation(summary = "Ottieni il profilo dell'utente autenticato", description = "Restituisce i dati dell'utente autenticato tramite il token JWT")
    @GetMapping("/me")
    public UserDTO getMe(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new it.unical.ea.Travel.Exception.ApiException(HttpStatus.UNAUTHORIZED, "auth.login.invalidCredentials");
        }
        String email = jwt.getClaimAsString("email");

        // Gestione speciale per l'utente ADMIN che non risiede nel DB locale
        if (isAdmin(jwt)) {
            UserDTO adminDTO = new UserDTO();
            adminDTO.setEmail(email);
            adminDTO.setFirstName("Admin");
            adminDTO.setLastName("User");
            adminDTO.setFullName("Admin User");
            return adminDTO;
        }

        User user = userService.getUserByEmail(email);
        UserDTO userDTO = toDTO(user);
        userDTO.setPassword(null); // Sicurezza extra: Non restituire mai il campo password nelle risposte
        return userDTO;
    }

    @Operation(summary = "Aggiorna il profilo dell'utente autenticato")
    @PutMapping("/me")
    public UserDTO updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UserDTO userDto) {
        if (jwt == null) {
            throw new it.unical.ea.Travel.Exception.ApiException(HttpStatus.UNAUTHORIZED, "auth.login.invalidCredentials");
        }
        String email = jwt.getClaimAsString("email");
        User updatedUser = userService.updateUser(email, userDto);
        UserDTO result = toDTO(updatedUser);
        result.setPassword(null); // Sicurezza extra: Non restituire mai il campo password nelle risposte
        return result;
    }

    private boolean isAdmin(Jwt jwt) {
        // 1. Controlla claim top-level "roles"
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null && roles.contains("ADMIN")) {
            return true;
        }

        // 2. Controlla standard Keycloak "resource_access.ae-client.roles"
        java.util.Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            Object clientAccess = resourceAccess.get("ae-client");
            if (clientAccess instanceof java.util.Map<?, ?> clientAccessMap) {
                Object clientRoles = clientAccessMap.get("roles");
                if (clientRoles instanceof java.util.Collection<?> roleCollection) {
                    if (roleCollection.contains("ADMIN")) {
                        return true;
                    }
                }
            }
        }

        // 3. Controlla fallback basato sull'email dell'amministratore
        String email = jwt.getClaimAsString("email");
        if ("admin-user@example.com".equals(email)) {
            return true;
        }

        return false;
    }


    @Operation(summary = "Elimina un utente")
    @DeleteMapping("/{stringId}")
    public void deleteUser(@Parameter(description = "ID dell'utente", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId){
        userService.deleteUser(stringId);
    }

    // --- Endpoints Avatar ---

    @Operation(summary = "Carica l'avatar dell'utente", description = "Accetta un file immagine (JPEG, PNG, WebP)")
    @PostMapping(value = "/{stringId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDTO uploadAvatar(
            @Parameter(description = "ID dell'utente", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String stringId,
            @RequestPart("file") MultipartFile file) {
        User updated = userService.uploadAvatar(stringId, file);
        return toDTO(updated);
    }

    @Operation(summary = "Scarica l'avatar dell'utente", description = "Restituisce l'avatar inline. Endpoint pubblico.")
    @GetMapping("/{stringId}/avatar")
    public ResponseEntity<Resource> getAvatar(
            @Parameter(description = "ID dell'utente", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String stringId) throws IOException {
        Resource resource = userService.loadAvatar(stringId);

        String contentType = Files.probeContentType(Path.of(resource.getFilename()));
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Elimina l'avatar dell'utente")
    @DeleteMapping("/{stringId}/avatar")
    public UserDTO deleteAvatar(
            @Parameter(description = "ID dell'utente", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String stringId) {
        User updated = userService.deleteAvatar(stringId);
        return toDTO(updated);
    }

    // --- Helpers per arricchire URL ---

    private UserDTO toDTO(User user) {
        UserDTO dto = userMapper.toDTO(user);
        if (user.getAvatarUrl() != null) {
            String avatarUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/user/")
                    .path(user.getId().toString())
                    .path("/avatar")
                    .toUriString();
            dto.setAvatarUrl(avatarUrl);
        }
        return dto;
    }
}

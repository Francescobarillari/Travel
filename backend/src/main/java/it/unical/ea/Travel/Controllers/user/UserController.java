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

import it.unical.ea.dtos.user.UserPrivateDTO;
import it.unical.ea.dtos.user.UserPublicDTO;
import it.unical.ea.dtos.user.UserDTO;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
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
    public UserPrivateDTO saveUser(@Valid @RequestBody SignupRequest request) {
        User user = userService.saveUser(request);
        return userMapper.toPrivateDTO(user);
    }

    @Operation(summary = "Ottieni un utente per ID (Dati privati se proprietario/admin, altrimenti pubblici)")
    @GetMapping("/{stringId}")
    public ResponseEntity<?> getUser(
            @Parameter(description = "ID dell'utente", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId,
            @AuthenticationPrincipal Jwt jwt) {
        User user = userService.getUser(stringId);
        if (jwt != null) {
            String email = jwt.getClaimAsString("email");
            if (isAdmin(jwt) || (email != null && email.equalsIgnoreCase(user.getEmail()))) {
                UserPrivateDTO privateDto = userMapper.toPrivateDTO(user);
                privateDto.setPassword(null);
                return ResponseEntity.ok(privateDto);
            }
        }
        UserPublicDTO publicDto = userMapper.toPublicDTO(user);
        return ResponseEntity.ok(publicDto);
    }

    @Operation(summary = "Ottieni tutti gli utenti")
    @GetMapping
    public List<UserPrivateDTO> getUsers(@AuthenticationPrincipal Jwt jwt) {
        boolean isAdmin = isAdmin(jwt);
        return userService.getUsers().stream()
                .map(user -> {
                    UserPrivateDTO dto = userMapper.toPrivateDTO(user);
                    dto.setPassword(null);
                    if (!isAdmin) {
                        dto.setPhone(null);
                        dto.setVatNumber(null);
                        dto.setDocumentPhotos(null);
                    }
                    return dto;
                })
                .toList();
    }

    @Operation(summary = "Ottieni il profilo dell'utente autenticato", description = "Restituisce i dati dell'utente autenticato tramite il token JWT")
    @GetMapping("/me")
    public UserPrivateDTO getMe(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new it.unical.ea.Travel.Exception.ApiException(HttpStatus.UNAUTHORIZED, "auth.login.invalidCredentials");
        }
        String email = jwt.getClaimAsString("email");

        // Gestione speciale per l'utente ADMIN che non risiede nel DB locale
        if (isAdmin(jwt)) {
            UserPrivateDTO adminDTO = new UserPrivateDTO();
            adminDTO.setEmail(email);
            adminDTO.setFirstName("Admin");
            adminDTO.setLastName("User");
            adminDTO.setFullName("Admin User");
            return adminDTO;
        }

        User user = userService.getUserByEmail(email);
        UserPrivateDTO userDTO = userMapper.toPrivateDTO(user);
        userDTO.setPassword(null); // Sicurezza extra: Non restituire mai il campo password nelle risposte
        return userDTO;
    }

    @Operation(summary = "Aggiorna il profilo dell'utente autenticato")
    @PutMapping("/me")
    public UserPrivateDTO updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UserPrivateDTO userDto) {
        if (jwt == null) {
            throw new it.unical.ea.Travel.Exception.ApiException(HttpStatus.UNAUTHORIZED, "auth.login.invalidCredentials");
        }
        String email = jwt.getClaimAsString("email");
        User updatedUser = userService.updateUserFromPrivateDTO(email, userDto);
        UserPrivateDTO result = userMapper.toPrivateDTO(updatedUser);
        result.setPassword(null); // Sicurezza extra: Non restituire mai il campo password nelle risposte
        return result;
    }

    private boolean isAdmin(Jwt jwt) {
        if (jwt == null) {
            return false;
        }
        // 1. Controlla claim top-level "roles"
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null && (roles.contains("ADMIN") || roles.contains("ROLE_ADMIN"))) {
            return true;
        }

        // 2. Controlla standard Keycloak "resource_access.ae-client.roles"
        java.util.Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            Object clientAccess = resourceAccess.get("ae-client");
            if (clientAccess instanceof java.util.Map<?, ?> clientAccessMap) {
                Object clientRoles = clientAccessMap.get("roles");
                if (clientRoles instanceof java.util.Collection<?> roleCollection) {
                    if (roleCollection.contains("ADMIN") || roleCollection.contains("ROLE_ADMIN")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void checkUserOwnershipOrAdmin(Jwt jwt, String stringId) {
        if (jwt == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "auth.login.invalidCredentials");
        }
        if (isAdmin(jwt)) {
            return;
        }
        String email = jwt.getClaimAsString("email");
        User user = userService.getUser(stringId);
        if (email == null || !email.equalsIgnoreCase(user.getEmail())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.forbidden");
        }
    }

    @Operation(summary = "Elimina un utente")
    @DeleteMapping("/{stringId}")
    public void deleteUser(
            @Parameter(description = "ID dell'utente", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String stringId,
            @AuthenticationPrincipal Jwt jwt) {
        checkUserOwnershipOrAdmin(jwt, stringId);
        userService.deleteUser(stringId);
    }

    @Operation(summary = "Carica l'avatar dell'utente", description = "Accetta un file immagine (JPEG, PNG, WebP)")
    @PostMapping(value = "/{stringId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserPrivateDTO uploadAvatar(
            @Parameter(description = "ID dell'utente", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String stringId,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {
        checkUserOwnershipOrAdmin(jwt, stringId);
        User updated = userService.uploadAvatar(stringId, file);
        return userMapper.toPrivateDTO(updated);
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
    public UserPrivateDTO deleteAvatar(
            @Parameter(description = "ID dell'utente", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String stringId,
            @AuthenticationPrincipal Jwt jwt) {
        checkUserOwnershipOrAdmin(jwt, stringId);
        User updated = userService.deleteAvatar(stringId);
        return userMapper.toPrivateDTO(updated);
    }

    // --- Helpers per arricchire URL ---

    private UserDTO toDTO(User user) {
        return userMapper.toDTO(user);
    }
}

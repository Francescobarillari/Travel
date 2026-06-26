package it.unical.ea.Travel.Services.user;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.unical.ea.dtos.authDto.SignupRequest;
import it.unical.ea.dtos.user.UserDTO;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Services.keycloak.KeycloakAdminService;
import it.unical.ea.enums.UserType;
import lombok.RequiredArgsConstructor;

import it.unical.ea.Travel.Services.storage.FileStorageService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

@RequiredArgsConstructor
@Service
public class UserService {
    private static final String USERS_AVATARS_SUBDIR = "users/avatars";

    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final FileStorageService fileStorageService;

    @Transactional
    public User saveUser(SignupRequest request) {
        userRepository.getUserByEmail(request.getEmail()).ifPresent(user -> {
            throw new ApiException(HttpStatus.CONFLICT, "auth.signup.emailAlreadyExists");
        });

        // Validazione condizionale a livello di business
        if (request.getUserType() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Il tipo utente è obbligatorio");
        }

        if (request.getUserType() == UserType.VIAGGIATORE) {
            if (request.getFirstName() == null || request.getFirstName().strip().isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Il nome è obbligatorio per i viaggiatori");
            }
            if (request.getLastName() == null || request.getLastName().strip().isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Il cognome è obbligatorio per i viaggiatori");
            }
        } else if (request.getUserType() == UserType.SOCIETA) {
            if (request.getCompanyName() == null || request.getCompanyName().strip().isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "La ragione sociale è obbligatoria per le società");
            }
            if (request.getVatNumber() == null || request.getVatNumber().strip().isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "La Partita IVA è obbligatoria per le società");
            }
        }

        String keycloakUserId = keycloakAdminService.createUser(request);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash("{keycloak}");
        user.setUserType(request.getUserType());
        user.setPhone(request.getPhone());

        if (request.getUserType() == UserType.VIAGGIATORE) {
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setRoles("ROLE_VIAGGIATORE");
        } else {
            user.setCompanyName(request.getCompanyName());
            user.setVatNumber(request.getVatNumber());
            user.setDocumentPhotos(request.getDocumentPhotos());
            user.setRoles("ROLE_SOCIETA");
        }

        try {
            return userRepository.save(user);
        } catch (RuntimeException exception) {
            keycloakAdminService.deleteUser(keycloakUserId);
            throw exception;
        }
    }

    public User getUser(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        return userRepository.findById(uuid).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));
    }

    @Transactional
    public User updateUser(String email, UserDTO userDto) {
        User user = userRepository.getUserByEmail(email).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound"));

        // Validazione sicurezza/business: aggiorna solo i campi adatti al tipo di
        // utente
        if (user.getUserType() == UserType.VIAGGIATORE) {
            if (userDto.getFirstName() != null && !userDto.getFirstName().strip().isEmpty()) {
                user.setFirstName(userDto.getFirstName());
            }
            if (userDto.getLastName() != null && !userDto.getLastName().strip().isEmpty()) {
                user.setLastName(userDto.getLastName());
            }
        } else if (user.getUserType() == UserType.SOCIETA) {
            if (userDto.getCompanyName() != null && !userDto.getCompanyName().strip().isEmpty()) {
                user.setCompanyName(userDto.getCompanyName());
            }
            if (userDto.getVatNumber() != null && !userDto.getVatNumber().strip().isEmpty()) {
                user.setVatNumber(userDto.getVatNumber());
            }
        }

        // Aggiorna telefono se presente
        if (userDto.getPhone() != null) {
            user.setPhone(userDto.getPhone().strip().isEmpty() ? null : userDto.getPhone());
        }

        // Se è specificata una nuova password, validala e aggiornala in Keycloak
        if (userDto.getPassword() != null && !userDto.getPassword().strip().isEmpty()) {
            String pwd = userDto.getPassword();
            // Controllo robustezza password (min 8 caratteri, una maiuscola, una minuscola,
            // un numero)
            if (pwd.length() < 8 || !pwd.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Password non conforme ai criteri di sicurezza");
            }
            keycloakAdminService.updateUserPassword(email, pwd);
        }

        return userRepository.save(user);
    }

    public void deleteUser(String stringId) {
        UUID uuid = UUID.fromString(stringId);
        userRepository.deleteById(uuid);
    }

    // Carica l'avatar dell'utente
    @Transactional
    public User uploadAvatar(String userId, MultipartFile file) {
        User user = getUser(userId);

        if (user.getAvatarUrl() != null) {
            fileStorageService.delete(user.getAvatarUrl());
        }

        String relativePath = fileStorageService.store(file, USERS_AVATARS_SUBDIR);
        user.setAvatarUrl(relativePath);

        return userRepository.save(user);
    }

    // Carica la risorsa dell'avatar
    public Resource loadAvatar(String userId) {
        User user = getUser(userId);

        if (user.getAvatarUrl() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "user.avatarNotFound");
        }

        return fileStorageService.load(user.getAvatarUrl());
    }

    // Elimina l'avatar dell'utente
    @Transactional
    public User deleteAvatar(String userId) {
        User user = getUser(userId);

        if (user.getAvatarUrl() != null) {
            fileStorageService.delete(user.getAvatarUrl());
            user.setAvatarUrl(null);
            return userRepository.save(user);
        }

        return user;
    }
}

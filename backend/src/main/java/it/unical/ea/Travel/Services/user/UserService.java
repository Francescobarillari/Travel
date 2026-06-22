package it.unical.ea.Travel.Services.user;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.unical.ea.Travel.DTOs.authDto.SignupRequest;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Exception.ApiException;
import it.unical.ea.Travel.Repositories.user.UserRepository;
import it.unical.ea.Travel.Services.keycloak.KeycloakAdminService;
import it.unical.ea.Travel.Entities.user.UserType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;

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

    public User getUser(String stringId){
        UUID uuid = UUID.fromString(stringId);
        return userRepository.findById(uuid).orElseThrow(
            () -> new ApiException(HttpStatus.NOT_FOUND, "user.notFound")
        );
    }

    public List<User> getUsers(){
        return userRepository.findAll();
    }

    public void updateUser(){
        return; //da implementare
    }

    public void deleteUser(String stringId){
        UUID uuid = UUID.fromString(stringId);
        userRepository.deleteById(uuid);
    }
}

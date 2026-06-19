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

        String keycloakUserId = keycloakAdminService.createUser(request);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash("{keycloak}");
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRoles("ROLE_USER");

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

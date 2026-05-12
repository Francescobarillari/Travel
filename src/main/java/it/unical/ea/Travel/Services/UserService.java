package it.unical.ea.Travel.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.unical.ea.Travel.Controllers.dto.SignupRequest;
import it.unical.ea.Travel.Entities.User;
import it.unical.ea.Travel.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User saveUser(SignupRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRoles("ROLE_USER");
        return userRepository.save(user);
    }

    public User getUser(String stringId){
        UUID uuid = UUID.fromString(stringId);
        return userRepository.findById(uuid).orElseThrow(
            () -> new RuntimeException("Utente non trovato")
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

package it.unical.ea.Travel.Services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import it.unical.ea.Travel.Entities.User;
import it.unical.ea.Travel.Repositories.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User saveUser(User user){
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

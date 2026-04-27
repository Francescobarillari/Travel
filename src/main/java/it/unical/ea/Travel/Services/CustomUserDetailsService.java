package it.unical.ea.Travel.Services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import it.unical.ea.Travel.Repositories.UserRepository;
import it.unical.ea.Travel.Security.UserInfoDetails;
import lombok.RequiredArgsConstructor;

/*
    Questa classe è lo userService che però si occupa della sicurezza definendo loadUserByUsername
    per vederne il ruolo (dello user)
*/ 

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService{
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.getUserByEmail(email)
        .map(UserInfoDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + email));
    }
}

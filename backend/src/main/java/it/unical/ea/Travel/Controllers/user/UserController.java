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

import it.unical.ea.Travel.DTOs.authDto.SignupRequest;
import it.unical.ea.Travel.DTOs.user.UserDTO;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Mappers.user.UserMapper;
import it.unical.ea.Travel.Services.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User", description = "Gestione degli utenti")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Crea un nuovo utente")
    @PostMapping
    public UserDTO saveUser(@Valid @RequestBody SignupRequest request){
        User user = userService.saveUser(request);
        return userMapper.toDTO(user);
    }

    @Operation(summary = "Ottieni un utente per ID")
    @GetMapping("/{stringId}")
    public UserDTO getUser(@Parameter(description = "ID dell'utente", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId){
        User user = userService.getUser(stringId);
        return userMapper.toDTO(user);
    }

    @Operation(summary = "Ottieni tutti gli utenti")
    @GetMapping
    public List<UserDTO> getUsers(){
        return userService.getUsers().stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Operation(summary = "Ottieni il profilo dell'utente autenticato", description = "Restituisce i dati dell'utente autenticato tramite il token JWT")
    @GetMapping("/me")
    public UserDTO getMe(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        User user = userService.getUserByEmail(email);
        UserDTO userDTO = userMapper.toDTO(user);
        userDTO.setPassword(null); // Sicurezza extra: Non restituire mai il campo password nelle risposte
        return userDTO;
    }

    @Operation(summary = "Aggiorna il profilo dell'utente autenticato")
    @PutMapping("/me")
    public UserDTO updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UserDTO userDto) {
        String email = jwt.getClaimAsString("email");
        User updatedUser = userService.updateUser(email, userDto);
        UserDTO result = userMapper.toDTO(updatedUser);
        result.setPassword(null); // Sicurezza extra: Non restituire mai il campo password nelle risposte
        return result;
    }

    @Operation(summary = "Elimina un utente")
    @DeleteMapping("/{stringId}")
    public void deleteUser(@Parameter(description = "ID dell'utente", schema = @Schema(format = "uuid"), example = "550e8400-e29b-41d4-a716-446655440000") @PathVariable String stringId){
        userService.deleteUser(stringId);
    }
}

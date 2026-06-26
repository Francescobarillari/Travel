package it.unical.ea.Travel.Controllers.user;

import java.util.List;

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
import it.unical.ea.dtos.authDto.SignupRequest;
import it.unical.ea.enums.UserType;

import it.unical.ea.dtos.user.UserDTO;
import it.unical.ea.Travel.Entities.user.User;
import it.unical.ea.Travel.Mappers.user.UserMapper;
import it.unical.ea.Travel.Services.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public UserDTO saveUser(@Valid @RequestBody SignupRequest request) {
        User user = userService.saveUser(request);
        return userMapper.toDTO(user);
    }

    @GetMapping("/{stringId}")
    public UserDTO getUser(@PathVariable String stringId) {
        User user = userService.getUser(stringId);
        return userMapper.toDTO(user);
    }

    @GetMapping
    public List<UserDTO> getUsers() {
        return userService.getUsers().stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @GetMapping("/me")
    public UserDTO getMe(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        User user = userService.getUserByEmail(email);
        UserDTO userDTO = userMapper.toDTO(user);
        userDTO.setPassword(null); // Sicurezza extra: Non restituire mai il campo password nelle risposte
        return userDTO;
    }

    @PutMapping("/me")
    public UserDTO updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UserDTO userDto) {
        String email = jwt.getClaimAsString("email");
        User updatedUser = userService.updateUser(email, userDto);
        UserDTO result = userMapper.toDTO(updatedUser);
        result.setPassword(null); // Sicurezza extra: Non restituire mai il campo password nelle risposte
        return result;
    }

    @DeleteMapping("/{stringId}")
    public void deleteUser(@PathVariable String stringId) {
        userService.deleteUser(stringId);
    }
}

package it.unical.ea.Travel.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.unical.ea.Travel.Entities.User;
import it.unical.ea.Travel.Services.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @PostMapping
    public void saveUser(@RequestBody User user){
        userService.registerNewUser(user);
    }

    @GetMapping("/{stringId}")
    public User getUser(@PathVariable String stringId){
        return userService.getUser(stringId);
    }

    @GetMapping
    public List<User> getUsers(){
        return userService.getUsers();
    }

    @DeleteMapping
    public void deleteUser(String stringId){
        userService.deleteUser(stringId);
    }
}

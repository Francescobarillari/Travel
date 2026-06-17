package it.unical.ea.Travel.Controllers.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class KeycloakTestController {

    @GetMapping("/basic")
    public String basic() {
        return "Accesso BASIC consentito";
    }

    @GetMapping("/admin")
    public String admin() {
        return "Accesso ADMIN consentito";
    }
}

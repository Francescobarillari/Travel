package it.unical.ea.Travel.DTOs.authDto;

import it.unical.ea.Travel.Entities.user.UserType;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank(message = "{validation.email.notBlank}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.notBlank}")
    @Size(min = 8, message = "{validation.password.minSize}")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "{validation.password.pattern}"
    )
    private String password;

    @NotNull(message = "Il tipo utente è obbligatorio")
    private UserType userType;

    // Campi specifici per Viaggiatore
    @Size(max = 100, message = "{validation.firstName.maxSize}")
    private String firstName;

    @Size(max = 100, message = "{validation.lastName.maxSize}")
    private String lastName;

    // Campi specifici per Società
    @Size(max = 150, message = "Il nome dell'azienda non può superare i 150 caratteri")
    private String companyName;

    @Size(max = 50, message = "La Partita IVA non può superare i 50 caratteri")
    private String vatNumber;

    private List<String> documentPhotos = new ArrayList<>();

    // Campi comuni
    @Size(max = 30, message = "Il numero di telefono non può superare i 30 caratteri")
    private String phone;
}


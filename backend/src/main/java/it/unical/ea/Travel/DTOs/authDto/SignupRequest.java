package it.unical.ea.Travel.DTOs.authDto;

import it.unical.ea.Travel.Entities.user.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(format = "email", example = "user@example.com")
    private String email;

    @NotBlank(message = "{validation.password.notBlank}")
    @Size(min = 8, message = "{validation.password.minSize}")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "{validation.password.pattern}"
    )
    @Schema(format = "password", example = "Password1")
    private String password;

    @NotNull(message = "Il tipo utente è obbligatorio")
    @Schema(example = "VIAGGIATORE", description = "Tipo utente: VIAGGIATORE o SOCIETA")
    private UserType userType;

    // Campi specifici per Viaggiatore
    @Size(max = 100, message = "{validation.firstName.maxSize}")
    @Schema(example = "Mario")
    private String firstName;

    @Size(max = 100, message = "{validation.lastName.maxSize}")
    @Schema(example = "Rossi")
    private String lastName;

    // Campi specifici per Società
    @Size(max = 150, message = "Il nome dell'azienda non può superare i 150 caratteri")
    @Schema(example = "Viaggi S.r.l.")
    private String companyName;

    @Size(max = 50, message = "La Partita IVA non può superare i 50 caratteri")
    @Schema(example = "IT12345678901", description = "Partita IVA (solo per SOCIETA)")
    private String vatNumber;

    @Schema(description = "URL delle foto dei documenti")
    private List<String> documentPhotos = new ArrayList<>();

    // Campi comuni
    @Size(max = 30, message = "Il numero di telefono non può superare i 30 caratteri")
    @Schema(format = "phone", example = "+39 333 1234567")
    private String phone;
}


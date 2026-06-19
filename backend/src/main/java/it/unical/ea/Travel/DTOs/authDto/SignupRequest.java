package it.unical.ea.Travel.DTOs.authDto;

import jakarta.validation.constraints.*;
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

    @NotBlank(message = "{validation.firstName.notBlank}")
    @Size(max = 100, message = "{validation.firstName.maxSize}")
    private String firstName;

    @NotBlank(message = "{validation.lastName.notBlank}")
    @Size(max = 100, message = "{validation.lastName.maxSize}")
    private String lastName;
}


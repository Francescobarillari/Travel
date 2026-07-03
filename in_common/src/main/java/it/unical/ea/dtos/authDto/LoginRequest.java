package it.unical.ea.dtos.authDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "{validation.email.notBlank}")
    @Email(message = "{validation.email.invalid}")
    @Schema(format = "email", example = "user@example.com")
    private String email;

    @NotBlank(message = "{validation.password.notBlank}")
    @Schema(format = "password", example = "Password1")
    private String password;

    @Schema(description = "Token CAPTCHA fornito da Google reCAPTCHA", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String captchaToken;
}


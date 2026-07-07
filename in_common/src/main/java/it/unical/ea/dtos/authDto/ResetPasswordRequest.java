package it.unical.ea.dtos.authDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "{validation.email.notBlank}")
    @Email(message = "{validation.email.invalid}")
    @Schema(format = "email", example = "user@example.com")
    private String email;

    @NotBlank(message = "Il codice OTP è obbligatorio")
    @Size(min = 6, max = 6, message = "Il codice OTP deve essere di 6 cifre")
    @Schema(example = "123456")
    private String otp;

    @NotBlank(message = "{validation.password.notBlank}")
    @Size(min = 8, message = "{validation.password.minSize}")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "{validation.password.pattern}"
    )
    @Schema(format = "password", example = "Password1")
    private String newPassword;
}

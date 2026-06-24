package it.unical.ea.Travel.DTOs.user;

import io.swagger.v3.oas.annotations.media.Schema;
import it.unical.ea.Travel.Entities.user.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    @Schema(format = "email", example = "user@example.com")
    private String email;
    private UserType userType;
    private String firstName;
    private String lastName;
    private String companyName;
    private String vatNumber;
    private List<String> documentPhotos = new ArrayList<>();
    @Schema(format = "phone", example = "+39 333 1234567")
    private String phone;
    private String fullName;
    @Schema(format = "password", example = "Password1")
    private String password;
}

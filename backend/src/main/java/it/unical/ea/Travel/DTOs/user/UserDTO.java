package it.unical.ea.Travel.DTOs.user;

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
    private String email;
    private UserType userType;
    private String firstName;
    private String lastName;
    private String companyName;
    private String vatNumber;
    private List<String> documentPhotos = new ArrayList<>();
    private String phone;
    private String fullName;
    private String password;
}

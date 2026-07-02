package it.unical.ea.dtos.user;

import it.unical.ea.enums.UserType;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    @Schema(format = "email", example = "user@example.com")
    private String email;
    @Schema(example = "VIAGGIATORE", description = "Tipo utente: VIAGGIATORE o SOCIETA")
    private UserType userType;
    @Schema(example = "Mario")
    private String firstName;
    @Schema(example = "Rossi")
    private String lastName;
    @Schema(example = "Viaggi S.r.l.")
    private String companyName;
    @Schema(example = "IT12345678901", description = "Partita IVA (solo per SOCIETA)")
    private String vatNumber;
    @Schema(description = "URL delle foto dei documenti")
    private List<String> documentPhotos = new ArrayList<>();
    @Schema(format = "phone", example = "+39 333 1234567")
    private String phone;
    @Schema(description = "URL dell'avatar/foto profilo dell'utente")
    private String avatarUrl;
    @Schema(example = "Mario Rossi", accessMode = Schema.AccessMode.READ_ONLY)
    private String fullName;
    @Schema(format = "password", example = "Password1@", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;
    @Schema(description = "Stato di approvazione dell'utente")
    private Boolean approved = true;
    @Schema(description = "Stato di blocco dell'utente")
    private Boolean blocked = false;
}

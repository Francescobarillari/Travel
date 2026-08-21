package it.unical.ea.dtos.user;

import it.unical.ea.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPublicDTO {
    @Schema(format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(example = "VIAGGIATORE", description = "Tipo utente: VIAGGIATORE o SOCIETA")
    private UserType userType;

    @Schema(example = "Mario")
    private String firstName;

    @Schema(example = "Rossi")
    private String lastName;

    @Schema(example = "Viaggi S.r.l.")
    private String companyName;

    @Schema(description = "URL dell'avatar/foto profilo dell'utente")
    private String avatarUrl;

    @Schema(example = "Mario Rossi", accessMode = Schema.AccessMode.READ_ONLY)
    private String fullName;
}

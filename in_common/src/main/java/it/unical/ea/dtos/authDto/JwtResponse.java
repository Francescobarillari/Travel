package it.unical.ea.dtos.authDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Risposta contenente i token di autenticazione")
public class JwtResponse {

    @Schema(description = "Token JWT di accesso da inserire negli header delle richieste autenticate", example = "eyJhbGciOi...")
    private String accessToken;

    @Schema(description = "Token di refresh utilizzato per richiedere un nuovo access token quando questo scade", example = "eyJhbGciOi...")
    private String refreshToken;
}

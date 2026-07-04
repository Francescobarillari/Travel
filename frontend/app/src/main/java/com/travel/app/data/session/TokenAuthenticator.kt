package com.travel.app.data.session

import android.util.Log
import com.google.gson.Gson
import com.travel.app.BuildConfig
import okhttp3.Authenticator
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val sessionManagerProvider: () -> SessionManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Se abbiamo già provato a fare l'autenticazione con questo token ed è fallita,
        // evitiamo loop infiniti di chiamate 401 ritornando null
        if (response.priorResponse != null) {
            return null
        }

        val sessionManager = sessionManagerProvider()
        val refreshToken = sessionManager.getSessionRefreshToken()

        if (refreshToken.isNullOrBlank()) {
            return null
        }

        synchronized(this) {
            // Controlla se un altro thread ha già aggiornato il token mentre eravamo in attesa
            val currentToken = sessionManager.getSessionToken()
            val originalRequestToken = response.request.header("Authorization")?.replace("Bearer ", "")

            // Se il token corrente sul session manager è già cambiato, usiamo direttamente il nuovo token
            if (currentToken != originalRequestToken && !currentToken.isNullOrBlank()) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Altrimenti, facciamo la richiesta diretta a Keycloak per ottenere il nuovo token
            val keycloakUrl = BuildConfig.KEYCLOAK_URL
            val realm = BuildConfig.KEYCLOAK_REALM
            val clientId = BuildConfig.KEYCLOAK_CLIENT_ID
            val clientSecret = BuildConfig.KEYCLOAK_CLIENT_SECRET

            val tokenUrl = "${keycloakUrl}realms/$realm/protocol/openid-connect/token"

            val client = OkHttpClient()
            val requestBody = FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .build()

            val request = Request.Builder()
                .url(tokenUrl)
                .post(requestBody)
                .build()

            try {
                client.newCall(request).execute().use { tokenResponse ->
                    if (tokenResponse.isSuccessful) {
                        val bodyString = tokenResponse.body?.string()
                        val gson = Gson()
                        val map = gson.fromJson(bodyString, Map::class.java)
                        val newAccessToken = map["access_token"] as? String
                        val newRefreshToken = map["refresh_token"] as? String

                        if (!newAccessToken.isNullOrBlank() && !newRefreshToken.isNullOrBlank()) {
                            // Salva i nuovi token nel session manager
                            sessionManager.saveTokens(newAccessToken, newRefreshToken)
                            Log.d("TokenAuthenticator", "Tokens aggiornati con successo!")

                            // Riprova la richiesta originale con il nuovo access token
                            return response.request.newBuilder()
                                .header("Authorization", "Bearer $newAccessToken")
                                .build()
                        }
                    } else {
                        Log.e("TokenAuthenticator", "Impossibile fare il refresh del token: ${tokenResponse.code}")
                    }
                    Unit
                }
            } catch (e: Exception) {
                Log.e("TokenAuthenticator", "Errore di rete durante il refresh del token", e)
            }

            // Se il refresh fallisce (es. anche il refresh token è scaduto), effettua il logout dell'utente
            Log.w("TokenAuthenticator", "Refresh token scaduto o non valido. Disconnessione utente.")
            sessionManager.clearSession()
            return null
        }
    }
}

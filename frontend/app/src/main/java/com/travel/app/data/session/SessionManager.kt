package com.travel.app.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.travel.app.domain.model.User

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            apply()
        }
    }

    fun saveSession(user: User, token: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_TYPE, user.userType)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_PHONE, user.phone)
            apply()
        }
    }

    fun getSessionToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getSessionUser(): User? {
        val email = prefs.getString(KEY_USER_EMAIL, null) ?: return null
        val userType = prefs.getString(KEY_USER_TYPE, "VIAGGIATORE") ?: "VIAGGIATORE"
        val name = prefs.getString(KEY_USER_NAME, null)
        val phone = prefs.getString(KEY_USER_PHONE, null)

        return User(
            email = email,
            userType = userType,
            phone = phone,
            name = name,
            password = null // Sicurezza: Non memorizziamo mai la password dell'utente in SharedPreferences
        )
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    companion object {
        private const val PREFS_NAME = "travel_app_prefs_encrypted"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}

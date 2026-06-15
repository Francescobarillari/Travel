package com.travel.app.data.datasource

import com.travel.app.domain.model.User

object InMemoryDataSource {
    val registeredUsers = mutableMapOf<String, String>(
        "user@travel.com" to "password123"
    )
    
    val userProfileNames = mutableMapOf<String, String>(
        "user@travel.com" to "Utente Test"
    )
}


package com.travel.app.domain.repository

import com.travel.app.domain.model.User

interface UserRepository {
    fun login(email: String, password: String): Result<User>
    fun register(email: String, username: String, password: String): Result<User>
}

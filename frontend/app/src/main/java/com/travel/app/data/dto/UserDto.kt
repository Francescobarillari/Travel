package com.travel.app.data.dto

data class UserDto(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String? = null
)

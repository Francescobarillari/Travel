package com.travel.app.data.dto

data class SignUpRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
)
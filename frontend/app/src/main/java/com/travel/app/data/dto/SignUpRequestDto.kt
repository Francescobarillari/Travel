package com.travel.app.data.dto

data class SignUpRequestDto(
    val email: String,
    val password: String,
    val userType: String,                       // "VIAGGIATORE" | "SOCIETA"
    val firstName: String? = null,              // solo VIAGGIATORE
    val lastName: String? = null,               // solo VIAGGIATORE
    val companyName: String? = null,            // solo SOCIETA
    val vatNumber: String? = null,              // solo SOCIETA
    val documentPhotos: List<String> = emptyList(), // solo SOCIETA
    val phone: String? = null,
)
package com.travel.app.domain.model

import com.travel.app.data.dto.UserDTO

data class User(
    val email: String,
    val username: String,
    val userType: String = "VIAGGIATORE",
    val phone: String? = null,
    val name: String? = null,
    val password: String? = null
)

fun User.toDto(): UserDTO {
    val isSocieta = userType == "SOCIETA"
    val parts = name?.split(" ") ?: emptyList()
    val fName = parts.firstOrNull()
    val lName = if (parts.size > 1) parts.drop(1).joinToString(" ") else null
    
    return UserDTO(
        email = email,
        userType = if (isSocieta) UserDTO.UserType.sOCIETA else UserDTO.UserType.vIAGGIATORE,
        phone = phone,
        firstName = if (!isSocieta) fName else null,
        lastName = if (!isSocieta) lName else null,
        companyName = if (isSocieta) name else null,
        fullName = name,
        password = password
    )
}

fun UserDTO.toDomain(): User {
    val detectedType = userType?.name ?: "VIAGGIATORE"
    val calculatedName = fullName ?: when {
        !firstName.isNullOrBlank() || !lastName.isNullOrBlank() -> "${firstName.orEmpty()} ${lastName.orEmpty()}".trim()
        !companyName.isNullOrBlank() -> companyName
        else -> null
    }
    return User(
        email = email.orEmpty(),
        username = calculatedName ?: email?.split("@")?.firstOrNull() ?: "Utente",
        userType = detectedType,
        phone = phone,
        name = calculatedName
    )
}


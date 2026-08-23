package com.travel.app.domain.model

import it.unical.ea.dtos.user.UserDTO
import it.unical.ea.dtos.user.UserPrivateDTO
import it.unical.ea.dtos.user.UserPublicDTO
import it.unical.ea.enums.UserType

data class User(
    val id: String? = null,
    val email: String = "",
    val userType: String = "VIAGGIATORE",
    val phone: String? = null,
    val name: String? = null,
    val password: String? = null,
    val vatNumber: String? = null,
    val approved: Boolean = true,
    val blocked: Boolean = false,
    val documentPhotos: List<String> = emptyList(),
    val avatarUrl: String? = null
)

fun User.toPrivateDto(): UserPrivateDTO {
    val isSocieta = userType == "SOCIETA"
    val parts = name?.split(" ") ?: emptyList()
    val fName = parts.firstOrNull()
    val lName = if (parts.size > 1) parts.drop(1).joinToString(" ") else null
    
    return UserPrivateDTO().apply {
        this.id = this@toPrivateDto.id?.let { java.util.UUID.fromString(it) }
        this.email = this@toPrivateDto.email
        this.userType = if (isSocieta) UserType.SOCIETA else UserType.VIAGGIATORE
        this.phone = this@toPrivateDto.phone
        this.firstName = if (!isSocieta) fName else null
        this.lastName = if (!isSocieta) lName else null
        this.companyName = if (isSocieta) name else null
        this.fullName = name
        this.password = this@toPrivateDto.password
        this.vatNumber = if (isSocieta) vatNumber else null
        this.approved = this@toPrivateDto.approved
        this.blocked = this@toPrivateDto.blocked
        this.documentPhotos = this@toPrivateDto.documentPhotos
        this.avatarUrl = this@toPrivateDto.avatarUrl
    }
}

fun UserPrivateDTO.toDomain(): User {
    val detectedType = when {
        email?.contains("admin", ignoreCase = true) == true -> "ADMIN"
        else -> userType?.name ?: "VIAGGIATORE"
    }
    val calculatedName = fullName ?: when {
        !firstName.isNullOrBlank() || !lastName.isNullOrBlank() -> "${firstName.orEmpty()} ${lastName.orEmpty()}".trim()
        !companyName.isNullOrBlank() -> companyName
        else -> null
    }
    return User(
        id = id?.toString(),
        email = email.orEmpty(),
        userType = detectedType,
        phone = phone,
        name = calculatedName,
        vatNumber = vatNumber,
        approved = approved ?: true,
        blocked = blocked ?: false,
        documentPhotos = documentPhotos ?: emptyList(),
        avatarUrl = avatarUrl
    )
}

fun UserPublicDTO.toDomain(): User {
    val detectedType = userType?.name ?: "VIAGGIATORE"
    val calculatedName = fullName ?: when {
        !firstName.isNullOrBlank() || !lastName.isNullOrBlank() -> "${firstName.orEmpty()} ${lastName.orEmpty()}".trim()
        !companyName.isNullOrBlank() -> companyName
        else -> null
    }
    return User(
        id = id?.toString(),
        email = "",
        userType = detectedType,
        name = calculatedName,
        avatarUrl = avatarUrl
    )
}

fun User.toDto(): UserDTO {
    val isSocieta = userType == "SOCIETA"
    val parts = name?.split(" ") ?: emptyList()
    val fName = parts.firstOrNull()
    val lName = if (parts.size > 1) parts.drop(1).joinToString(" ") else null
    
    return UserDTO().apply {
        this.id = this@toDto.id?.let { java.util.UUID.fromString(it) }
        this.email = this@toDto.email
        this.userType = if (isSocieta) UserType.SOCIETA else UserType.VIAGGIATORE
        this.phone = this@toDto.phone
        this.firstName = if (!isSocieta) fName else null
        this.lastName = if (!isSocieta) lName else null
        this.companyName = if (isSocieta) name else null
        this.fullName = name
        this.password = this@toDto.password
        this.vatNumber = if (isSocieta) vatNumber else null
        this.approved = this@toDto.approved
        this.blocked = this@toDto.blocked
        this.documentPhotos = this@toDto.documentPhotos
        this.avatarUrl = this@toDto.avatarUrl
    }
}

fun UserDTO.toDomain(): User {
    val detectedType = when {
        email?.contains("admin", ignoreCase = true) == true -> "ADMIN"
        else -> userType?.name ?: "VIAGGIATORE"
    }
    val calculatedName = fullName ?: when {
        !firstName.isNullOrBlank() || !lastName.isNullOrBlank() -> "${firstName.orEmpty()} ${lastName.orEmpty()}".trim()
        !companyName.isNullOrBlank() -> companyName
        else -> null
    }
    return User(
        id = id?.toString(),
        email = email.orEmpty(),
        userType = detectedType,
        phone = phone,
        name = calculatedName,
        vatNumber = vatNumber,
        approved = approved ?: true,
        blocked = blocked ?: false,
        documentPhotos = documentPhotos ?: emptyList(),
        avatarUrl = avatarUrl
    )
}

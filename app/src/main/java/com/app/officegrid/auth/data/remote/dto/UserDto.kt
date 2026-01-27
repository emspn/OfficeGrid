package com.app.officegrid.auth.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val full_name: String,
    val role: String,
    val company_id: String
)

@Serializable
data class AuthResponseDto(
    val access_token: String,
    val user: UserDto
)
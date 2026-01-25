package com.app.officegrid.auth.domain.model

import com.app.officegrid.core.common.UserRole

data class User(
    val id: String,
    val email: String,
    val role: UserRole,
    val companyId: String
)

data class UserSession(
    val user: User,
    val token: String
)
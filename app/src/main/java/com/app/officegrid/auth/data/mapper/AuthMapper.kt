package com.app.officegrid.auth.data.mapper

import com.app.officegrid.auth.data.remote.dto.UserDto
import com.app.officegrid.auth.domain.model.User
import com.app.officegrid.core.common.UserRole

fun UserDto.toDomain(): User {
    return User(
        id = id,
        email = email,
        fullName = full_name,
        role = UserRole.valueOf(role.uppercase()),
        companyId = company_id
    )
}
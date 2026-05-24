package com.vmptf.mobile.features.auth.data.model

import com.vmptf.mobile.features.auth.domain.model.User

data class LoginResponse(
    val token: String,
    val user: User
)

data class RegisterResponse(
    val message: String,
    val userId: Int
)

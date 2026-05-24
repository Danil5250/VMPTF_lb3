package com.vmptf.mobile.features.auth.domain.repository

import com.vmptf.mobile.features.auth.data.model.LoginResponse
import com.vmptf.mobile.features.auth.data.model.RegisterResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): LoginResponse
    suspend fun register(email: String, password: String, name: String): RegisterResponse
}

package com.vmptf.mobile.features.auth.data.repository

import com.vmptf.mobile.features.auth.data.api.AuthApiService
import com.vmptf.mobile.features.auth.data.model.LoginRequest
import com.vmptf.mobile.features.auth.data.model.LoginResponse
import com.vmptf.mobile.features.auth.data.model.RegisterRequest
import com.vmptf.mobile.features.auth.data.model.RegisterResponse
import com.vmptf.mobile.features.auth.domain.repository.AuthRepository

// get data from backend
//single source of data
//isolate logic of getting data from the rest logic of application
// : = implementation of AuthRepository
class AuthRepositoryImpl(
    private val apiService: AuthApiService
) : AuthRepository {
    //suspend - function which can pause coroutines without blocking the thread
    //used for asynchronous
    override suspend fun login(email: String, password: String): LoginResponse {
        return apiService.login(LoginRequest(email, password))
    }

    override suspend fun register(email: String, password: String, name: String): RegisterResponse {
        return apiService.register(RegisterRequest(email, password, name))
    }
}

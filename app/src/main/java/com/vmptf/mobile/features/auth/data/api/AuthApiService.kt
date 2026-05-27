package com.vmptf.mobile.features.auth.data.api

import com.vmptf.mobile.features.auth.data.model.LoginRequest
import com.vmptf.mobile.features.auth.data.model.LoginResponse
import com.vmptf.mobile.features.auth.data.model.RegisterRequest
import com.vmptf.mobile.features.auth.data.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

//routes using which mobile works with backend
interface AuthApiService {
    //suspend - function which can pause coroutines without blocking the thread
    //used for asynchronous
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
}

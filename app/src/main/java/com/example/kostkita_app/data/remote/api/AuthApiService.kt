package com.example.kostkita_app.data.remote.api

import com.example.kostkita_app.data.remote.dto.*
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): MessageResponse

    @PUT("auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: UpdateProfileRequest
    ): LoginResponse

    @PUT("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): MessageResponse
}
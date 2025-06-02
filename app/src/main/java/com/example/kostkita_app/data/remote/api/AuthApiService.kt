package com.example.kostkita.data.remote.api

import com.example.kostkita.data.remote.dto.*
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): MessageResponse

    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): LoginResponse

    @PUT("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): MessageResponse
}
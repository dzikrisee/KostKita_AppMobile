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
    suspend fun changePassword(
        @Header("Authorization") authorization: String,
        @Body request: ChangePasswordRequest
    ): MessageResponse

    @GET("auth/profile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String
    ): GetProfileResponse
}

// Response untuk GET profile
data class GetProfileResponse(
    val user: UserDto
)
package com.example.kostkita_app.data.remote.dto

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val full_name: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class UpdateProfileRequest(
    val username: String,
    val email: String,
    val full_name: String
)

data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String
)

data class LoginResponse(
    val token: String,
    val user: UserDto
)

data class MessageResponse(
    val message: String
)

data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val full_name: String,
    val role: String
)
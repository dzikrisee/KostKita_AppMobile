package com.example.kostkita.domain.repository

import com.example.kostkita.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun register(
        username: String,
        email: String,
        password: String,
        fullName: String
    ): Result<User>
    suspend fun forgotPassword(email: String): Result<String>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun updateProfile(user: User): Result<User>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Boolean>
}
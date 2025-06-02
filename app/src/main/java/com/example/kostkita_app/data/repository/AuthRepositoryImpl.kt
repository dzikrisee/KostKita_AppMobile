package com.example.kostkita_app.data.repository

import android.content.Context
import com.example.kostkita_app.data.remote.api.AuthApiService
import com.example.kostkita_app.data.remote.dto.*
import com.example.kostkita_app.domain.model.User
import com.example.kostkita_app.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = authApiService.login(LoginRequest(username, password))
            val user = response.user.toDomain(response.token)
            saveToken(response.token)
            saveUserData(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        fullName: String
    ): Result<User> {
        return try {
            val response = authApiService.register(
                RegisterRequest(username, email, password, fullName)
            )
            val user = response.user.toDomain(response.token)
            saveToken(response.token)
            saveUserData(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response = authApiService.forgotPassword(ForgotPasswordRequest(email))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(user: User): Result<User> {
        return try {
            val response = authApiService.updateProfile(
                UpdateProfileRequest(user.username, user.email, user.fullName)
            )
            val updatedUser = response.user.toDomain(response.token)
            saveUserData(updatedUser)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Boolean> {
        return try {
            authApiService.changePassword(ChangePasswordRequest(oldPassword, newPassword))
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        prefs.edit().clear().apply()
    }

    override suspend fun getCurrentUser(): User? {
        val token = getToken() ?: return null
        val id = prefs.getString("user_id", null) ?: return null
        val username = prefs.getString("username", null) ?: return null
        val email = prefs.getString("email", null) ?: return null
        val fullName = prefs.getString("full_name", null) ?: return null
        val role = prefs.getString("role", null) ?: return null

        return User(id, username, email, fullName, role, token)
    }

    override suspend fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    override suspend fun getToken(): String? {
        return prefs.getString("auth_token", null)
    }

    private fun saveUserData(user: User) {
        prefs.edit().apply {
            putString("user_id", user.id)
            putString("username", user.username)
            putString("email", user.email)
            putString("full_name", user.fullName)
            putString("role", user.role)
            apply()
        }
    }

    private fun UserDto.toDomain(token: String): User {
        return User(
            id = id,
            username = username,
            email = email,
            fullName = full_name,
            role = role,
            token = token
        )
    }
}
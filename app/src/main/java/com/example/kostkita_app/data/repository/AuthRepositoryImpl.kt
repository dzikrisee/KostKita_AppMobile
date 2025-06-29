package com.example.kostkita_app.data.repository

import android.content.Context
import android.util.Log
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
            Log.d("AuthRepository", "=== LOGIN ATTEMPT ===")
            Log.d("AuthRepository", "Username: $username")

            val response = authApiService.login(LoginRequest(username, password))
            val user = response.user.toDomain(response.token)

            saveToken(response.token)
            saveUserData(user)

            Log.d("AuthRepository", "=== LOGIN SUCCESS ===")
            Log.d("AuthRepository", "User: ${user.username}")
            Log.d("AuthRepository", "Profile Photo: ${user.profilePhoto}")

            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "=== LOGIN FAILED ===", e)
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
            Log.d("AuthRepository", "=== REGISTER ATTEMPT ===")

            val response = authApiService.register(
                RegisterRequest(username, email, password, fullName)
            )
            val user = response.user.toDomain(response.token)

            saveToken(response.token)
            saveUserData(user)

            Log.d("AuthRepository", "=== REGISTER SUCCESS ===")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "=== REGISTER FAILED ===", e)
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
            val token = getToken()
            if (token.isNullOrEmpty()) {
                Log.e("AuthRepository", "=== NO TOKEN AVAILABLE ===")
                return Result.failure(Exception("Token tidak tersedia"))
            }

            Log.d("AuthRepository", "=== UPDATE PROFILE REQUEST ===")
            Log.d("AuthRepository", "User ID: ${user.id}")
            Log.d("AuthRepository", "Username: ${user.username}")
            Log.d("AuthRepository", "Email: ${user.email}")
            Log.d("AuthRepository", "Full Name: ${user.fullName}")
            Log.d("AuthRepository", "Profile Photo Path: ${user.profilePhoto}")
            Log.d("AuthRepository", "Token: ${token.take(20)}...")

            val response = authApiService.updateProfile(
                authorization = "Bearer $token",
                request = UpdateProfileRequest(
                    username = user.username,
                    email = user.email,
                    full_name = user.fullName,
                    profile_photo = user.profilePhoto
                )
            )

            val updatedUser = response.user.toDomain(response.token)

            // PENTING: Simpan data yang diperbarui
            saveToken(response.token)
            saveUserData(updatedUser)

            Log.d("AuthRepository", "=== PROFILE UPDATE SUCCESS ===")
            Log.d("AuthRepository", "Updated Username: ${updatedUser.username}")
            Log.d("AuthRepository", "Updated Photo Path: ${updatedUser.profilePhoto}")

            Result.success(updatedUser)

        } catch (e: Exception) {
            Log.e("AuthRepository", "=== UPDATE PROFILE FAILED ===", e)
            Result.failure(Exception("Gagal memperbarui profil: ${e.message}"))
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Boolean> {
        return try {
            val token = getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token tidak tersedia"))
            }

            Log.d("AuthRepository", "=== CHANGE PASSWORD ATTEMPT ===")

            authApiService.changePassword(
                authorization = "Bearer $token",
                request = ChangePasswordRequest(oldPassword, newPassword)
            )

            Log.d("AuthRepository", "=== PASSWORD CHANGED SUCCESS ===")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("AuthRepository", "=== CHANGE PASSWORD FAILED ===", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        Log.d("AuthRepository", "=== LOGOUT ===")
        prefs.edit().clear().apply()
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val token = getToken()
            Log.d("AuthRepository", "=== GET CURRENT USER ===")
            Log.d("AuthRepository", "Token available: ${!token.isNullOrEmpty()}")

            if (token.isNullOrEmpty()) {
                Log.w("AuthRepository", "No token found in getCurrentUser")
                return null
            }

            val id = prefs.getString("user_id", null) ?: return null
            val username = prefs.getString("username", null) ?: return null
            val email = prefs.getString("email", null) ?: return null
            val fullName = prefs.getString("full_name", null) ?: return null
            val role = prefs.getString("role", null) ?: return null
            val profilePhoto = prefs.getString("profile_photo", null)

            val user = User(id, username, email, fullName, role, token, profilePhoto)

            Log.d("AuthRepository", "=== CURRENT USER LOADED ===")
            Log.d("AuthRepository", "Username: $username")
            Log.d("AuthRepository", "Profile Photo: $profilePhoto")

            user
        } catch (e: Exception) {
            Log.e("AuthRepository", "Get current user failed", e)
            null
        }
    }

    override suspend fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
        Log.d("AuthRepository", "Token saved: ${token.take(20)}...")
    }

    override suspend fun getToken(): String? {
        return prefs.getString("auth_token", null)
    }

    /**
     * Simpan data user ke SharedPreferences
     */
    private fun saveUserData(user: User) {
        prefs.edit().apply {
            putString("user_id", user.id)
            putString("username", user.username)
            putString("email", user.email)
            putString("full_name", user.fullName)
            putString("role", user.role)
            putString("profile_photo", user.profilePhoto)
            apply()
        }

        Log.d("AuthRepository", "=== USER DATA SAVED ===")
        Log.d("AuthRepository", "Username: ${user.username}")
        Log.d("AuthRepository", "Profile Photo saved: ${user.profilePhoto}")
    }

    /**
     * Convert UserDto ke Domain Model
     */
    private fun UserDto.toDomain(token: String): User {
        return User(
            id = id,
            username = username,
            email = email,
            fullName = full_name,
            role = role,
            token = token,
            profilePhoto = profile_photo
        )
    }
}
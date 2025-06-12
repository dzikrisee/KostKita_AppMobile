package com.example.kostkita_app.presentation.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kostkita_app.domain.model.User
import com.example.kostkita_app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                val currentUser = authRepository.getCurrentUser()
                _user.value = currentUser
                Log.d("ProfileViewModel", "User loaded: ${currentUser?.username}, Photo: ${currentUser?.profilePhoto}")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to load user", e)
                _user.value = null
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                _logoutState.value = LogoutState.Loading
                authRepository.logout()
                _user.value = null
                _logoutState.value = LogoutState.Success
            } catch (e: Exception) {
                _logoutState.value = LogoutState.Error(e.message ?: "Logout failed")
            }
        }
    }

    fun refreshUser() {
        loadCurrentUser()
    }

    fun refreshUserData() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true

                // Force refresh dari repository
                val currentUser = authRepository.getCurrentUser()
                _user.value = currentUser

                Log.d("ProfileViewModel", "=== USER DATA REFRESHED ===")
                Log.d("ProfileViewModel", "Username: ${currentUser?.username}")
                Log.d("ProfileViewModel", "Full Name: ${currentUser?.fullName}")
                Log.d("ProfileViewModel", "Email: ${currentUser?.email}")
                Log.d("ProfileViewModel", "Profile Photo: ${currentUser?.profilePhoto}")

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to refresh user data", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * FIXED: Method khusus untuk refresh setelah update profile
     * Dipanggil dari ProfileScreen setelah menerima flag dari EditProfile
     */
    fun forceRefreshAfterUpdate() {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "=== FORCE REFRESH AFTER UPDATE ===")
                _isRefreshing.value = true

                // Clear current user first untuk trigger recomposition
                _user.value = null

                // Tunggu sebentar untuk memastikan data sudah tersimpan
                kotlinx.coroutines.delay(300)

                // Get fresh data dari repository
                val updatedUser = authRepository.getCurrentUser()

                Log.d("ProfileViewModel", "=== FRESH USER DATA ===")
                Log.d("ProfileViewModel", "Updated User: ${updatedUser?.username}")
                Log.d("ProfileViewModel", "Updated Photo: ${updatedUser?.profilePhoto}")

                // Set new user data
                _user.value = updatedUser

                // Trigger additional refresh untuk memastikan UI update
                kotlinx.coroutines.delay(100)

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to force refresh user data", e)
                // Fallback: load current user
                loadCurrentUser()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Clear state ketika user logout
     */
    fun clearUserData() {
        _user.value = null
        _logoutState.value = LogoutState.Idle
    }

    /**
     * Manual trigger untuk refresh photo
     * Bisa dipanggil dari UI jika foto tidak muncul
     */
    fun refreshPhotoOnly() {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "=== REFRESH PHOTO ONLY ===")

                val currentUser = _user.value
                if (currentUser != null) {
                    // Force reload user dengan photo terbaru
                    val freshUser = authRepository.getCurrentUser()

                    if (freshUser?.profilePhoto != currentUser.profilePhoto) {
                        Log.d("ProfileViewModel", "Photo changed: ${currentUser.profilePhoto} → ${freshUser?.profilePhoto}")
                        _user.value = freshUser
                    } else {
                        Log.d("ProfileViewModel", "Photo unchanged: ${currentUser.profilePhoto}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to refresh photo", e)
            }
        }
    }
}

sealed class LogoutState {
    object Idle : LogoutState()
    object Loading : LogoutState()
    object Success : LogoutState()
    data class Error(val message: String) : LogoutState()
}
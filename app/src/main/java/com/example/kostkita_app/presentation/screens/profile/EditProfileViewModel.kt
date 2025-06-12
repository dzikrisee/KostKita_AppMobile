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
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateState: StateFlow<UpdateProfileState> = _updateState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _user.value = authRepository.getCurrentUser()
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            try {
                Log.d("EditProfileViewModel", "=== STARTING UPDATE ===")
                Log.d("EditProfileViewModel", "User: ${user.username}")

                _updateState.value = UpdateProfileState.Loading
                Log.d("EditProfileViewModel", "State set to Loading")

                val result = authRepository.updateProfile(user)

                result.onSuccess { updatedUser ->
                    Log.d("EditProfileViewModel", "=== UPDATE SUCCESS ===")
                    _user.value = updatedUser
                    _updateState.value = UpdateProfileState.Success
                }.onFailure { exception ->
                    Log.e("EditProfileViewModel", "=== UPDATE FAILED ===", exception)
                    _updateState.value = UpdateProfileState.Error(
                        exception.message ?: "Gagal memperbarui profil"
                    )
                }
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "=== UNEXPECTED ERROR ===", e)
                _updateState.value = UpdateProfileState.Error("Terjadi kesalahan tidak terduga")
            }
        }
    }
}

sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}
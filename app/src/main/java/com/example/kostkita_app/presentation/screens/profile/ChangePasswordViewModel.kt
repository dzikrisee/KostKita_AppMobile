package com.example.kostkita.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kostkita.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState.asStateFlow()

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordState.Loading

            authRepository.changePassword(oldPassword, newPassword)
                .onSuccess {
                    _changePasswordState.value = ChangePasswordState.Success
                }
                .onFailure { exception ->
                    _changePasswordState.value = ChangePasswordState.Error(
                        exception.message ?: "Gagal mengubah password"
                    )
                }
        }
    }
}

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}
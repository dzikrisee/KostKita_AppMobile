package com.example.kostkita.presentation.screens.auth

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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun register(username: String, email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            authRepository.register(username, email, password, fullName)
                .onSuccess { user ->
                    _registerState.value = RegisterState.Success(user)
                }
                .onFailure { exception ->
                    _registerState.value = RegisterState.Error(
                        exception.message ?: "Registrasi gagal. Silakan coba lagi."
                    )
                }
        }
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: com.example.kostkita.domain.model.User) : RegisterState()
    data class Error(val message: String) : RegisterState()
}
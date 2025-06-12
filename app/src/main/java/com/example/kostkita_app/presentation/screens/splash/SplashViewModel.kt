package com.example.kostkita_app.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kostkita_app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _splashState = MutableStateFlow<SplashState>(SplashState.Loading)
    val splashState: StateFlow<SplashState> = _splashState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                // Tunggu minimal 3 detik untuk splash screen
                delay(3000)

                val currentUser = authRepository.getCurrentUser()
                val hasValidToken = authRepository.getToken() != null

                _splashState.value = if (currentUser != null && hasValidToken) {
                    SplashState.Authenticated
                } else {
                    SplashState.Unauthenticated
                }
            } catch (e: Exception) {
                _splashState.value = SplashState.Unauthenticated
            }
        }
    }
}

sealed class SplashState {
    object Loading : SplashState()
    object Authenticated : SplashState()
    object Unauthenticated : SplashState()
}
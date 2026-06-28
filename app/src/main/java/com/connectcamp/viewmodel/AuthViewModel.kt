package com.connectcamp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectcamp.data.model.User
import com.connectcamp.data.model.UserRole
import com.connectcamp.data.repository.AuthRepository
import com.connectcamp.data.repository.AuthResult
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: FirebaseUser) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _passwordResetState = MutableStateFlow<String?>(null)
    val passwordResetState: StateFlow<String?> = _passwordResetState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authStateFlow.collect { firebaseUser ->
                if (firebaseUser != null) {
                    loadUserProfile()
                } else {
                    _currentUser.value = null
                }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUserProfile()
        }
    }

    fun login(email: String, password: String) {
        if (!validateLoginInputs(email, password)) return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.login(email.trim(), password)
            _uiState.value = when (result) {
                is AuthResult.Success -> AuthUiState.Success(result.user)
                is AuthResult.Error -> AuthUiState.Error(result.message)
            }
        }
    }

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String,
        phone: String,
        role: UserRole
    ) {
        if (!validateRegisterInputs(email, password, confirmPassword, fullName)) return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.register(
                email = email.trim(),
                password = password,
                fullName = fullName.trim(),
                phone = phone.trim(),
                role = role
            )
            _uiState.value = when (result) {
                is AuthResult.Success -> AuthUiState.Success(result.user)
                is AuthResult.Error -> AuthUiState.Error(result.message)
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _passwordResetState.value = "Introduce tu correo electrónico"
            return
        }
        viewModelScope.launch {
            val result = authRepository.resetPassword(email.trim())
            _passwordResetState.value = if (result.isSuccess) {
                "Se ha enviado un correo para restablecer la contraseña"
            } else {
                "Error al enviar el correo. Verifica la dirección"
            }
        }
    }

    fun clearPasswordResetState() {
        _passwordResetState.value = null
    }

    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn

    private fun validateLoginInputs(email: String, password: String): Boolean {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("El correo electrónico no puede estar vacío")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Correo electrónico inválido")
            return false
        }
        if (password.isBlank()) {
            _uiState.value = AuthUiState.Error("La contraseña no puede estar vacía")
            return false
        }
        return true
    }

    private fun validateRegisterInputs(
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String
    ): Boolean {
        if (fullName.isBlank()) {
            _uiState.value = AuthUiState.Error("El nombre no puede estar vacío")
            return false
        }
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("El correo electrónico no puede estar vacío")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Correo electrónico inválido")
            return false
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 6 caracteres")
            return false
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Las contraseñas no coinciden")
            return false
        }
        return true
    }
}

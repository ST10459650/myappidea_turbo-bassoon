package com.example.pillarpocket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillarpocket.data.local.User
import com.example.pillarpocket.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(username: String, password: String) {
        if (!validateLogin(username, password)) return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(username.trim(), password)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(it) },
                onFailure = { AuthState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (!validateRegistration(username, email, password, confirmPassword)) return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(username.trim(), email.trim(), password)
            _authState.value = result.fold(
                onSuccess = {
                    val loginResult = repository.login(username.trim(), password)
                    loginResult.fold(
                        onSuccess = { AuthState.Success(it) },
                        onFailure = { AuthState.Error("Account created but login failed") }
                    )
                },
                onFailure = { AuthState.Error(it.message ?: "Registration failed") }
            )
        }
    }

    private fun validateLogin(username: String, password: String): Boolean {
        return when {
            username.isBlank() -> { _authState.value = AuthState.Error("Username cannot be empty"); false }
            password.isBlank() -> { _authState.value = AuthState.Error("Password cannot be empty"); false }
            else -> true
        }
    }

    private fun validateRegistration(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return when {
            username.isBlank() -> {
                _authState.value = AuthState.Error("Username cannot be empty"); false
            }
            username.trim().length < 3 -> {
                _authState.value = AuthState.Error("Username must be at least 3 characters"); false
            }
            email.isBlank() -> {
                _authState.value = AuthState.Error("Email cannot be empty"); false
            }
            !emailRegex.matches(email.trim()) -> {
                _authState.value = AuthState.Error("Please enter a valid email address"); false
            }
            password.length < 6 -> {
                _authState.value = AuthState.Error("Password must be at least 6 characters"); false
            }
            password != confirmPassword -> {
                _authState.value = AuthState.Error("Passwords do not match"); false
            }
            else -> true
        }
    }

    fun resetState() { _authState.value = AuthState.Idle }
}
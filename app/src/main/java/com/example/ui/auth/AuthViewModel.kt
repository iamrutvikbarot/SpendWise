package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DataStoreManager
import com.example.data.local.entity.User
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val userRepository: UserRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun register(fullName: String, email: String, pin: String, confirmPin: String) {
        if (fullName.isBlank() || email.isBlank() || pin.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }
        if (pin != confirmPin) {
            _authState.value = AuthState.Error("PINs do not match")
            return
        }
        if (pin.length != 4) {
            _authState.value = AuthState.Error("PIN must be 4 digits")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val existingUser = userRepository.getUserByEmail(email)
                if (existingUser != null) {
                    _authState.value = AuthState.Error("Email already registered")
                    return@launch
                }

                val hashedPin = hashPin(pin)
                val newUser = User(
                    fullName = fullName,
                    email = email,
                    pinHash = hashedPin
                )
                val userId = userRepository.insertUser(newUser).toInt()
                val createdUser = newUser.copy(id = userId)
                
                _authState.value = AuthState.Success(createdUser)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun login(email: String, pin: String) {
        if (email.isBlank() || pin.isBlank()) {
            _authState.value = AuthState.Error("Email and PIN are required")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = userRepository.getUserByEmail(email)
                if (user == null) {
                    _authState.value = AuthState.Error("User not found")
                    return@launch
                }

                if (user.pinHash != hashPin(pin)) {
                    _authState.value = AuthState.Error("Invalid PIN")
                    return@launch
                }

                dataStoreManager.saveLoginState(true, user.id)
                _authState.value = AuthState.Success(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

package com.victorgangas.arduinotb.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.victorgangas.arduinotb.data.local.database.AppDatabase
import com.victorgangas.arduinotb.data.preferences.UserPreferences
import com.victorgangas.arduinotb.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val userPreferences = UserPreferences(application)
    private val authRepository = AuthRepository(database.userDao(), userPreferences)
    
    // Estado de autenticación
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // Estado de carga
    var isLoading by mutableStateOf(false)
        private set
    
    // Mensaje de error
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    // Estado de sesión
    val isLoggedIn = userPreferences.isLoggedIn
    val username = userPreferences.username
    val userEmail = userPreferences.userEmail
    
    init {
        checkLoginStatus()
    }
    
    // Verificar si hay sesión activa
    private fun checkLoginStatus() {
        viewModelScope.launch {
            isLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }
    
    // Registrar usuario
    fun register(username: String, password: String, confirmPassword: String, email: String?) {
        // Validaciones
        if (username.isBlank()) {
            errorMessage = "El nombre de usuario no puede estar vacío"
            return
        }
        
        if (username.length < 3) {
            errorMessage = "El nombre de usuario debe tener al menos 3 caracteres"
            return
        }
        
        if (password.isBlank()) {
            errorMessage = "La contraseña no puede estar vacía"
            return
        }
        
        if (password.length < 6) {
            errorMessage = "La contraseña debe tener al menos 6 caracteres"
            return
        }
        
        if (password != confirmPassword) {
            errorMessage = "Las contraseñas no coinciden"
            return
        }
        
        // Validar email si se proporciona
        if (!email.isNullOrBlank() && !isValidEmail(email)) {
            errorMessage = "El correo electrónico no es válido"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            val result = authRepository.registerUser(username, password, email)
            
            result.fold(
                onSuccess = { user ->
                    // Auto-login después del registro
                    val loginResult = authRepository.login(username, password)
                    loginResult.fold(
                        onSuccess = {
                            _authState.value = AuthState.Authenticated
                            isLoading = false
                        },
                        onFailure = { e ->
                            errorMessage = e.message
                            isLoading = false
                        }
                    )
                },
                onFailure = { e ->
                    errorMessage = e.message
                    isLoading = false
                }
            )
        }
    }
    
    // Iniciar sesión
    fun login(username: String, password: String) {
        if (username.isBlank()) {
            errorMessage = "El nombre de usuario no puede estar vacío"
            return
        }
        
        if (password.isBlank()) {
            errorMessage = "La contraseña no puede estar vacía"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            val result = authRepository.login(username, password)
            
            result.fold(
                onSuccess = {
                    _authState.value = AuthState.Authenticated
                    isLoading = false
                },
                onFailure = { e ->
                    errorMessage = e.message
                    isLoading = false
                }
            )
        }
    }
    
    // Cerrar sesión
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    // Recuperar contraseña
    fun resetPassword(email: String, newPassword: String, confirmPassword: String) {
        if (email.isBlank()) {
            errorMessage = "El correo electrónico no puede estar vacío"
            return
        }
        
        if (!isValidEmail(email)) {
            errorMessage = "El correo electrónico no es válido"
            return
        }
        
        if (newPassword.isBlank()) {
            errorMessage = "La nueva contraseña no puede estar vacía"
            return
        }
        
        if (newPassword.length < 6) {
            errorMessage = "La contraseña debe tener al menos 6 caracteres"
            return
        }
        
        if (newPassword != confirmPassword) {
            errorMessage = "Las contraseñas no coinciden"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            val result = authRepository.resetPassword(email, newPassword)
            
            result.fold(
                onSuccess = {
                    _authState.value = AuthState.PasswordReset
                    isLoading = false
                },
                onFailure = { e ->
                    errorMessage = e.message
                    isLoading = false
                }
            )
        }
    }
    
    // Vincular email
    fun linkEmail(userId: Int, email: String) {
        if (!isValidEmail(email)) {
            errorMessage = "El correo electrónico no es válido"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            val result = authRepository.linkEmail(userId, email)
            
            result.fold(
                onSuccess = {
                    errorMessage = "Correo electrónico vinculado exitosamente"
                    isLoading = false
                },
                onFailure = { e ->
                    errorMessage = e.message
                    isLoading = false
                }
            )
        }
    }
    
    // Limpiar mensaje de error
    fun clearError() {
        errorMessage = null
    }
    
    // Resetear estado de autenticación
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
    
    // Validar email
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

// Estados de autenticación
sealed class AuthState {
    object Idle : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object PasswordReset : AuthState()
}


package com.victorgangas.arduinotb.data.repository

import com.victorgangas.arduinotb.data.local.dao.UserDao
import com.victorgangas.arduinotb.data.local.entity.User
import com.victorgangas.arduinotb.data.preferences.UserPreferences
import java.security.MessageDigest

class AuthRepository(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {
    
    // Registrar nuevo usuario
    suspend fun registerUser(username: String, password: String, email: String?): Result<User> {
        return try {
            // Verificar si el usuario ya existe
            if (userDao.isUsernameExists(username) > 0) {
                return Result.failure(Exception("El nombre de usuario ya está en uso"))
            }
            
            // Verificar si el email ya existe (si se proporcionó)
            if (!email.isNullOrEmpty() && userDao.isEmailExists(email) > 0) {
                return Result.failure(Exception("El correo electrónico ya está registrado"))
            }
            
            // Hashear la contraseña
            val hashedPassword = hashPassword(password)
            
            // Crear y guardar el usuario
            val user = User(
                username = username,
                password = hashedPassword,
                email = email
            )
            
            val userId = userDao.insertUser(user)
            val savedUser = user.copy(id = userId.toInt())
            
            Result.success(savedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Iniciar sesión
    suspend fun login(username: String, password: String): Result<User> {
        return try {
            val hashedPassword = hashPassword(password)
            val user = userDao.getUserByCredentials(username, hashedPassword)
            
            if (user != null) {
                // Guardar sesión
                userPreferences.saveUserSession(user.id, user.username, user.email)
                Result.success(user)
            } else {
                Result.failure(Exception("Usuario o contraseña incorrectos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Cerrar sesión
    suspend fun logout() {
        userPreferences.clearUserSession()
    }
    
    // Recuperar contraseña por email
    suspend fun resetPassword(email: String, newPassword: String): Result<Boolean> {
        return try {
            val user = userDao.getUserByEmail(email)
            
            if (user == null) {
                return Result.failure(Exception("No existe una cuenta con este correo electrónico"))
            }
            
            val hashedPassword = hashPassword(newPassword)
            val rowsUpdated = userDao.updatePasswordByEmail(email, hashedPassword)
            
            if (rowsUpdated > 0) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al actualizar la contraseña"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Verificar si hay email registrado
    suspend fun isEmailRegistered(username: String): Result<Boolean> {
        return try {
            val user = userDao.getUserByUsername(username)
            Result.success(!user?.email.isNullOrEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Vincular email a un usuario existente
    suspend fun linkEmail(userId: Int, email: String): Result<Boolean> {
        return try {
            // Verificar si el email ya existe
            if (userDao.isEmailExists(email) > 0) {
                return Result.failure(Exception("Este correo electrónico ya está registrado"))
            }
            
            val user = userDao.getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(email = email)
                userDao.updateUser(updatedUser)
                userPreferences.saveUserSession(user.id, user.username, email)
                Result.success(true)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Obtener información del usuario actual
    suspend fun getCurrentUser(userId: Int): Result<User> {
        return try {
            val user = userDao.getUserById(userId)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Cambiar contraseña del usuario actual
    suspend fun changePassword(userId: Int, currentPassword: String, newPassword: String): Result<Boolean> {
        return try {
            val user = userDao.getUserById(userId)
            
            if (user == null) {
                return Result.failure(Exception("Usuario no encontrado"))
            }
            
            // Verificar que la contraseña actual sea correcta
            val hashedCurrentPassword = hashPassword(currentPassword)
            if (user.password != hashedCurrentPassword) {
                return Result.failure(Exception("La contraseña actual es incorrecta"))
            }
            
            // Verificar que la nueva contraseña sea diferente
            val hashedNewPassword = hashPassword(newPassword)
            if (hashedCurrentPassword == hashedNewPassword) {
                return Result.failure(Exception("La nueva contraseña debe ser diferente a la actual"))
            }
            
            // Actualizar la contraseña
            val updatedUser = user.copy(password = hashedNewPassword)
            userDao.updateUser(updatedUser)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Hashear contraseña con SHA-256
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}


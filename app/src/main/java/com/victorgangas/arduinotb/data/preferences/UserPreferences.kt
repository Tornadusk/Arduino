package com.victorgangas.arduinotb.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    
    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = intPreferencesKey("user_id")
        private val USERNAME = stringPreferencesKey("username")
        private val USER_EMAIL = stringPreferencesKey("user_email")
    }
    
    // Flow para observar el estado de autenticación
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }
    
    val userId: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID]
        }
    
    val username: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USERNAME]
        }
    
    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL]
        }
    
    // Guardar sesión del usuario
    suspend fun saveUserSession(userId: Int, username: String, email: String?) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = userId
            preferences[USERNAME] = username
            if (email != null) {
                preferences[USER_EMAIL] = email
            }
        }
    }
    
    // Cerrar sesión
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            preferences.remove(USER_ID)
            preferences.remove(USERNAME)
            preferences.remove(USER_EMAIL)
        }
    }
    
    // Obtener datos de sesión de forma síncrona
    suspend fun getUserId(): Int? {
        var id: Int? = null
        context.dataStore.data.map { preferences ->
            id = preferences[USER_ID]
        }
        return id
    }
}


package com.victorgangas.arduinotb.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.victorgangas.arduinotb.data.local.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun getUserByCredentials(username: String, password: String): User?
    
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun isUsernameExists(username: String): Int
    
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun isEmailExists(email: String): Int
    
    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    suspend fun updatePasswordByEmail(email: String, newPassword: String): Int
}


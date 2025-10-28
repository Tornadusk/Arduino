package com.victorgangas.arduinotb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String, // Almacenada con hash
    val email: String? = null, // Email opcional para recuperación
    val createdAt: Long = System.currentTimeMillis()
)


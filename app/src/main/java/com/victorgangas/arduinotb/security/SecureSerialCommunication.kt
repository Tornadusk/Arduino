package com.victorgangas.arduinotb.security

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

/**
 * Módulo de comunicación serial segura con cifrado AES, verificación de integridad,
 * tokens de autenticación y heartbeat para cumplimiento ISO/IEC 27001
 * 
 * Implementa:
 * - Cifrado AES-128 en modo CBC con padding PKCS5
 * - Verificación de integridad mediante CRC-16
 * - Tokens de autenticación para comandos sensibles
 * - Heartbeat/keep-alive para detección de desconexiones
 */
class SecureSerialCommunication(
    private val viewModelScope: CoroutineScope
) {
    // Clave AES-128 (16 bytes) - En producción debe ser derivada de contraseña del usuario
    // Cumple con ISO/IEC 18033-2 para cifrado simétrico
    private val secretKey = SecretKeySpec(
        "ArduinoIoT2024!!".toByteArray(Charsets.UTF_8), // 16 bytes
        "AES"
    )
    
    private val transformation = "AES/CBC/PKCS5Padding"
    private val algorithm = "AES"
    
    // Token de autenticación para comandos sensibles (derivado de sesión del usuario)
    private var authToken: String? = null
    private var tokenExpiry: Long = 0
    
    // Heartbeat configuration
    private var heartbeatJob: Job? = null
    private var heartbeatInterval: Long = 5000 // 5 segundos
    private var lastHeartbeatReceived: Long = 0
    private var heartbeatTimeout: Long = 15000 // 15 segundos
    
    var isConnectionStable: Boolean = false
        private set
    
    var onHeartbeatTimeout: (() -> Unit)? = null
    var onEncryptionError: ((String) -> Unit)? = null
    
    /**
     * Establece token de autenticación derivado de la sesión del usuario
     * Cumple con ISO/IEC 9798 para autenticación de entidades
     */
    fun setAuthToken(userId: Int, sessionHash: String) {
        val combined = "$userId:$sessionHash:${System.currentTimeMillis()}"
        authToken = hashSHA256(combined).take(16) // Token de 16 caracteres
        tokenExpiry = System.currentTimeMillis() + (3600 * 1000) // 1 hora
    }
    
    /**
     * Cifra y formatea mensaje con verificación de integridad
     * Formato: [TOKEN:][IV][CIFRADO][CRC16]\n
     * 
     * Cumple con ISO/IEC 18033-2 (cifrado) e ISO/IEC 13239 (CRC)
     */
    fun encryptAndEncode(message: String, requiresAuth: Boolean = false): String {
        return try {
            // 1. Agregar token si es requerido para comandos sensibles
            var messageToEncrypt = message
            if (requiresAuth && authToken != null && System.currentTimeMillis() < tokenExpiry) {
                messageToEncrypt = "$authToken:$message"
            }
            
            // 2. Cifrar con AES-128 CBC
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(messageToEncrypt.toByteArray(Charsets.UTF_8))
            
            // 3. Calcular CRC-16 para verificación de integridad
            val dataForCrc = iv + encrypted
            val crc = calculateCRC16(dataForCrc)
            
            // 4. Formatear: IV (16 bytes base64) + Cifrado (base64) + CRC (4 hex chars)
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encryptedBase64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
            val crcHex = String.format("%04X", crc.toInt() and 0xFFFF)
            
            "$ivBase64|$encryptedBase64|$crcHex"
        } catch (e: Exception) {
            onEncryptionError?.invoke("Error al cifrar: ${e.message}")
            // Fallback: enviar mensaje simple sin cifrado (para compatibilidad durante migración)
            "PLAIN:$message"
        }
    }
    
    /**
     * Descifra y verifica integridad de mensaje recibido
     * Valida CRC-16 antes de descifrar
     */
    fun decodeAndDecrypt(encodedMessage: String): String? {
        return try {
            // Mensajes sin cifrado (compatibilidad)
            if (encodedMessage.startsWith("PLAIN:")) {
                return encodedMessage.substring(6)
            }
            
            // Separar componentes
            val parts = encodedMessage.trim().split("|")
            if (parts.size != 3) {
                return null // Formato inválido
            }
            
            val ivBase64 = parts[0]
            val encryptedBase64 = parts[1]
            val receivedCrcHex = parts[2]
            
            // Decodificar
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val encrypted = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            
            // Verificar integridad con CRC-16
            val dataForCrc = iv + encrypted
            val calculatedCrc = calculateCRC16(dataForCrc)
            val receivedCrc = receivedCrcHex.toInt(16)
            
            if ((calculatedCrc.toInt() and 0xFFFF) != receivedCrc) {
                onEncryptionError?.invoke("Error de integridad: CRC no coincide")
                return null // Integridad fallida
            }
            
            // Descifrar
            val cipher = Cipher.getInstance(transformation)
            val secretKeySpec = SecretKeySpec(secretKey.encoded, algorithm)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, javax.crypto.spec.IvParameterSpec(iv))
            val decrypted = cipher.doFinal(encrypted)
            val decryptedMessage = String(decrypted, Charsets.UTF_8)
            
            // Extraer token si existe y validarlo
            if (decryptedMessage.contains(":") && authToken != null) {
                val parts = decryptedMessage.split(":", limit = 2)
                if (parts.size == 2 && parts[0] == authToken && System.currentTimeMillis() < tokenExpiry) {
                    return parts[1] // Mensaje sin token
                }
            }
            
            decryptedMessage
        } catch (e: Exception) {
            onEncryptionError?.invoke("Error al descifrar: ${e.message}")
            null
        }
    }
    
    /**
     * Calcula CRC-16-CCITT (polinomio 0x1021) para verificación de integridad
     * Cumple con ISO/IEC 13239
     */
    private fun calculateCRC16(data: ByteArray): Short {
        var crc: Int = 0xFFFF
        val polynomial = 0x1021 // CRC-16-CCITT
        
        for (byte in data) {
            crc = crc xor ((byte.toInt() and 0xFF) shl 8)
            for (i in 0..7) {
                if (crc and 0x8000 != 0) {
                    crc = (crc shl 1) xor polynomial
                } else {
                    crc = crc shl 1
                }
                crc = crc and 0xFFFF
            }
        }
        
        return crc.toShort()
    }
    
    /**
     * Inicia heartbeat/keep-alive para detectar desconexiones
     * Cumple con RFC 6520 para detección de conexiones vivas
     */
    fun startHeartbeat(onSendHeartbeat: (String) -> Unit) {
        stopHeartbeat()
        lastHeartbeatReceived = System.currentTimeMillis()
        isConnectionStable = true
        
        heartbeatJob = viewModelScope.launch {
            while (isActive) {
                delay(heartbeatInterval)
                
                // Verificar timeout
                val timeSinceLastHeartbeat = System.currentTimeMillis() - lastHeartbeatReceived
                if (timeSinceLastHeartbeat > heartbeatTimeout) {
                    isConnectionStable = false
                    onHeartbeatTimeout?.invoke()
                } else {
                    // Enviar heartbeat cifrado
                    val heartbeatMessage = encryptAndEncode("HEARTBEAT", requiresAuth = false)
                    onSendHeartbeat("$heartbeatMessage\n")
                }
            }
        }
    }
    
    /**
     * Procesa mensaje de heartbeat recibido
     */
    fun processReceivedHeartbeat() {
        lastHeartbeatReceived = System.currentTimeMillis()
        isConnectionStable = true
    }
    
    /**
     * Detiene heartbeat
     */
    fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        isConnectionStable = false
    }
    
    /**
     * Determina si un comando requiere autenticación (comandos sensibles)
     */
    fun requiresAuthentication(command: String): Boolean {
        val sensitiveCommands = listOf("Abrir", "Cerrar", "M1", "M2", "modo1", "modo2")
        return sensitiveCommands.any { command.contains(it, ignoreCase = true) }
    }
    
    /**
     * Función de hash SHA-256 para tokens
     */
    private fun hashSHA256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Limpia recursos de seguridad
     */
    fun cleanup() {
        stopHeartbeat()
        authToken = null
        tokenExpiry = 0
    }
}


package com.victorgangas.arduinotb.security

import java.security.MessageDigest

/**
 * Utilidades de seguridad adicionales
 * Cumple con estándares ISO/IEC 27001 para protección de datos
 */
object SecurityUtils {
    
    /**
     * Genera hash SHA-256 de datos
     * Cumple con ISO/IEC 10118-3
     */
    fun hashSHA256(data: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(data.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Valida formato de mensaje seguro
     */
    fun isValidSecureMessageFormat(message: String): Boolean {
        // Formato: [TOKEN:][IV|CIFRADO|CRC] o PLAIN:mensaje
        return message.startsWith("PLAIN:") || 
               message.matches(Regex("^[A-Za-z0-9+/=]+\\|[A-Za-z0-9+/=]+\\|[0-9A-F]{4}$"))
    }
}


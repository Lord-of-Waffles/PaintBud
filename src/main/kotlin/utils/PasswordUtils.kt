package com.example.utils

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordUtils {
    fun hashPassword(password: String, salt: String): String {
        // Use a proper hashing algorithm like BCrypt, Argon2, or PBKDF2
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val passwordBytes = (password + salt).toByteArray()
        val hashBytes = messageDigest.digest(passwordBytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }
    
    fun verifyPassword(password: String, salt: String, storedHash: String): Boolean {
        val calculatedHash = hashPassword(password, salt)
        return calculatedHash == storedHash
    }
}
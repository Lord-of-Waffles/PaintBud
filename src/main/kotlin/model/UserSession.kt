package com.example.model

import io.ktor.server.auth.*
import kotlinx.serialization.Serializable

/**
 * Represents a user session for authentication
 */
@Serializable
data class UserSession(
    val userId: String,
    val username: String,
    val isLoggedIn: Boolean = true,
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 hours default
) : Principal {
    /**
     * Check if the session is still valid
     */
    val isValid: Boolean
        get() = isLoggedIn && System.currentTimeMillis() < expiresAt
}
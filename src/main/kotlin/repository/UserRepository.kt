package com.example.repository

import com.example.model.User
import com.example.services.FirebaseService

class UserRepository(private val firebaseService: FirebaseService) {
    
    /**
     * Find a user by username
     * @param username The username to search for
     * @return User object if found, null otherwise
     */
    suspend fun findByUsername(username: String): User? {
        val users = firebaseService.readData("users") as? Map<String, Any> ?: return null
        
        // Find user with matching username
        for ((userId, userData) in users) {
            if (userData is Map<*, *>) {
                val userMap = userData as Map<String, Any>
                if (userMap["username"] == username) {
                    return User(
                        id = userId,
                        username = userMap["username"] as? String ?: "",
                        passwordHash = userMap["passwordHash"] as? String ?: "",
                        salt = userMap["salt"] as? String ?: ""
                    )
                }
            }
        }
        
        return null
    }
    
    /**
     * Create a new user
     * @param username User's username
     * @param passwordHash Hashed password
     * @param salt Salt used for password hashing
     * @return Created User object
     */
    suspend fun createUser(username: String, passwordHash: String, salt: String): User {
        // Generate a new user ID
        val userId = firebaseService.generateKey("users")
        
        val user = User(
            id = userId,
            username = username,
            passwordHash = passwordHash,
            salt = salt
        )
        
        // Convert user to map
        val userValues = mapOf(
            "username" to user.username,
            "passwordHash" to user.passwordHash,
            "salt" to user.salt,
            "createdAt" to System.currentTimeMillis()
        )
        
        // Write user data to database
        val success = firebaseService.writeData("users/$userId", userValues)
        
        if (!success) {
            throw Exception("Failed to create user")
        }
        
        return user
    }
    
    /**
     * Update user's last login timestamp
     * @param userId The user's ID
     */
    suspend fun updateLastLogin(userId: String) {
        val updates = mapOf<String, Any>(
            "lastLogin" to System.currentTimeMillis()
        )
        
        val success = firebaseService.updateData("users/$userId", updates)
        
        if (!success) {
            throw Exception("Failed to update last login")
        }
    }
}
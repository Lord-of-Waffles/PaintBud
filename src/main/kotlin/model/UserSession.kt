package com.example.model 

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val userId: String,
    val username: String,
    val isLoggedIn: Boolean= true
)
package com.example.services

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service class for interacting with Firebase Realtime Database using Admin SDK
 */
class FirebaseService {
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    
    /**
     * Write data to a specified path
     * @param path The database path
     * @param data The data to write
     * @return true if successful
     */
    suspend fun writeData(path: String, data: Any): Boolean = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            try {
                // Admin SDK doesn't use Tasks API, it uses CompletionListener
                database.getReference(path).setValue(data) { error, ref ->
                    if (error != null) {
                        continuation.resumeWithException(Exception(error.message))
                    } else {
                        continuation.resume(true)
                    }
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    /**
     * Read data from a specified path
     * @param path The database path
     * @return The data as Map<String, Any> or null if not found
     */
    suspend fun readData(path: String): Any? = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            try {
                database.getReference(path).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            continuation.resume(snapshot.getValue())
                        } else {
                            continuation.resume(null)
                        }
                    }
                    
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(Exception(error.message))
                    }
                })
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    /**
     * Update specific fields at a path
     * @param path The database path
     * @param updates Map of field paths to values
     * @return true if successful
     */
    suspend fun updateData(path: String, updates: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            try {
                database.getReference(path).updateChildren(updates) { error, ref ->
                    if (error != null) {
                        continuation.resumeWithException(Exception(error.message))
                    } else {
                        continuation.resume(true)
                    }
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    /**
     * Delete data at a specified path
     * @param path The database path
     * @return true if successful
     */
    suspend fun deleteData(path: String): Boolean = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            try {
                database.getReference(path).removeValue { error, ref ->
                    if (error != null) {
                        continuation.resumeWithException(Exception(error.message))
                    } else {
                        continuation.resume(true)
                    }
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    /**
     * Listen for real-time updates at a path
     * @param path The database path
     * @param callback Function to call with updated data
     * @return The ValueEventListener that can be used to remove the listener later
     */
    fun listenForChanges(path: String, callback: (Any?) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.getValue())
            }
            
            override fun onCancelled(error: DatabaseError) {
                // Handle error
                println("Database error: ${error.message}")
            }
        }
        
        database.getReference(path).addValueEventListener(listener)
        return listener
    }
    
    /**
     * Stop listening for changes
     * @param path The database path
     * @param listener The listener to remove
     */
    fun removeListener(path: String, listener: ValueEventListener) {
        database.getReference(path).removeEventListener(listener)
    }
    
    /**
     * Generate a new unique key for a child item
     * @param path The parent path
     * @return A new unique key
     */
    fun generateKey(path: String): String {
        return database.getReference(path).push().key ?: 
            throw IllegalStateException("Failed to generate key")
    }
}
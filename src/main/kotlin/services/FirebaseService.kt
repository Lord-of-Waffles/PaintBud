package com.example.services

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service class for interacting with Firebase Firestore database
 */
class FirebaseService {
    private val db: Firestore by lazy { FirestoreClient.getFirestore() }
    
    /**
     * Add a document to a collection
     * @param collection The collection name
     * @param data The data to add
     * @return The ID of the created document
     */
    suspend fun addDocument(collection: String, data: Map<String, Any>): String = withContext(Dispatchers.IO) {
        val docRef = db.collection(collection).add(data).get()
        return@withContext docRef.id
    }
    
    /**
     * Get a document by its ID
     * @param collection The collection name
     * @param documentId The document ID
     * @return The document data or null if not found
     */
    suspend fun getDocument(collection: String, documentId: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        val documentSnapshot = db.collection(collection).document(documentId).get().get()
        return@withContext if (documentSnapshot.exists()) {
            documentSnapshot.data
        } else {
            null
        }
    }
    
    /**
     * Update a document
     * @param collection The collection name
     * @param documentId The document ID
     * @param data The new data
     * @return true if successful
     */
    suspend fun updateDocument(collection: String, documentId: String, data: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        db.collection(collection).document(documentId).update(data).get()
        return@withContext true
    }
    
    /**
     * Delete a document
     * @param collection The collection name
     * @param documentId The document ID
     * @return true if successful
     */
    suspend fun deleteDocument(collection: String, documentId: String): Boolean = withContext(Dispatchers.IO) {
        db.collection(collection).document(documentId).delete().get()
        return@withContext true
    }
    
    /**
     * Get all documents from a collection
     * @param collection The collection name
     * @return List of document data
     */
    suspend fun getAllDocuments(collection: String): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        val querySnapshot = db.collection(collection).get().get()
        return@withContext querySnapshot.documents.mapNotNull { 
            if (it.exists()) it.data else null 
        }
    }
}
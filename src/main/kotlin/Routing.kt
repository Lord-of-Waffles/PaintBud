package com.example

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.thymeleaf.*
import io.ktor.server.plugins.contentnegotiation.*
import com.example.services.FirebaseService

fun Application.configureRouting() {
    routing {
        val firebaseService = FirebaseService()


        get("/") {
            call.respondText("Hello World!")
        }

        get("/test1") {
            val text = "<h1>Hello From Ktor</h1>"
            val type = ContentType.parse("text/html")
            call.respondText(text, type)
        }
        
        get("/index") {
            call.respond(ThymeleafContent("index", mapOf("name" to "Ktor")))
        }

        // Firebase Realtime Database API routes
        route("/api") {
            // Create or update an item
            post("/items/{id}") {
                try {
                    val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest, 
                        mapOf("error" to "Missing item ID"))
                    
                    val data = call.receive<Map<String, Any>>()
                    val success = firebaseService.writeData("items/$id", data)
                    
                    if (success) {
                        call.respond(HttpStatusCode.Created, mapOf("id" to id))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to create item"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
            
            // Get an item by ID
            get("/items/{id}") {
                try {
                    val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, 
                        mapOf("error" to "Missing item ID"))
                    
                    val item = firebaseService.readData("items/$id")
                    
                    if (item != null) {
                        call.respond(item)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Item not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
            
            // Get all items - for Realtime Database this is just a path read
            get("/items") {
                try {
                    val items = firebaseService.readData("items")
                    if (items != null) {
                        call.respond(items)
                    } else {
                        call.respond(emptyMap<String, Any>())
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
            
            // Update an item
            patch("/items/{id}") {
                try {
                    val id = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest, 
                        mapOf("error" to "Missing item ID"))
                    
                    val updates = call.receive<Map<String, Any>>()
                    
                    val updated = firebaseService.updateData("items/$id", updates)
                    if (updated) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Item updated successfully"))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to update item"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
            
            // Delete an item
            delete("/items/{id}") {
                try {
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, 
                        mapOf("error" to "Missing item ID"))
                    
                    val deleted = firebaseService.deleteData("items/$id")
                    if (deleted) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete item"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
            
            // Generate a new ID for an item
            post("/items/keys") {
                try {
                    val key = firebaseService.generateKey("items")
                    call.respond(HttpStatusCode.OK, mapOf("key" to key))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        }
        
        // General purpose API for any path in the database
        route("/api/db") {
            // Create or update data at a path
            post("/{path...}") {
                try {
                    val path = call.parameters.getAll("path")?.joinToString("/") 
                        ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path is required"))
                    
                    val data = call.receive<Map<String, Any>>()
                    val success = firebaseService.writeData(path, data)
                    
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Data written successfully"))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to write data"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
            
            // Read data from a path
            get("/{path...}") {
                try {
                    val path = call.parameters.getAll("path")?.joinToString("/") 
                        ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path is required"))
                    
                    val data = firebaseService.readData(path)
                    
                    if (data != null) {
                        call.respond(data)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("message" to "No data found at specified path"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
            
            // Update specific fields at a path
            patch("/{path...}") {
                try {
                    val path = call.parameters.getAll("path")?.joinToString("/") 
                        ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path is required"))
                    
                    val updates = call.receive<Map<String, Any>>()
                    val success = firebaseService.updateData(path, updates)
                    
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Data updated successfully"))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to update data"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
            
            // Delete data at a path
            delete("/{path...}") {
                try {
                    val path = call.parameters.getAll("path")?.joinToString("/") 
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path is required"))
                    
                    val success = firebaseService.deleteData(path)
                    
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Data deleted successfully"))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete data"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
            
            // Generate new key for a path
            post("/{path...}/keys") {
                try {
                    val path = call.parameters.getAll("path")?.joinToString("/") 
                        ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path is required"))
                    
                    val key = firebaseService.generateKey(path)
                    call.respond(mapOf("key" to key))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
        }
    }
}
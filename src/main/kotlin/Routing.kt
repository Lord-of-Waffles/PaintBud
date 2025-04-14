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
        //works!
        get("/index") {
            call.respond(ThymeleafContent("index", mapOf("name" to "Ktor")))
        }

        // firebase stuff



        // API routes
        route("/api") {
            // Create a new item
            post("/items") {
                try {
                    val data = call.receive<Map<String, Any>>()
                    val documentId = firebaseService.addDocument("items", data)
                    call.respond(HttpStatusCode.Created, mapOf("id" to documentId))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
            
            // Get an item by ID
            get("/items/{id}") {
                try {
                    val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, 
                        mapOf("error" to "Missing item ID"))
                    
                    val item = firebaseService.getDocument("items", id)
                    
                    if (item != null) {
                        call.respond(item)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Item not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
            
            // Get all items
            get("/items") {
                try {
                    val items = firebaseService.getAllDocuments("items")
                    call.respond(items)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
            
            // Update an item
            put("/items/{id}") {
                try {
                    val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, 
                        mapOf("error" to "Missing item ID"))
                    
                    val data = call.receive<Map<String, Any>>()
                    
                    val updated = firebaseService.updateDocument("items", id, data)
                    if (updated) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Item updated successfully"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Item not found"))
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
                    
                    val deleted = firebaseService.deleteDocument("items", id)
                    if (deleted) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Item not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        
    }
}
}
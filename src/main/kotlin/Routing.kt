
package com.example

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.thymeleaf.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.sessions.*
import io.ktor.server.auth.*
import com.example.model.UserSession
import com.example.services.FirebaseService
import com.example.repository.UserRepository
import com.example.utils.PasswordUtils
import kotlinx.coroutines.delay
import kotlin.random.Random
import org.slf4j.LoggerFactory
import com.example.services.ColourService
import com.example.services.PaintColourService
import com.example.services.PaintColourServiceKey
import com.example.services.configurePaintColourService


// Move these functions outside of the routing block
suspend fun loadPaints(firebaseService: FirebaseService): Map<String, List<Map<String, Any>>> {
    val paintTypes = listOf("base", "layer", "dry", "technical", "contrast", "shade")
    val result = mutableMapOf<String, List<Map<String, Any>>>()

    for (type in paintTypes) {
        try {
            val paintsPath = "paints/$type"
            val paintsData = firebaseService.readData(paintsPath)
            
            // Check if paintsData is a list (Firebase returns a list of paint names)
            if (paintsData is List<*>) {
                // Convert the list of paint names to a list of maps with "id" and "name"
                val paintsList = paintsData.mapIndexed { index, paintName ->
                    mapOf(
                        "id" to index.toString(), // Use index as the ID
                        "name" to paintName!!
                    )
                }.sortedBy { it["name"] as String } // Sort by paint name
                
                result[type] = paintsList
            } else {
                println("Unexpected format for $type: $paintsData")
                result[type] = emptyList()
            }
        } catch (e: Exception) {
            // Log error but continue with other paint types
            println("Error loading paints for type $type: ${e.message}")
            result[type] = emptyList()
        }
    }
    
    return result
}



// Function to load paints used in a specific project
suspend fun loadProjectPaints(
    firebaseService: FirebaseService, 
    userId: String, 
    projectId: String, 
    availablePaints: Map<String, List<Map<String, Any>>>
): Map<String, List<Map<String, Any>>> {
    val projectPaints = mutableMapOf<String, List<Map<String, Any>>>()
    val paintTypes = listOf("base", "layer", "dry", "technical", "contrast", "shade")
    
    // Initialize all paint types with empty lists
    paintTypes.forEach { type ->
        projectPaints[type] = emptyList()
    }
    
    try {
        for (type in paintTypes) {
            val projectPaintsPath = "users/$userId/projects/$projectId/paints/$type"
            val projectPaintsData = firebaseService.readData(projectPaintsPath) as? Map<String, Any> ?: emptyMap()
            
            if (projectPaintsData.isNotEmpty()) {
                // Get all available paints of this type
                val typePaints = availablePaints[type] ?: emptyList()
                
                // Filter for paints used in this project
                val projectTypePaints = typePaints.filter { paint -> 
                    val paintId = paint["id"] as String
                    projectPaintsData.containsKey(paintId)
                }
                
                projectPaints[type] = projectTypePaints
            }
            // If no paints found, the default empty list remains
        }
    } catch (e: Exception) {
        println("Error loading project paints: ${e.message}")
    }
    
    return projectPaints
}

fun Application.configureRouting() {
    val logger = LoggerFactory.getLogger("Routing")
    val colourService = attributes[ColourServiceKey]

    routing {
        val firebaseService = FirebaseService()
        val userRepository = UserRepository(firebaseService)


        get("/") {
            call.respond(ThymeleafContent("index", mapOf("name" to "Ktor")))
        }

        // login/register

        get("/login") {
            //check if already logged in
            call.sessions.get<UserSession>()?.let {
                if (it.isLoggedIn) {
                    call.respondRedirect("/dashboard")
                    return@get
                }
            }
            call.respond(ThymeleafContent("login", mapOf("name" to "Ktor")))
        }

        post("/login") {
            try {
                val postParameters = call.receiveParameters()
                val username = postParameters["username"]?.trim() ?: ""
                val password = postParameters["password"] ?: ""
                
                // Validate input
                if (username.isEmpty() || password.isEmpty()) {
                    call.respond(ThymeleafContent("login", mapOf(
                        "message" to "Username and password are required",
                        "error" to true
                    )))
                    return@post
                }
                
                // Find user in database
                val user = userRepository.findByUsername(username)
                
                if (user != null && PasswordUtils.verifyPassword(password, user.salt, user.passwordHash)) {
                    // Create and store session with expiration
                    call.sessions.set(UserSession(
                        userId = user.id,
                        username = user.username,
                        isLoggedIn = true
                        // expiresAt is now provided by default in the UserSession class
                    ))
                    
                    // Log successful login
                    logger.info("User ${user.username} logged in successfully")
                    
                    // Update last login timestamp
                    userRepository.updateLastLogin(user.id)
                    
                    call.respondRedirect("/dashboard")
                } else {
                    // Add a small delay to prevent timing attacks
                    delay(Random.nextLong(100, 300))
                    
                    // Log failed login attempt
                    logger.warn("Failed login attempt for username: $username")
                    
                    call.respond(ThymeleafContent("login", mapOf(
                        "message" to "Invalid username or password",
                        "error" to true
                    )))
                }
            } catch (e: Exception) {
                logger.error("Login error", e)
                call.respond(ThymeleafContent("login", mapOf(
                    "message" to "An error occurred. Please try again later.",
                    "error" to true
                )))
            }
        }

        // Protected routes (requiring authentication)
        // Updated Dashboard Route
        get("/dashboard") {
            val userSession = call.sessions.get<UserSession>()
            if (userSession != null && userSession.isLoggedIn) {
                try {
                    // Get user's projects
                    val userProjectsPath = "users/${userSession.userId}/projects"
                    val userProjects = firebaseService.readData(userProjectsPath) as? Map<String, Any> ?: emptyMap()
                    
                    // Convert the map to a list of projects WITH FORMATTED DATES (just like in the projects route)
                    val projectsList = userProjects.map { (_, value) -> 
                        val projectMap = value as Map<String, Any>
                        // Create a mutable map from the project data
                        val mutableProject = projectMap.toMutableMap()
                        
                        // Format the date
                        val createdAt = projectMap["createdAt"] as? Long ?: 0L
                        val formattedDate = java.text.SimpleDateFormat("MMM dd, yyyy").format(java.util.Date(createdAt))
                        
                        // Add the formatted date to the project
                        mutableProject["formattedDate"] = formattedDate
                        
                        mutableProject
                    }.sortedByDescending { it["createdAt"] as Long }
                    
                    // Create activity list (combine recent projects and supplies)
                    val recentActivity = mutableListOf<Map<String, Any>>()
                    
                    // Add projects to activity with type
                    projectsList.forEach { project ->
                        recentActivity.add(mapOf(
                            "type" to "project",
                            "id" to (project["projectId"] ?: ""),
                            "name" to (project["projectName"] ?: ""),
                            "timestamp" to (project["createdAt"] ?: 0L)
                        ))
                    }
                    
                    // Sort activity by timestamp (most recent first)
                    val sortedActivity = recentActivity.sortedByDescending { it["timestamp"] as Long }
                    
                    call.respond(ThymeleafContent("dashboard", mapOf(
                        "username" to userSession.username,
                        "projects" to projectsList,
                        "recentActivity" to sortedActivity
                    )))
                } catch (e: Exception) {
                    call.respond(ThymeleafContent("dashboard", mapOf(
                        "username" to userSession.username,
                        "error" to true,
                        "message" to "Failed to load dashboard data: ${e.message}"
                    )))
                }
            } else {
                call.respondRedirect("/login")
            }
        }
                        
        // Logout route
        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("/login")
        }

        // registering
        get("/register") {
            call.sessions.get<UserSession>()?.let {
                if (it.isLoggedIn) {
                    call.respondRedirect("/dashboard")
                    return@get
                }
            }
            call.respond(ThymeleafContent("register", mapOf()))
        }

        post("/register") {
            val postParameters = call.receiveParameters()
            val username = postParameters["username"] ?: ""
            val email = postParameters["email"] ?: ""
            val password = postParameters["password"] ?: ""
            val confirmPassword = postParameters["confirmPassword"] ?: ""
        
            when {
                username.isBlank() -> {
                    call.respond(ThymeleafContent("register", mapOf(
                        "error" to true,
                        "message" to "Username cannot be empty"
                    )))
                    return@post
                }
                email.isBlank() -> {
                    call.respond(ThymeleafContent("register", mapOf(
                        "error" to true,
                        "message" to "Email cannot be empty"
                    )))
                    return@post
                }
                password.isBlank() -> {
                    call.respond(ThymeleafContent("register", mapOf(
                        "error" to true,
                        "message" to "Password cannot be empty"
                    )))
                    return@post
                }
                password != confirmPassword -> {
                    call.respond(ThymeleafContent("register", mapOf(
                        "error" to true,
                        "message" to "Passwords do not match"
                    )))
                    return@post
                }
            }
        
            try {
                // Check if username already exists
                val existingUser = userRepository.findByUsername(username)
                
                if (existingUser != null) {
                    call.respond(ThymeleafContent("register", mapOf(
                        "error" to true,
                        "message" to "Username already exists, please choose another"
                    )))
                    return@post
                }
        
                // Generate salt and hash password
                val salt = PasswordUtils.generateSalt()
                val passwordHash = PasswordUtils.hashPassword(password, salt)
                
                // Create user with secure password
                val user = userRepository.createUser(username, passwordHash, salt)
        
                // Create user session
                call.sessions.set(UserSession(
                    userId = user.id,
                    username = user.username,
                    isLoggedIn = true
                ))
                
                // Log successful registration
                logger.info("New user registered: $username")
                
                call.respondRedirect("/dashboard")
            } catch (e: Exception) {
                logger.error("Registration error", e)
                call.respond(ThymeleafContent("register", mapOf(
                    "error" to true,
                    "message" to "An error occurred: ${e.message}"
                )))
            }
        }

        // projects
        // Complete implementation for add_project
        post("/add_project") {
            // Check if user is logged in
            val userSession = call.sessions.get<UserSession>()
            if (userSession == null || !userSession.isLoggedIn) {
                call.respondRedirect("/login")
                return@post
            }

            val postParameters = call.receiveParameters()
            val projectName = postParameters["projectName"] ?: ""
            val projectDescription = postParameters["projectDescription"] ?: ""
            
            if (projectName.isBlank()) {
                call.respond(ThymeleafContent("add_project", mapOf(
                    "error" to true,
                    "message" to "Project name cannot be empty"
                )))
                return@post
            }

            try {
                // Generate a unique key for the project
                val projectId = firebaseService.generateKey("users/${userSession.userId}/projects")
                
                // Create project data
                val projectData = mapOf(
                    "projectId" to projectId,
                    "projectName" to projectName,
                    "projectDescription" to projectDescription,
                    "createdAt" to System.currentTimeMillis()
                )
                
                // Save project directly under the user's data
                val userProjectPath = "users/${userSession.userId}/projects/$projectId"
                val success = firebaseService.writeData(userProjectPath, projectData)
                
                if (success) {
                    call.respondRedirect("/projects")
                } else {
                    call.respond(ThymeleafContent("add_project", mapOf(
                        "error" to true,
                        "message" to "Failed to create project. Please try again."
                    )))
                }
            } catch (e: Exception) {
                call.respond(ThymeleafContent("add_project", mapOf(
                    "error" to true,
                    "message" to "An error occurred: ${e.message}"
                )))
            }
        }

        // Implementation for listing projects on the projects page
// Updated projects route to work with existing Firebase data structure
get("/projects") {
    // Check if user is logged in
    val userSession = call.sessions.get<UserSession>()
    if (userSession == null || !userSession.isLoggedIn) {
        call.respondRedirect("/login")
        return@get
    }
    
    try {
        // Get user's projects
        val userProjectsPath = "users/${userSession.userId}/projects"
        val userProjects = firebaseService.readData(userProjectsPath) as? Map<String, Any> ?: emptyMap()
        
        // Get the PaintColourService
        val paintColourService = application.attributes[PaintColourServiceKey]
        
        // Load all paints data
        val paintsData = firebaseService.readData("paints") as? Map<String, Any> ?: emptyMap()
        
        // Convert the map to a list of projects with formatted dates and paint data
        val projectsList = userProjects.map { (projectId, value) -> 
            val projectMap = value as Map<String, Any>
            // Create a mutable map from the project data
            val mutableProject = projectMap.toMutableMap()
            
            // Format the date
            val createdAt = projectMap["createdAt"] as? Long ?: 0L
            val formattedDate = java.text.SimpleDateFormat("MMM dd, yyyy").format(java.util.Date(createdAt))
            
            // Add the formatted date to the project
            mutableProject["formattedDate"] = formattedDate
            
            // Process project paints
            val projectPaints = mutableMapOf<String, List<Map<String, Any>>>()
            var totalPaintCount = 0
            
            // Get project paints if available
            val projectPaintsData = (projectMap["paints"] as? Map<String, Any>) ?: emptyMap()
            
            // Process each paint type
            for (paintType in listOf("base", "layer", "contrast", "dry", "shade", "technical")) {
                val typePaints = (projectPaintsData[paintType] as? Map<String, Any>) ?: emptyMap()
                
                if (typePaints.isNotEmpty()) {
                    // Get array of this paint type from the database
                    val paintsArray = (paintsData[paintType] as? List<*>) ?: emptyList<String>()
                    
                    // Process each paint in this type
                    val paintsOfType = typePaints.keys.mapNotNull { paintIndex ->
                        val index = paintIndex.toIntOrNull() ?: return@mapNotNull null
                        if (index < paintsArray.size) {
                            val paintName = paintsArray[index] as? String ?: return@mapNotNull null
                            val paintColour = paintColourService.getPaintColour(paintName, paintType)
                            
                            mapOf(
                                "id" to paintIndex,
                                "name" to paintName,
                                "colour" to paintColour
                            )
                        } else null
                    }
                    
                    if (paintsOfType.isNotEmpty()) {
                        projectPaints[paintType] = paintsOfType
                        totalPaintCount += paintsOfType.size
                    }
                }
            }
            
            // Add paint data to project
            mutableProject["paints"] = projectPaints
            mutableProject["paintCount"] = totalPaintCount
            
            mutableProject
        }
        
        // Sort projects by creation date (newest first)
        val sortedProjects = projectsList.sortedByDescending { it["createdAt"] as Long }
        
        call.respond(ThymeleafContent("projects", mapOf(
            "projects" to sortedProjects,
            "username" to userSession.username
        )))
    } catch (e: Exception) {
        logger.error("Failed to load projects: ${e.message}", e)
        call.respond(ThymeleafContent("projects", mapOf(
            "error" to true,
            "message" to "Failed to load projects: ${e.message}",
            "username" to userSession.username
        )))
    }
}

        get("/add_project") {
            // Check if user is logged in
            val userSession = call.sessions.get<UserSession>()
            if (userSession == null || !userSession.isLoggedIn) {
                call.respondRedirect("/login")
                return@get
            }
            
            // Render the add_project form template
            call.respond(ThymeleafContent("add_project", mapOf(
                "username" to userSession.username
            )))
        }

        // UPDATED: Modified edit_project route to include paints
        get("/edit_project/{id}") {
            val userSession = call.sessions.get<UserSession>()
            if (userSession == null || !userSession.isLoggedIn) {
                call.respondRedirect("/login")
                return@get
            }
            
            val projectId = call.parameters["id"] ?: return@get call.respondRedirect("/projects")
            
            try {
                // Get the project data directly from the user's projects
                val projectPath = "users/${userSession.userId}/projects/$projectId"
                val projectData = firebaseService.readData(projectPath) as? Map<String, Any>
                
                if (projectData != null) {
                    // Create a mutable map from the project data
                    val projectWithDates = projectData.toMutableMap()
                    
                    // Format dates
                    val createdAt = projectData["createdAt"] as? Long ?: 0L
                    val formattedCreatedAt = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm").format(java.util.Date(createdAt))
                    projectWithDates["formattedCreatedAt"] = formattedCreatedAt
                    
                    // Format updated date if available
                    val updatedAt = projectData["updatedAt"] as? Long
                    if (updatedAt != null) {
                        val formattedUpdatedAt = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm").format(java.util.Date(updatedAt))
                        projectWithDates["formattedUpdatedAt"] = formattedUpdatedAt
                    }
                    
                    // Load all available paints
                    val availablePaints = loadPaints(firebaseService)
                    
                    // Load project paints
                    val projectPaints = loadProjectPaints(firebaseService, userSession.userId, projectId, availablePaints)
                    
                    call.respond(ThymeleafContent("edit_project", mapOf(
                        "project" to projectWithDates,
                        "username" to userSession.username,
                        "availablePaints" to availablePaints,
                        "projectPaints" to projectPaints
                    )))
                } else {
                    call.respondRedirect("/projects")
                }
            } catch (e: Exception) {
                call.respond(ThymeleafContent("projects", mapOf(
                    "error" to true,
                    "message" to "Failed to load project: ${e.message}",
                    "username" to userSession.username
                )))
            }
        }

        post("/edit_project/{id}") {
            val userSession = call.sessions.get<UserSession>()
            if (userSession == null || !userSession.isLoggedIn) {
                call.respondRedirect("/login")
                return@post
            }
            
            val projectId = call.parameters["id"] ?: return@post call.respondRedirect("/projects")
            
            val postParameters = call.receiveParameters()
            val projectName = postParameters["projectName"] ?: ""
            val projectDescription = postParameters["projectDescription"] ?: ""
            
            if (projectName.isBlank()) {
                call.respond(ThymeleafContent("edit_project", mapOf(
                    "error" to true,
                    "message" to "Project name cannot be empty",
                    "username" to userSession.username
                )))
                return@post
            }
            
            try {
                // Update project data directly in the user's projects
                val projectPath = "users/${userSession.userId}/projects/$projectId"
                val updates = mapOf(
                    "projectName" to projectName,
                    "projectDescription" to projectDescription,
                    "updatedAt" to System.currentTimeMillis()
                )
                
                val success = firebaseService.updateData(projectPath, updates)
                
                if (success) {
                    call.respondRedirect("/projects")
                } else {
                    call.respond(ThymeleafContent("edit_project", mapOf(
                        "error" to true,
                        "message" to "Failed to update project. Please try again.",
                        "username" to userSession.username
                    )))
                }
            } catch (e: Exception) {
                call.respond(ThymeleafContent("edit_project", mapOf(
                    "error" to true, 
                    "message" to "An error occurred: ${e.message}",
                    "username" to userSession.username
                )))
            }
        }

        // Implementation for deleting a project
        get("/delete_project/{id}") {
            val userSession = call.sessions.get<UserSession>()
            if (userSession == null || !userSession.isLoggedIn) {
                call.respondRedirect("/login")
                return@get
            }
            
            val projectId = call.parameters["id"] ?: return@get call.respondRedirect("/projects")
            
            try {
                // Delete the project directly from the user's projects
                val projectPath = "users/${userSession.userId}/projects/$projectId"
                val success = firebaseService.deleteData(projectPath)
                
                if (success) {
                    call.respondRedirect("/projects")
                } else {
                    call.respond(ThymeleafContent("projects", mapOf(
                        "error" to true,
                        "message" to "Failed to delete project",
                        "username" to userSession.username
                    )))
                }
            } catch (e: Exception) {
                call.respond(ThymeleafContent("projects", mapOf(
                    "error" to true,
                    "message" to "Failed to delete project: ${e.message}",
                    "username" to userSession.username
                )))
            }
        }

        // Add API routes for paint management
        // Add API route to add a paint to a project
        post("/api/project/{projectId}/add-paint") {
            val userSession = call.sessions.get<UserSession>()
            if (userSession == null || !userSession.isLoggedIn) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }
            
            val projectId = call.parameters["projectId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            
            try {
                val postParameters = call.receiveParameters()
                val paintType = postParameters["paintType"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val paintId = postParameters["paintId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                
                // Path to project paint
                val paintPath = "users/${userSession.userId}/projects/$projectId/paints/$paintType/$paintId"
                
                // Add the paint to the project
                val success = firebaseService.writeData(paintPath, true)
                
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        // Add API route to remove a paint from a project
        post("/api/project/{projectId}/remove-paint") {
            val userSession = call.sessions.get<UserSession>()
            if (userSession == null || !userSession.isLoggedIn) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }
            
            val projectId = call.parameters["projectId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            
            try {
                val postParameters = call.receiveParameters()
                val paintType = postParameters["paintType"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val paintId = postParameters["paintId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                
                // Path to project paint
                val paintPath = "users/${userSession.userId}/projects/$projectId/paints/$paintType/$paintId"
                
                // Remove the paint from the project
                val success = firebaseService.deleteData(paintPath)
                
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

    // api routes for palette generator

    route("/api/palettes") {
        get("/random") {
            val count = call.parameters["count"]?.toIntOrNull() ?: 5
            val palette = colourService.generateRandomPalette(count)
            call.respond(mapOf("colours" to palette))
        }

        //different endpoints for other methods of palette generators go here (if i feel like making any teehee)
    }

    get("/palette") {
        call.respond(ThymeleafContent("palette", mapOf("name" to "Ktor")))
    }
    
    get("/wishlist") {
        call.respond(ThymeleafContent("wishlist", mapOf("name" to "Ktor")))
    }

    get("/about") {
        call.respond(ThymeleafContent("about", mapOf("name" to "Ktor")))
    }

    get("/contact") {
        call.respond(ThymeleafContent("contact", mapOf("name" to "Ktor")))
    }
}
}
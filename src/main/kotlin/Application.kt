package com.example

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase
import io.ktor.server.application.*
import io.ktor.server.thymeleaf.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.sessions.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.util.*
import com.example.model.UserSession
import io.ktor.serialization.jackson.*
import io.ktor.server.plugins.contentnegotiation.*
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.io.FileInputStream
import com.example.services.ColourService
import com.example.services.PaintColourService
import com.example.services.configurePaintColourService


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

val ColourServiceKey = AttributeKey<ColourService>("ColourService")

fun Application.module() {
    // Initialize Firebase
    configureFirebase()

    // Install content negotiation for JSON handling
    install(ContentNegotiation) {
        jackson()
    }

    configurePaintColourService()


val colourService = ColourService()
attributes.put(ColourServiceKey, colourService)


install(Sessions) {
    cookie<UserSession>("user_session") {
        cookie.path = "/"
        cookie.maxAgeInSeconds = 60 * 60 * 24 // 24 hours
        cookie.secure = false // Set to true in production
        cookie.httpOnly = true
    }
}

    // Install Authentication before routing
    install(Authentication) {
        form("auth-form") {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                // replace code below with actual validation when db is set up
                if (credentials.name == "admin" && credentials.password == "password") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            } 
            challenge {
                // redirect to login page if auth fails
                call.respondRedirect("/login")
            }
        }
    }

    // Install Thymeleaf 
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/"
            suffix = ".html"
            characterEncoding = "UTF-8"
        })
    }

    // Configure static resources
    routing {
        static("/static") {
            resources("static")
        }
    }

    // Configure routing AFTER all plugins are installed
    configureRouting()
}


fun Application.configureFirebase() {
    try {
        // Load the service account key JSON file
        // You need to download this from Firebase console
        val serviceAccount = FileInputStream("src/main/resources/fb_prkey.json")
        
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://paintbud-845c3-default-rtdb.europe-west1.firebasedatabase.app/")
            .build()
        
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
            log.info("Firebase has been initialized successfully")
        }
    } catch (e: Exception) {
        log.error("Error initializing Firebase", e)
    }
}
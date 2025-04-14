package com.example

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.*
import io.ktor.server.thymeleaf.*
import io.ktor.server.routing.*
import io.ktor.serialization.jackson.*
import io.ktor.server.plugins.contentnegotiation.*
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.io.FileInputStream

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Initialize Firebase
    configureFirebase()

    // Install existing Thymeleaf templating
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/"
            suffix = ".html"
            characterEncoding = "UTF-8"
        })
    }

    // Install content negotiation for JSON handling
    install(ContentNegotiation) {
        jackson()
    }

    // Configure routing
    configureRouting()
}

fun Application.configureFirebase() {
    try {
        // Load the service account key JSON file
        // You need to download this from Firebase console
        val serviceAccount = FileInputStream("/src/main/resources/fb_prkey.json")
        
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
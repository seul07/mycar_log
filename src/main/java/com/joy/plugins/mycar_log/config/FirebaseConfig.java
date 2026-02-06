package com.joy.plugins.mycar_log.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.path:classpath:firebase-service-account.json}")
    private Resource firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(firebaseConfigPath.getInputStream()))
                        .build();
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            // Firebase config not found - running in development mode without Firebase
            System.out.println("Firebase configuration not found. Running without Firebase authentication.");
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseAuth.getInstance();
        }
        return null;
    }
}

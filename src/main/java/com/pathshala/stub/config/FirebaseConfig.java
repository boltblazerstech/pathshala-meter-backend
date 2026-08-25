package com.pathshala.stub.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account.path:}")
    private String serviceAccountPath;

    @PostConstruct
    public void initialize() {
        try {
            if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
                System.out.println("No Firebase service account path provided. FCM pushes will be disabled.");
                return;
            }

            FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK initialized successfully.");
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase Admin SDK: " + e.getMessage());
        }
    }
}

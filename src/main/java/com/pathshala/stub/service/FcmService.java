package com.pathshala.stub.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FcmService {

    /**
     * Sends a silent data push to the given FCM token to trigger an on-demand location fetch.
     */
    public void sendOnDemandLocationRequest(String fcmToken, UUID userId) {
        if (fcmToken == null || fcmToken.isBlank()) {
            System.out.println("No FCM token for user " + userId + ", skipping push.");
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            System.out.println("Firebase is not initialized. Skipping FCM push for user " + userId);
            return;
        }

        // We use putData (data message) instead of setNotification so it's a silent push
        // that wakes up the background isolate in Flutter.
        Message message = Message.builder()
                .putData("action", "REFRESH_LOCATION")
                .putData("requested_at", String.valueOf(System.currentTimeMillis()))
                .setToken(fcmToken)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent FCM message: " + response + " to user " + userId);
        } catch (Exception e) {
            System.err.println("Error sending FCM message to user " + userId + ": " + e.getMessage());
        }
    }
}

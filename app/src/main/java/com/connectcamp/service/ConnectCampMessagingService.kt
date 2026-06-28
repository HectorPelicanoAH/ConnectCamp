package com.connectcamp.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ConnectCampMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Handle incoming FCM messages (new chat messages, etc.)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to Firestore for push notifications
    }
}

package com.connectcamp.data.model

import com.google.firebase.firestore.DocumentId

enum class UserRole {
    PRODUCER, CONSUMER
}

data class User(
    @DocumentId
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.CONSUMER,
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

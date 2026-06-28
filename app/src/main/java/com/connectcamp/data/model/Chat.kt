package com.connectcamp.data.model

import com.google.firebase.firestore.DocumentId

data class ChatMessage(
    @DocumentId
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class Chat(
    @DocumentId
    val id: String = "",
    val producerId: String = "",
    val producerName: String = "",
    val consumerId: String = "",
    val consumerName: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val unreadCountProducer: Int = 0,
    val unreadCountConsumer: Int = 0
) {
    fun getChatId(uid1: String, uid2: String): String {
        return listOf(uid1, uid2).sorted().joinToString("_")
    }
}

package com.connectcamp.data.repository

import com.connectcamp.data.model.Chat
import com.connectcamp.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val chatsCollection = firestore.collection("chats")
    private val messagesCollection = firestore.collection("messages")

    private fun buildChatId(uid1: String, uid2: String): String =
        listOf(uid1, uid2).sorted().joinToString("_")

    suspend fun getOrCreateChat(
        producerId: String,
        producerName: String,
        consumerId: String,
        consumerName: String
    ): String {
        val chatId = buildChatId(producerId, consumerId)
        val doc = chatsCollection.document(chatId).get().await()
        if (!doc.exists()) {
            val chat = Chat(
                id = chatId,
                producerId = producerId,
                producerName = producerName,
                consumerId = consumerId,
                consumerName = consumerName
            )
            chatsCollection.document(chatId).set(chat).await()
        }
        return chatId
    }

    fun getChatsFlow(): Flow<List<Chat>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listenerProducer = chatsCollection.whereEqualTo("producerId", uid)
            .addSnapshotListener { snapshot, _ ->
                // handled by combined listener below
            }
        listenerProducer.remove()

        val listener = chatsCollection
            .whereIn("producerId", listOf(uid))
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val chats = snapshot?.documents?.mapNotNull { it.toObject(Chat::class.java) }
                trySend(chats ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    fun getChatsForUserFlow(uid: String, isProducer: Boolean): Flow<List<Chat>> = callbackFlow {
        val field = if (isProducer) "producerId" else "consumerId"
        val listener = chatsCollection.whereEqualTo(field, uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val chats = snapshot?.documents?.mapNotNull { it.toObject(Chat::class.java) }
                    ?.sortedByDescending { it.lastMessageTimestamp }
                trySend(chats ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    fun getMessagesFlow(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = messagesCollection
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val messages = snapshot?.documents?.mapNotNull { it.toObject(ChatMessage::class.java) }
                trySend(messages ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(chatId: String, text: String, senderName: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val message = ChatMessage(
                chatId = chatId,
                senderId = uid,
                senderName = senderName,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            messagesCollection.add(message).await()
            chatsCollection.document(chatId).update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageTimestamp" to message.timestamp
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markMessagesAsRead(chatId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val unreadMessages = messagesCollection
                .whereEqualTo("chatId", chatId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
                .documents
                .filter { it.getString("senderId") != uid }

            val batch = firestore.batch()
            unreadMessages.forEach { batch.update(it.reference, "isRead", true) }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

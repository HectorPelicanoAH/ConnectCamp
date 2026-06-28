package com.connectcamp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectcamp.data.model.Chat
import com.connectcamp.data.model.ChatMessage
import com.connectcamp.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _activeChatId = MutableStateFlow<String?>(null)

    fun loadChats(uid: String, isProducer: Boolean) {
        viewModelScope.launch {
            chatRepository.getChatsForUserFlow(uid, isProducer).collect {
                _chats.value = it
            }
        }
    }

    fun openChat(chatId: String) {
        _activeChatId.value = chatId
        viewModelScope.launch {
            chatRepository.getMessagesFlow(chatId).collect {
                _messages.value = it
            }
        }
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(chatId)
        }
    }

    fun sendMessage(chatId: String, text: String, senderName: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(chatId, text, senderName)
        }
    }

    suspend fun getOrCreateChat(
        producerId: String,
        producerName: String,
        consumerId: String,
        consumerName: String
    ): String {
        return chatRepository.getOrCreateChat(producerId, producerName, consumerId, consumerName)
    }
}

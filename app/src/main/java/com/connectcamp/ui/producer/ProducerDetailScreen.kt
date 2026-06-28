package com.connectcamp.ui.producer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.connectcamp.data.model.Product
import com.connectcamp.data.model.ProducerProfile
import com.connectcamp.data.model.UserRole
import com.connectcamp.viewmodel.AuthViewModel
import com.connectcamp.viewmodel.ChatViewModel
import com.connectcamp.viewmodel.ProducerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerDetailScreen(
    producerId: String,
    authViewModel: AuthViewModel,
    producerViewModel: ProducerViewModel,
    chatViewModel: ChatViewModel,
    onNavigateToChat: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val allProducers by producerViewModel.allProducers.collectAsState()

    val producer = allProducers.find { it.uid == producerId }
    var producerProfile by remember { mutableStateOf<ProducerProfile?>(null) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(producerId) {
        // In a real app this would use a dedicated use case; here we reuse repository methods
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(producer?.fullName ?: "Productor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (producer == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(producer.fullName, style = MaterialTheme.typography.headlineSmall)
                    if (producer.phone.isNotBlank()) {
                        Text(
                            producer.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Efectivo") },
                            leadingIcon = { Icon(Icons.Default.LocalAtm, contentDescription = null) }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("Envío") },
                            leadingIcon = { Icon(Icons.Default.DeliveryDining, contentDescription = null) }
                        )
                    }
                }

                item {
                    if (currentUser?.role == UserRole.CONSUMER) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val chatId = chatViewModel.getOrCreateChat(
                                        producerId = producerId,
                                        producerName = producer.fullName,
                                        consumerId = currentUser!!.uid,
                                        consumerName = currentUser!!.fullName
                                    )
                                    onNavigateToChat(chatId, producer.fullName)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                            Text("Contactar productor")
                        }
                    }
                }
            }
        }
    }
}

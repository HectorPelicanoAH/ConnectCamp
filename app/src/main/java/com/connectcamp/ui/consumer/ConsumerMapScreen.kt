package com.connectcamp.ui.consumer

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.connectcamp.data.model.User
import com.connectcamp.data.model.UserRole
import com.connectcamp.viewmodel.AuthViewModel
import com.connectcamp.viewmodel.ChatViewModel
import com.connectcamp.viewmodel.ConsumerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ConsumerMapScreen(
    consumerViewModel: ConsumerViewModel,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    onNavigateToChat: (String, String) -> Unit,
    onNavigateToProducerDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val allProducers by consumerViewModel.allProducers.collectAsState()

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var selectedProducer by remember { mutableStateOf<User?>(null) }
    val scope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(40.4168, -3.7038), 6f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productores cerca de ti") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = locationPermission.status.isGranted
                )
            ) {
                // In a real implementation we would fetch ProducerProfile for each producer
                // and display markers at their registered locations.
                // For now we show a placeholder marker for producers without location data.
                allProducers.forEach { producer ->
                    // Placeholder position — real location fetched from ProducerProfile
                    val position = LatLng(40.4168 + (producer.uid.hashCode() % 100) * 0.01, -3.7038)
                    Marker(
                        state = MarkerState(position = position),
                        title = producer.fullName,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                        onClick = {
                            selectedProducer = producer
                            false
                        }
                    )
                }
            }

            selectedProducer?.let { producer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(producer.fullName, style = MaterialTheme.typography.titleMedium)
                        if (producer.phone.isNotBlank()) {
                            Text(
                                producer.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = { onNavigateToProducerDetail(producer.uid) }
                        ) {
                            Text("Ver perfil")
                        }
                        if (currentUser?.role == UserRole.CONSUMER) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        val chatId = chatViewModel.getOrCreateChat(
                                            producerId = producer.uid,
                                            producerName = producer.fullName,
                                            consumerId = currentUser!!.uid,
                                            consumerName = currentUser!!.fullName
                                        )
                                        onNavigateToChat(chatId, producer.fullName)
                                    }
                                }
                            ) {
                                Text("Contactar")
                            }
                        }
                    }
                }
            }
        }
    }
}

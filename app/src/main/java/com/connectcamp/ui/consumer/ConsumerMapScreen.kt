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
import com.connectcamp.data.model.ProducerWithLocation
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
    val producersWithLocation by consumerViewModel.producersWithLocation.collectAsState()
    val allProducers by consumerViewModel.allProducers.collectAsState()
    // Map uid -> User so we can look up fullName/phone for map markers
    val producerUserMap = remember(allProducers) { allProducers.associateBy { it.uid } }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var selectedProducer by remember { mutableStateOf<ProducerWithLocation?>(null) }
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
                producersWithLocation.forEach { producerWithLocation ->
                    val user = producerUserMap[producerWithLocation.user.uid]
                    Marker(
                        state = MarkerState(position = producerWithLocation.location!!),
                        title = user?.fullName ?: producerWithLocation.user.uid,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                        onClick = {
                            selectedProducer = producerWithLocation.copy(
                                user = user ?: producerWithLocation.user
                            )
                            false
                        }
                    )
                }
            }

            selectedProducer?.let { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(entry.user.fullName, style = MaterialTheme.typography.titleMedium)
                        if (entry.user.phone.isNotBlank()) {
                            Text(
                                entry.user.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = { onNavigateToProducerDetail(entry.user.uid) }
                        ) {
                            Text("Ver perfil")
                        }
                        if (currentUser?.role == UserRole.CONSUMER) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        val chatId = chatViewModel.getOrCreateChat(
                                            producerId = entry.user.uid,
                                            producerName = entry.user.fullName,
                                            consumerId = currentUser!!.uid,
                                            consumerName = currentUser!!.fullName
                                        )
                                        onNavigateToChat(chatId, entry.user.fullName)
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


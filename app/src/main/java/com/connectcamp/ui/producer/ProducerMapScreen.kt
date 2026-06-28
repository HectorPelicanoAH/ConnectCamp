package com.connectcamp.ui.producer

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.connectcamp.viewmodel.ProducerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProducerMapScreen(
    producerViewModel: ProducerViewModel,
    onBack: () -> Unit
) {
    val producerProfile by producerViewModel.producerProfile.collectAsState()
    val operationResult by producerViewModel.operationResult.collectAsState()
    val isLoading by producerViewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val initialLocation = producerProfile?.location?.let {
        LatLng(it.latitude, it.longitude)
    } ?: LatLng(40.4168, -3.7038) // Default: Madrid

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 12f)
    }

    var markerPosition by remember {
        mutableStateOf(
            producerProfile?.location?.let { LatLng(it.latitude, it.longitude) }
        )
    }

    LaunchedEffect(operationResult) {
        operationResult?.let {
            snackbarHostState.showSnackbar(it)
            producerViewModel.clearOperationResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi ubicación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (locationPermission.status.isGranted) {
                        scope.launch {
                            try {
                                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                                val location = fusedClient.lastLocation.await()
                                location?.let {
                                    val latLng = LatLng(it.latitude, it.longitude)
                                    markerPosition = latLng
                                    cameraPositionState.position =
                                        CameraPosition.fromLatLngZoom(latLng, 14f)
                                }
                            } catch (e: SecurityException) {
                                snackbarHostState.showSnackbar("Error al obtener la ubicación")
                            }
                        }
                    } else {
                        locationPermission.launchPermissionRequest()
                    }
                }
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                ),
                onMapClick = { latLng ->
                    markerPosition = latLng
                }
            ) {
                markerPosition?.let { pos ->
                    Marker(
                        state = MarkerState(position = pos),
                        title = "Mi granja"
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                if (markerPosition != null) {
                    Text(
                        text = "Toca el mapa para ajustar tu posición",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 8.dp)
                    )
                    Button(
                        onClick = {
                            markerPosition?.let { pos ->
                                producerViewModel.updateLocation(
                                    pos.latitude,
                                    pos.longitude,
                                    "Mi explotación"
                                )
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar ubicación")
                    }
                } else {
                    Text(
                        text = "Toca el mapa para establecer tu ubicación",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

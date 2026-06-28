package com.connectcamp.ui.producer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.connectcamp.data.model.ProducerProfile
import com.connectcamp.viewmodel.AuthViewModel
import com.connectcamp.viewmodel.ProducerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducerProfileScreen(
    authViewModel: AuthViewModel,
    producerViewModel: ProducerViewModel,
    onBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val producerProfile by producerViewModel.producerProfile.collectAsState()
    val isLoading by producerViewModel.isLoading.collectAsState()
    val operationResult by producerViewModel.operationResult.collectAsState()

    var description by rememberSaveable { mutableStateOf(producerProfile?.description ?: "") }
    var acceptsOnlinePayment by remember { mutableStateOf(producerProfile?.acceptsOnlinePayment ?: false) }
    var acceptsCash by remember { mutableStateOf(producerProfile?.acceptsCash ?: true) }
    var offersDelivery by remember { mutableStateOf(producerProfile?.offersDelivery ?: false) }
    var deliveryRadius by rememberSaveable { mutableStateOf(producerProfile?.deliveryRadius?.toString() ?: "") }
    var hasPickupPoint by remember { mutableStateOf(producerProfile?.hasPickupPoint ?: false) }
    var pickupAddress by rememberSaveable { mutableStateOf(producerProfile?.pickupAddress ?: "") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(producerProfile) {
        producerProfile?.let {
            description = it.description
            acceptsOnlinePayment = it.acceptsOnlinePayment
            acceptsCash = it.acceptsCash
            offersDelivery = it.offersDelivery
            deliveryRadius = it.deliveryRadius.toString()
            hasPickupPoint = it.hasPickupPoint
            pickupAddress = it.pickupAddress
        }
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
                title = { Text("Mi perfil de productor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = currentUser?.fullName ?: "",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = currentUser?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción de tu explotación") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Métodos de pago", style = MaterialTheme.typography.titleSmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pago online")
                Switch(
                    checked = acceptsOnlinePayment,
                    onCheckedChange = { acceptsOnlinePayment = it }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Efectivo")
                Switch(
                    checked = acceptsCash,
                    onCheckedChange = { acceptsCash = it }
                )
            }

            Text("Logística", style = MaterialTheme.typography.titleSmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ofrece envío a domicilio")
                Switch(
                    checked = offersDelivery,
                    onCheckedChange = { offersDelivery = it }
                )
            }

            if (offersDelivery) {
                OutlinedTextField(
                    value = deliveryRadius,
                    onValueChange = { deliveryRadius = it },
                    label = { Text("Radio de entrega (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tiene punto de recogida")
                Switch(
                    checked = hasPickupPoint,
                    onCheckedChange = { hasPickupPoint = it }
                )
            }

            if (hasPickupPoint) {
                OutlinedTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it },
                    label = { Text("Dirección del punto de recogida") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    producerViewModel.updateProfile(
                        ProducerProfile(
                            uid = currentUser?.uid ?: "",
                            description = description.trim(),
                            acceptsOnlinePayment = acceptsOnlinePayment,
                            acceptsCash = acceptsCash,
                            offersDelivery = offersDelivery,
                            deliveryRadius = deliveryRadius.toDoubleOrNull() ?: 0.0,
                            hasPickupPoint = hasPickupPoint,
                            pickupAddress = pickupAddress.trim(),
                            location = producerProfile?.location,
                            locationName = producerProfile?.locationName ?: ""
                        )
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text("Guardar cambios")
                }
            }
        }
    }
}

package com.connectcamp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.connectcamp.data.model.UserRole
import com.connectcamp.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToProducerHome: () -> Unit,
    onNavigateToConsumerHome: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(500)
        if (authViewModel.isLoggedIn()) {
            // Wait up to 3 seconds for the user profile to load from Firestore
            val user = withTimeoutOrNull(3_000L) {
                authViewModel.currentUser.first { it != null }
            }
            if (user?.role == UserRole.PRODUCER) onNavigateToProducerHome()
            else if (user != null) onNavigateToConsumerHome()
            else onNavigateToLogin()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
    }
}

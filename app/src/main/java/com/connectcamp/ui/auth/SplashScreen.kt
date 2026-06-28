package com.connectcamp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.connectcamp.data.model.UserRole
import com.connectcamp.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToProducerHome: () -> Unit,
    onNavigateToConsumerHome: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        delay(500)
        if (authViewModel.isLoggedIn()) {
            // Wait for user profile to load
            var attempts = 0
            while (currentUser == null && attempts < 10) {
                delay(200)
                attempts++
            }
            val user = authViewModel.currentUser.value
            if (user != null) {
                if (user.role == UserRole.PRODUCER) onNavigateToProducerHome()
                else onNavigateToConsumerHome()
            } else {
                onNavigateToLogin()
            }
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

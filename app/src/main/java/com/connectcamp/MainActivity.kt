package com.connectcamp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.connectcamp.ui.navigation.ConnectCampNavGraph
import com.connectcamp.ui.theme.ConnectCampTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConnectCampTheme {
                val navController = rememberNavController()
                ConnectCampNavGraph(navController = navController)
            }
        }
    }
}

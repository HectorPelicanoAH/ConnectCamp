package com.connectcamp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.connectcamp.data.model.UserRole
import com.connectcamp.ui.auth.LoginScreen
import com.connectcamp.ui.auth.RegisterScreen
import com.connectcamp.ui.auth.SplashScreen
import com.connectcamp.ui.chat.ChatListScreen
import com.connectcamp.ui.chat.ChatScreen
import com.connectcamp.ui.consumer.AddShoppingItemScreen
import com.connectcamp.ui.consumer.ConsumerHomeScreen
import com.connectcamp.ui.consumer.ConsumerMapScreen
import com.connectcamp.ui.consumer.ConsumerProfileScreen
import com.connectcamp.ui.consumer.ShoppingListScreen
import com.connectcamp.ui.producer.AddEditProductScreen
import com.connectcamp.ui.producer.ProducerDetailScreen
import com.connectcamp.ui.producer.ProducerHomeScreen
import com.connectcamp.ui.producer.ProducerMapScreen
import com.connectcamp.ui.producer.ProducerProfileScreen
import com.connectcamp.viewmodel.AuthViewModel
import com.connectcamp.viewmodel.ChatViewModel
import com.connectcamp.viewmodel.ConsumerViewModel
import com.connectcamp.viewmodel.ProducerViewModel

@Composable
fun ConnectCampNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val producerViewModel: ProducerViewModel = hiltViewModel()
    val consumerViewModel: ConsumerViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToProducerHome = {
                    navController.navigate(Screen.ProducerHome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToConsumerHome = {
                    navController.navigate(Screen.ConsumerHome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = { role ->
                    val destination = if (role == UserRole.PRODUCER) Screen.ProducerHome.route
                    else Screen.ConsumerHome.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { role ->
                    val destination = if (role == UserRole.PRODUCER) Screen.ProducerHome.route
                    else Screen.ConsumerHome.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Producer screens
        composable(Screen.ProducerHome.route) {
            ProducerHomeScreen(
                authViewModel = authViewModel,
                producerViewModel = producerViewModel,
                onNavigateToMap = { navController.navigate(Screen.ProducerMap.route) },
                onNavigateToProducts = { navController.navigate(Screen.ProductManagement.route) },
                onNavigateToChats = { navController.navigate(Screen.ChatList.route) },
                onNavigateToProfile = { navController.navigate(Screen.ProducerProfile.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ProducerMap.route) {
            ProducerMapScreen(
                producerViewModel = producerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProductManagement.route) {
            AddEditProductScreen(
                producerViewModel = producerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddEditProduct.route,
            arguments = listOf(navArgument("productId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            AddEditProductScreen(
                producerViewModel = producerViewModel,
                productId = productId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProducerProfile.route) {
            ProducerProfileScreen(
                authViewModel = authViewModel,
                producerViewModel = producerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Consumer screens
        composable(Screen.ConsumerHome.route) {
            ConsumerHomeScreen(
                authViewModel = authViewModel,
                consumerViewModel = consumerViewModel,
                onNavigateToMap = { navController.navigate(Screen.ConsumerMap.route) },
                onNavigateToShoppingList = { navController.navigate(Screen.ShoppingList.route) },
                onNavigateToChats = { navController.navigate(Screen.ChatList.route) },
                onNavigateToProfile = { navController.navigate(Screen.ConsumerProfile.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ConsumerMap.route) {
            ConsumerMapScreen(
                consumerViewModel = consumerViewModel,
                authViewModel = authViewModel,
                chatViewModel = chatViewModel,
                onNavigateToChat = { chatId, otherName ->
                    navController.navigate(Screen.Chat.createRoute(chatId, otherName))
                },
                onNavigateToProducerDetail = { producerId ->
                    navController.navigate(Screen.ProducerDetail.createRoute(producerId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ShoppingList.route) {
            ShoppingListScreen(
                consumerViewModel = consumerViewModel,
                onAddItem = { navController.navigate(Screen.AddShoppingItem.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddShoppingItem.route) {
            AddShoppingItemScreen(
                consumerViewModel = consumerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ConsumerProfile.route) {
            ConsumerProfileScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Shared screens
        composable(Screen.ChatList.route) {
            ChatListScreen(
                authViewModel = authViewModel,
                chatViewModel = chatViewModel,
                onNavigateToChat = { chatId, otherName ->
                    navController.navigate(Screen.Chat.createRoute(chatId, otherName))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("otherUserName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            val otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: ""
            ChatScreen(
                chatId = chatId,
                otherUserName = otherUserName,
                authViewModel = authViewModel,
                chatViewModel = chatViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ProducerDetail.route,
            arguments = listOf(navArgument("producerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val producerId = backStackEntry.arguments?.getString("producerId") ?: return@composable
            ProducerDetailScreen(
                producerId = producerId,
                authViewModel = authViewModel,
                producerViewModel = producerViewModel,
                chatViewModel = chatViewModel,
                onNavigateToChat = { chatId, otherName ->
                    navController.navigate(Screen.Chat.createRoute(chatId, otherName))
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

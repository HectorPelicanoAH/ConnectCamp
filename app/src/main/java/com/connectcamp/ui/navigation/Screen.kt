package com.connectcamp.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object RoleSelection : Screen("role_selection")

    // Producer
    object ProducerHome : Screen("producer_home")
    object ProducerMap : Screen("producer_map")
    object ProductManagement : Screen("product_management")
    object AddEditProduct : Screen("add_edit_product?productId={productId}") {
        fun createRoute(productId: String? = null) =
            if (productId != null) "add_edit_product?productId=$productId"
            else "add_edit_product"
    }
    object ProducerProfile : Screen("producer_profile")

    // Consumer
    object ConsumerHome : Screen("consumer_home")
    object ConsumerMap : Screen("consumer_map")
    object ShoppingList : Screen("shopping_list")
    object AddShoppingItem : Screen("add_shopping_item")
    object ConsumerProfile : Screen("consumer_profile")

    // Shared
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat/{chatId}/{otherUserName}") {
        fun createRoute(chatId: String, otherUserName: String) =
            "chat/$chatId/$otherUserName"
    }
    object ProducerDetail : Screen("producer_detail/{producerId}") {
        fun createRoute(producerId: String) = "producer_detail/$producerId"
    }
}

package com.connectcamp.data.model

import com.google.firebase.firestore.DocumentId

data class ShoppingListItem(
    @DocumentId
    val id: String = "",
    val consumerId: String = "",
    val productName: String = "",
    val category: ProductCategory = ProductCategory.FRUITS,
    val season: Season = Season.ALL_YEAR,
    val quantity: Double = 1.0,
    val unit: ProductUnit = ProductUnit.KG,
    val notes: String = "",
    val isAcquired: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

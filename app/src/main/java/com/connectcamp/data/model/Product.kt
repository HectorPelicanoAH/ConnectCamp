package com.connectcamp.data.model

import com.google.firebase.firestore.DocumentId

enum class Season(val displayName: String) {
    SPRING("Primavera"),
    SUMMER("Verano"),
    AUTUMN("Otoño"),
    WINTER("Invierno"),
    ALL_YEAR("Todo el año")
}

enum class ProductCategory(val displayName: String) {
    FRUITS("Frutas"),
    VEGETABLES("Verduras"),
    EGGS("Huevos"),
    LEGUMES("Legumbres"),
    HERBS("Hierbas aromáticas"),
    OTHER("Otros")
}

enum class ProductUnit(val displayName: String) {
    KG("kg"),
    UNIT("unidad"),
    DOZEN("docena"),
    LITER("litro"),
    GRAM("gramo"),
    BOX("caja")
}

data class Product(
    @DocumentId
    val id: String = "",
    val producerId: String = "",
    val name: String = "",
    val category: ProductCategory = ProductCategory.FRUITS,
    val season: Season = Season.ALL_YEAR,
    val price: Double = 0.0,
    val unit: ProductUnit = ProductUnit.KG,
    val availableQuantity: Double = 0.0,
    val description: String = "",
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

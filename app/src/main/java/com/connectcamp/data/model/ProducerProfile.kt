package com.connectcamp.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class ProducerProfile(
    @DocumentId
    val uid: String = "",
    val location: GeoPoint? = null,
    val locationName: String = "",
    val description: String = "",
    val acceptsOnlinePayment: Boolean = false,
    val acceptsCash: Boolean = true,
    val offersDelivery: Boolean = false,
    val deliveryRadius: Double = 0.0,
    val hasPickupPoint: Boolean = false,
    val pickupAddress: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

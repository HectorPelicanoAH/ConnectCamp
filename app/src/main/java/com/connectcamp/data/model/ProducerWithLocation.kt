package com.connectcamp.data.model

import com.google.android.gms.maps.model.LatLng

data class ProducerWithLocation(
    val user: User,
    val location: LatLng?
)

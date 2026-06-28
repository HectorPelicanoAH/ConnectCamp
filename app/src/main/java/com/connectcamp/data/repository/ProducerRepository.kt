package com.connectcamp.data.repository

import com.connectcamp.data.model.Product
import com.connectcamp.data.model.ProducerProfile
import com.connectcamp.data.model.ProducerWithLocation
import com.connectcamp.data.model.User
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProducerRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val producersCollection = firestore.collection("producers")
    private val productsCollection = firestore.collection("products")
    private val usersCollection = firestore.collection("users")

    suspend fun getOrCreateProducerProfile(): ProducerProfile? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = producersCollection.document(uid).get().await()
            if (doc.exists()) {
                doc.toObject(ProducerProfile::class.java)
            } else {
                val profile = ProducerProfile(uid = uid)
                producersCollection.document(uid).set(profile).await()
                profile
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getProducerProfileFlow(uid: String): Flow<ProducerProfile?> = callbackFlow {
        val listener = producersCollection.document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(ProducerProfile::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateProducerProfile(profile: ProducerProfile): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            producersCollection.document(uid)
                .set(profile.copy(uid = uid, updatedAt = System.currentTimeMillis()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLocation(latitude: Double, longitude: Double, locationName: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            producersCollection.document(uid)
                .update(
                    mapOf(
                        "location" to GeoPoint(latitude, longitude),
                        "locationName" to locationName,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addProduct(product: Product): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val docRef = productsCollection.add(product.copy(producerId = uid)).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            productsCollection.document(product.id)
                .set(product.copy(updatedAt = System.currentTimeMillis()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMyProductsFlow(): Flow<List<Product>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = productsCollection
            .whereEqualTo("producerId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { it.toObject(Product::class.java) }
                trySend(products ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    fun getAllProducersFlow(): Flow<List<User>> = callbackFlow {
        val listener = usersCollection
            .whereEqualTo("role", "PRODUCER")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val producers = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) }
                trySend(producers ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun getProducerProducts(producerId: String): List<Product> {
        return try {
            productsCollection
                .whereEqualTo("producerId", producerId)
                .whereEqualTo("isAvailable", true)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Product::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchProducersByProduct(productName: String): List<User> {
        return try {
            val matchingProductDocs = productsCollection
                .whereEqualTo("isAvailable", true)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Product::class.java) }
                .filter { it.name.contains(productName, ignoreCase = true) }

            val producerIds = matchingProductDocs.map { it.producerId }.distinct()

            producerIds.mapNotNull { uid ->
                try {
                    usersCollection.document(uid).get().await().toObject(User::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAllProducersWithLocationFlow(): Flow<List<ProducerWithLocation>> = callbackFlow {
        val listener = producersCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val profiles = snapshot?.documents?.mapNotNull { it.toObject(ProducerProfile::class.java) }
                    ?: emptyList()
                // Only include producers who have registered a location
                val result = profiles.mapNotNull { profile ->
                    val geoPoint = profile.location ?: return@mapNotNull null
                    val location = LatLng(geoPoint.latitude, geoPoint.longitude)
                    ProducerWithLocation(
                        user = User(uid = profile.uid),
                        location = location
                    )
                }
                trySend(result)
            }
        awaitClose { listener.remove() }
    }
}

package com.connectcamp.data.repository

import com.connectcamp.data.model.ShoppingListItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsumerRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val shoppingListCollection = firestore.collection("shoppingList")

    fun getShoppingListFlow(): Flow<List<ShoppingListItem>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = shoppingListCollection
            .whereEqualTo("consumerId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { it.toObject(ShoppingListItem::class.java) }
                trySend(items ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun addShoppingListItem(item: ShoppingListItem): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val docRef = shoppingListCollection.add(item.copy(consumerId = uid)).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateShoppingListItem(item: ShoppingListItem): Result<Unit> {
        return try {
            shoppingListCollection.document(item.id).set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleItemAcquired(itemId: String, isAcquired: Boolean): Result<Unit> {
        return try {
            shoppingListCollection.document(itemId)
                .update("isAcquired", isAcquired)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteShoppingListItem(itemId: String): Result<Unit> {
        return try {
            shoppingListCollection.document(itemId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

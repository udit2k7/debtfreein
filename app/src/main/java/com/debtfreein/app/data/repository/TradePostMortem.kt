package com.debtfreein.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

data class TradePostMortem(
    val tradeId: String = "",
    val isPaperTrade: Boolean = false,
    val rationale: String = "",
    val netProfit: Double = 0.0,
    val mistakesMade: String = "",
    val lessonsLearned: String = ""
)

class TradePostMortemRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection = firestore.collection("trade_post_mortems")

    suspend fun savePostMortem(postMortem: TradePostMortem): Unit = suspendCoroutine { continuation ->
        collection.document(postMortem.tradeId)
            .set(postMortem)
            .addOnSuccessListener {
                continuation.resume(Unit)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }

    suspend fun getPostMortem(tradeId: String): TradePostMortem? = suspendCoroutine { continuation ->
        collection.document(tradeId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    continuation.resume(document.toObject(TradePostMortem::class.java))
                } else {
                    continuation.resume(null)
                }
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }

    suspend fun getRecentLosses(limit: Long = 5): List<TradePostMortem> = suspendCoroutine { continuation ->
        collection.whereLessThan("netProfit", 0.0)
            .limit(limit)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.mapNotNull { it.toObject(TradePostMortem::class.java) }
                continuation.resume(list)
            }
            .addOnFailureListener { exception ->
                continuation.resume(emptyList()) // Fail-safe fallback to empty list
            }
    }

    fun getAllPostMortemsFlow(): kotlinx.coroutines.flow.Flow<List<TradePostMortem>> = kotlinx.coroutines.flow.callbackFlow {
        val listenerRegistration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { it.toObject(TradePostMortem::class.java) }
                trySend(list)
            }
        }
        awaitClose { listenerRegistration.remove() }
    }
}


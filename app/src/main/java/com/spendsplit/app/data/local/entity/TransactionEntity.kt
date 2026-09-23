package com.spendsplit.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["description"]),
        Index(value = ["personId"]),
        Index(value = ["categoryId"]),
        Index(value = ["date"]),
        Index(value = ["splitGroupId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val date: Long, // Epoch timestamp in millis
    val time: String, // "HH:mm" formatted
    val categoryId: Long,
    val categoryName: String,
    val description: String,
    val type: String, // "personal", "owed_to_me", "i_owe", "split", "settlement"
    val totalAmount: Double? = null,
    val myShare: Double? = null,
    val theirShare: Double? = null,
    val personId: Long? = null,
    val personName: String? = null,
    val splitGroupId: String? = null,
    val splitDetails: String? = null,
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

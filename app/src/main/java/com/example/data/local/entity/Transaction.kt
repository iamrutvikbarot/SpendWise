package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String,
    val title: String,
    val note: String? = null,
    val paymentMethod: String, // "CASH", "UPI", "CARD", "NET_BANKING"
    val platform: String? = null, // "Google Pay", "PhonePe", etc.
    val upiRefId: String? = null,
    val merchantName: String? = null,
    val isRecurring: Boolean = false,
    val recurringFrequency: String? = null,
    val date: Long, // timestamp
    val createdAt: Long = System.currentTimeMillis(),
    val screenshotPath: String? = null
)

package com.warh.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["type", "categoryId", "date"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val type: String,
    val amountMinor: Long,
    val currency: String,
    val date: LocalDateTime,
    val categoryId: Long?,
    val merchant: String?,
    val note: String?,
)
package com.warh.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["type", "categoryId", "date"]),
        Index(value = ["accountId", "date"]),
        Index(value = ["date"]),
        Index(value = ["merchant"]),
        Index(value = ["accountId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val type: String,
    val amountMinor: Long,
    val date: LocalDateTime,
    val yearMonth: String,
    val categoryId: Long?,
    val merchant: String?,
    val note: String?,
)
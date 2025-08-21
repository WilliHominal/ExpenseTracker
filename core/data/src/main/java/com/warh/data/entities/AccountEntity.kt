package com.warh.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val type: String,
    val currency: String,
    val balanceMinor: Long,
    val iconIndex: Int = 1,
    val iconColorArgb: Long? = null
)
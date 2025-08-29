package com.warh.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val currency: String,
    val balance: Long,
    val initialBalance: Long,
    val iconIndex: Int = 1,
    val iconColorArgb: Long? = null
)
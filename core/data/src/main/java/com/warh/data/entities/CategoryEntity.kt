package com.warh.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val iconIndex: Int = 1,
    val iconColorArgb: Long? = null,
    val type: String = "EXPENSE"
)
package com.warh.data.entities

import androidx.room.Entity

@Entity(
    tableName = "budgets",
    primaryKeys = ["categoryId", "year", "month"]
)
data class BudgetEntity(
    val categoryId: Long,
    val year: Int,
    val month: Int,
    val limitMinor: Long,
)
package com.warh.domain.models

data class BudgetProgress(
    val category: Category,
    val year: Int,
    val month: Int,
    val limitMinor: Long,
    val spentMinor: Long,
) {
    val remainingMinor: Long get() = (limitMinor - spentMinor).coerceAtLeast(0)
    val ratio: Float get() = if (limitMinor <= 0) 0f else (spentMinor.toFloat() / limitMinor.toFloat()).coerceIn(0f, 1.5f)
}
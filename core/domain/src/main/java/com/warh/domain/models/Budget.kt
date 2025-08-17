package com.warh.domain.models

data class Budget(
    val categoryId: Long,
    val year: Int,
    val month: Int,
    val limitMinor: Long,
)
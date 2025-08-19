package com.warh.domain.dto

data class MonthlySumDTO(
    val yearMonth: String,
    val incomeMinor: Long,
    val expenseMinor: Long
)
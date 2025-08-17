package com.warh.domain.models

import java.time.LocalDateTime

data class TransactionFilter(
    val accountIds: Set<Long> = emptySet(),
    val categoryIds: Set<Long> = emptySet(),
    val from: LocalDateTime? = null,
    val to: LocalDateTime? = null,
    val text: String? = null,
)
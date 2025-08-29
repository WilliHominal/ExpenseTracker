package com.warh.domain.models

import java.time.LocalDateTime

data class Transaction(
    val id: Long,
    val accountId: Long,
    val type: TxType,
    val amountMinor: Long,
    val date: LocalDateTime,
    val categoryId: Long?,
    val merchant: String?,
    val note: String?,
)
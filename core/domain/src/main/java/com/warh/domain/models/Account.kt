package com.warh.domain.models

import java.util.Currency
import java.util.Locale

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType = AccountType.CASH,
    val currency: String = Currency.getInstance(Locale.getDefault()).currencyCode,
    val balanceMinor: Long = 0,
    val iconIndex: Int = 1,
    val iconColorArgb: Long? = null
)
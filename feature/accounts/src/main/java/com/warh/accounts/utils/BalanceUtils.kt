package com.warh.accounts.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

object BalanceUtils {
    fun parseMinor(text: String, currency: String): Long {
        val digits = runCatching { Currency.getInstance(currency).defaultFractionDigits }
            .getOrDefault(2).coerceAtLeast(0)
        val bd = text.replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO
        return bd.movePointRight(digits).setScale(0, RoundingMode.HALF_UP).longValueExact()
    }

    fun formatMajor(minor: Long, currency: String): String {
        val digits = runCatching { Currency.getInstance(currency).defaultFractionDigits }
            .getOrDefault(2).coerceAtLeast(0)
        return BigDecimal(minor).movePointLeft(digits).stripTrailingZeros().toPlainString()
    }
}
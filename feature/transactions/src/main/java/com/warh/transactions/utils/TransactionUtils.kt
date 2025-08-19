package com.warh.transactions.utils

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale
import kotlin.math.max

fun formatAmountMajor(minor: Long, currencyCode: String): String {
    val currency = runCatching { Currency.getInstance(currencyCode) }.getOrElse {
        Currency.getInstance(Locale.getDefault())
    }
    val digits = max(0, currency.defaultFractionDigits)
    val major = BigDecimal.valueOf(minor).movePointLeft(digits)

    return NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        this.currency = currency
        this.maximumFractionDigits = digits
        this.minimumFractionDigits = digits
    }.format(major)
}

fun formatDateTime(dt: LocalDateTime): String {
    return DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.SHORT)
        .withLocale(Locale.getDefault())
        .format(dt)
}
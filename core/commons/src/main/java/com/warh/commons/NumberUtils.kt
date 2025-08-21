package com.warh.commons

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

enum class CurrencyLabel { NONE, SYMBOL, CODE }

object NumberUtils {
    private fun formatAmountMajor(
        amountMinor: Long,
        currencyCode: String,
        label: CurrencyLabel = CurrencyLabel.SYMBOL,
        trimZeroDecimals: Boolean = true,
        grouping: Boolean = true,
    ): String {
        val currency = runCatching { Currency.getInstance(currencyCode) }
            .getOrElse { Currency.getInstance(Locale.getDefault()) }

        val digits = currency.defaultFractionDigits.coerceAtLeast(0)
        val major = BigDecimal(amountMinor).movePointLeft(digits)

        val fractionIsZero = (digits == 0) || (amountMinor % pow10(digits) == 0L)

        val nf = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            isGroupingUsed = grouping
            if (trimZeroDecimals && fractionIsZero) {
                minimumFractionDigits = 0
                maximumFractionDigits = 0
            } else {
                minimumFractionDigits = digits
                maximumFractionDigits = digits
            }
        }

        val absText = nf.format(major.abs())
        val prefix = when (label) {
            CurrencyLabel.SYMBOL -> currency.getSymbol(Locale.getDefault())
            CurrencyLabel.CODE   -> currency.currencyCode
            CurrencyLabel.NONE   -> ""
        }
        val sep = if (label == CurrencyLabel.NONE) "" else " "

        return if (major.signum() < 0)
            "-$prefix$sep$absText"
        else
            "$prefix$sep$absText"
    }

    private fun pow10(n: Int): Long {
        var r = 1L
        repeat(n) { r *= 10L }
        return r
    }

    fun formatAmountWithSymbol(minor: Long, code: String, trimZeroDecimals: Boolean = true) =
        formatAmountMajor(minor, code, label = CurrencyLabel.SYMBOL, trimZeroDecimals = trimZeroDecimals)

    fun formatAmountWithCode(minor: Long, code: String, trimZeroDecimals: Boolean = true) =
        formatAmountMajor(minor, code, label = CurrencyLabel.CODE, trimZeroDecimals = trimZeroDecimals)

    fun formatAmountPlain(minor: Long, code: String, trimZeroDecimals: Boolean = true) =
        formatAmountMajor(minor, code, label = CurrencyLabel.NONE, trimZeroDecimals = trimZeroDecimals)
}
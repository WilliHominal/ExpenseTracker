package com.warh.commons

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow

enum class CurrencyLabel { NONE, SYMBOL, CODE }
enum class HeroRounding { HALF_EVEN, THRESHOLD }

object NumberUtils {
    private fun roundByThreshold(value: Double, decimals: Int, threshold: Double = 0.75): Double {
        require(threshold in 0.0..1.0)
        if (decimals <= 0) {
            val base = floor(value)
            val frac = value - base
            return if (frac >= threshold) base + 1 else base
        }
        val p = BigDecimal.TEN.pow(decimals)
        val scaled = BigDecimal.valueOf(value).multiply(p)
        val intPart = scaled.setScale(0, RoundingMode.FLOOR)
        val frac = scaled.subtract(intPart)
        val res = if (frac >= BigDecimal.valueOf(threshold)) intPart.add(BigDecimal.ONE) else intPart
        return res.divide(p).toDouble()
    }

    private fun roundHalfEven(value: Double, decimals: Int): Double =
        BigDecimal.valueOf(value).setScale(decimals.coerceAtLeast(0), RoundingMode.HALF_EVEN).toDouble()

    fun formatHeroAmount(
        minor: Long,
        currency: String,
        locale: Locale = Locale.getDefault(),
        rounding: HeroRounding = HeroRounding.THRESHOLD,
        threshold: Double = 0.75
    ): String {
        val digits = runCatching { Currency.getInstance(currency).defaultFractionDigits }
            .getOrDefault(2).coerceAtLeast(0)

        val negative = minor < 0
        val sign = if (negative) "-" else ""
        val factor = 10.0.pow(digits)
        val major = abs(minor) / factor

        fun formatPlain(v: Double): String = NumberFormat.getNumberInstance(locale).apply {
            isGroupingUsed = true
            minimumFractionDigits = if (digits == 0) 0 else 2
            maximumFractionDigits = if (digits == 0) 0 else 2
        }.format(v)

        var rawScaled: Double
        var suffix: String
        when {
            major >= 1_000_000 -> { rawScaled = major / 1_000_000; suffix = "M" }
            major >= 10_000    -> { rawScaled = major / 1_000;     suffix = "k" }
            else               -> return sign + formatPlain(major)
        }

        fun decimalsFor(v: Double): Int = when {
            v < 100   -> 2
            v < 1000  -> 1
            else      -> 0
        }

        fun applyRounding(v: Double, dec: Int): Double = when (rounding) {
            HeroRounding.THRESHOLD -> roundByThreshold(v, dec, threshold)
            HeroRounding.HALF_EVEN -> roundHalfEven(v, dec)
        }

        var decimals = decimalsFor(rawScaled)
        var scaled = applyRounding(rawScaled, decimals)

        if (suffix == "k" && scaled >= 1000) {
            rawScaled = major / 1_000_000
            suffix = "M"
            decimals = decimalsFor(rawScaled)
            scaled = applyRounding(rawScaled, decimals)
        }

        val nf = NumberFormat.getNumberInstance(locale).apply {
            isGroupingUsed = (decimals == 0)
            minimumFractionDigits = decimals
            maximumFractionDigits = decimals
        }

        return sign + nf.format(scaled) + suffix
    }

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
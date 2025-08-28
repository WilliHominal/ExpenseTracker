package com.warh.accounts.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.accounts.R
import com.warh.accounts.utils.BalanceUtils.parseMinor
import com.warh.commons.Strings
import com.warh.commons.get
import com.warh.domain.models.Account
import com.warh.domain.models.AccountType
import com.warh.domain.use_cases.GetAccountUseCase
import com.warh.domain.use_cases.UpsertAccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Currency

data class AccountAddUiState(
    val name: String = "",
    val type: AccountType = AccountType.CASH,
    val currency: String = defaultCurrency(),
    val balanceText: String = "",
    val iconIndex: Int = 1,
    val iconColorArgb: Long? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val editingId: Long? = null,
    val originalInitialBalance: Long? = null,
    val originalBalance: Long? = null
)

private fun defaultCurrency(): String =
    runCatching { Currency.getInstance(java.util.Locale.getDefault()).currencyCode }
        .getOrDefault("ARS")

private const val ACCOUNT_NAME_MAX_CHARS = 25

class AccountAddViewModel(
    private val editingId: Long?,
    private val getAccount: GetAccountUseCase,
    private val upsert: UpsertAccountUseCase,
    private val strings: Strings
) : ViewModel() {

    private val _ui = MutableStateFlow(AccountAddUiState())
    val ui: StateFlow<AccountAddUiState> = _ui.asStateFlow()

    init {
        if (editingId != null) {
            viewModelScope.launch {
                getAccount(editingId)?.let { acc ->
                    val digits = digitsFor(acc.currency)
                    _ui.update {
                        it.copy(
                            isEditing = true,
                            editingId = acc.id,
                            name = acc.name,
                            type = acc.type,
                            currency = acc.currency,
                            balanceText = formatForInput(acc.initialBalance, digits),
                            iconIndex = acc.iconIndex,
                            iconColorArgb = acc.iconColorArgb,
                            originalInitialBalance = acc.initialBalance,
                            originalBalance = acc.balance
                        )
                    }
                }
            }
        }
    }

    fun onName(v: String) = _ui.update { it.copy(name = sanitizeAccountName(v)) }
    fun onType(v: AccountType) = _ui.update { it.copy(type = v) }

    fun onCurrency(v: String) {
        val digits = digitsFor(v)
        val sanitized = sanitizeBalanceInput(_ui.value.balanceText, digits)
        _ui.update { it.copy(currency = v, balanceText = sanitized) }
    }

    fun onBalanceText(v: String) {
        val digits = digitsFor(_ui.value.currency)
        _ui.update { it.copy(balanceText = sanitizeBalanceInput(v, digits)) }
    }

    fun onIconIndex(v: Int) = _ui.update { it.copy(iconIndex = v) }
    fun onIconColor(v: Long?) = _ui.update { it.copy(iconColorArgb = v) }

    fun save(onError: (String) -> Unit, onDone: () -> Unit) {
        val d = _ui.value
        if (d.name.isBlank()) {
            onError(strings[R.string.account_add_error_name_required])
            _ui.update { it.copy(error = strings[R.string.account_add_error_name_required]) }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(isSaving = true, error = null) }
            val result = runCatching {
                if (d.isEditing) {
                    val newInitial   = parseMinor(d.balanceText, d.currency)
                    val origInitial  = d.originalInitialBalance ?: 0L
                    val origBalance  = d.originalBalance ?: 0L
                    val delta        = newInitial - origInitial
                    val newBalance   = origBalance + delta

                    upsert(
                        Account(
                            id = d.editingId,
                            name = d.name.trim(),
                            type = d.type,
                            currency = d.currency,
                            initialBalance = newInitial,
                            balance = newBalance,
                            iconIndex = d.iconIndex,
                            iconColorArgb = d.iconColorArgb
                        )
                    )
                } else {
                    val initial = parseMinor(d.balanceText, d.currency)
                    upsert(
                        Account(
                            name = d.name.trim(),
                            type = d.type,
                            currency = d.currency,
                            initialBalance = initial,
                            balance = initial,
                            iconIndex = d.iconIndex,
                            iconColorArgb = d.iconColorArgb
                        )
                    )
                }
            }
            _ui.update { it.copy(isSaving = false) }
            result.onSuccess { onDone() }
                .onFailure { e ->
                    val msg = e.message ?: strings[R.string.account_add_error_default]
                    _ui.update { it.copy(error = msg) }
                    onError(msg)
                }
        }
    }


    private fun sanitizeAccountName(input: String): String =
        input.take(ACCOUNT_NAME_MAX_CHARS)

    private fun digitsFor(code: String): Int =
        runCatching { Currency.getInstance(code).defaultFractionDigits }.getOrDefault(2).coerceAtLeast(0)

    private fun pow10(n: Int): Long {
        var r = 1L
        repeat(n) { r *= 10L }
        return r
    }

    private fun formatForInput(minor: Long, digits: Int): String {
        if (digits <= 0) return minor.toString()
        val abs = kotlin.math.abs(minor)
        val sign = if (minor < 0) "-" else ""
        val intPart = abs / pow10(digits)
        val fracPart = (abs % pow10(digits)).toString().padStart(digits, '0')
        return "$sign$intPart.$fracPart"
    }

    private fun sanitizeBalanceInput(raw: String, digits: Int): String {
        val prev = _ui.value.balanceText
        if (raw.trim() == "-") return "-"

        fun cleanToParts(s: String): Triple<String,String,String>? {
            var hasSep = false
            var hasSign = false
            val sb = StringBuilder()
            for (ch in s) {
                when {
                    ch == '-' && !hasSign && sb.isEmpty() -> { sb.append(ch); hasSign = true }
                    (ch == '.' || ch == ',') && !hasSep && digits > 0 -> { sb.append('.'); hasSep = true }
                    ch.isDigit() -> sb.append(ch)
                }
            }
            val t = sb.toString()
            if (t.isEmpty() || t == "-") return null
            val sign = if (t[0] == '-') "-" else ""
            var unsigned = if (sign.isEmpty()) t else t.substring(1)
            if (digits == 0 && '.' in unsigned) unsigned = unsigned.substringBefore('.')
            val parts = unsigned.split('.', limit = 2)
            var intPart = parts.getOrNull(0)?.filter { it.isDigit() }.orEmpty()
            var fracPart = parts.getOrNull(1)?.filter { it.isDigit() }.orEmpty()
            intPart = intPart.trimStart('0').ifEmpty { "0" }
            fracPart = if (digits == 0) "" else fracPart.take(digits)
            return Triple(sign, intPart, fracPart)
        }

        fun join(sign: String, intPart: String, fracPart: String): String {
            val zero = intPart == "0" && (digits == 0 || fracPart.all { it == '0' })
            val body = if (digits == 0 || fracPart.isEmpty()) intPart else "$intPart.$fracPart"
            return if (sign == "-" && zero) "-" else sign + body
        }

        fun exceeds(ip: String, fp: String, maxI: String, maxF: String): Boolean {
            if (ip.length > maxI.length) return true
            if (ip.length < maxI.length) return false
            if (ip > maxI) return true
            if (digits > 0 && ip == maxI && fp > maxF) return true
            return false
        }

        fun cropInt(ip: String, maxI: String): String {
            var res = ip
            if (res.length > maxI.length) res = res.substring(0, maxI.length)
            if (res.length < maxI.length) return res
            for (i in res.indices) {
                val a = res[i]; val b = maxI[i]
                if (a < b) return res
                if (a > b) return res.substring(0, i)
            }
            return res
        }

        fun cropFrac(fp: String, maxF: String): String {
            val res = fp.take(maxF.length)
            for (i in maxF.indices) {
                val a = if (i < res.length) res[i] else '0'
                val b = maxF[i]
                if (a < b) return res
                if (a > b) return res.substring(0, i)
            }
            return res
        }

        val cand = cleanToParts(raw) ?: return raw.filter { it == '-' || it.isDigit() || it == '.' || it == ',' }
        val (signC, intC, fracC) = cand

        val p = pow10(digits).coerceAtLeast(1L)
        val maxI = (Long.MAX_VALUE / p).toString()
        val maxF = (Long.MAX_VALUE % p).toString().padStart(digits, '0')

        if (!exceeds(intC, fracC, maxI, maxF)) return join(signC, intC, fracC)

        val croppedInt = cropInt(intC, maxI).ifEmpty { "0" }
        val croppedFrac = if (digits > 0 && croppedInt == maxI) cropFrac(fracC, maxF) else fracC.take(digits)
        val cropped = join(signC, croppedInt, croppedFrac)

        val prevParts = cleanToParts(prev)
        val prevText = if (prevParts == null) prev else join(prevParts.first, prevParts.second, prevParts.third)

        return if (prevText.isNotBlank() && prevText.length >= cropped.length) prevText else cropped
    }
}

private inline fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
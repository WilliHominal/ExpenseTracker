package com.warh.accounts.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.accounts.R
import com.warh.accounts.utils.BalanceUtils.parseMinor
import com.warh.commons.Strings
import com.warh.commons.get
import com.warh.domain.models.Account
import com.warh.domain.models.AccountType
import com.warh.domain.use_cases.UpsertAccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountAddUiState(
    val name: String = "",
    val type: AccountType = AccountType.CASH,
    val currency: String = defaultCurrency(),
    val balanceText: String = "",
    val iconIndex: Int = 1,
    val iconColorArgb: Long? = null,
    val isSaving: Boolean = false,
    val error: String? = null
)

private fun defaultCurrency(): String =
    runCatching { java.util.Currency.getInstance(java.util.Locale.getDefault()).currencyCode }
        .getOrDefault("ARS")

class AccountAddViewModel(
    private val upsert: UpsertAccountUseCase,
    private val strings: Strings
) : ViewModel() {

    private val _ui = MutableStateFlow(AccountAddUiState())
    val ui: StateFlow<AccountAddUiState> = _ui.asStateFlow()

    fun onName(v: String) = _ui.update { it.copy(name = v) }
    fun onType(v: AccountType) = _ui.update { it.copy(type = v) }
    fun onCurrency(v: String) = _ui.update { it.copy(currency = v) }
    fun onBalanceText(v: String) = _ui.update { it.copy(balanceText = normalizeAmountInput(v)) }
    fun onIconIndex(v: Int) = _ui.update { it.copy(iconIndex = v) }
    fun onIconColor(v: Long?) = _ui.update { it.copy(iconColorArgb = v) }

    fun save(onError: (String) -> Unit, onDone: () -> Unit) {
        val d = _ui.value
        val name = d.name.trim()
        if (name.isBlank()) {
            onError(strings[R.string.account_add_error_name_required])
            _ui.update { it.copy(error = strings[R.string.account_add_error_name_required]) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(isSaving = true, error = null) }
            val result = runCatching {
                val initial = parseMinor(d.balanceText, d.currency)
                val account = Account(
                    name = name,
                    type = d.type,
                    currency = d.currency,
                    initialBalance = initial,
                    balance = initial,
                    iconIndex = d.iconIndex,
                    iconColorArgb = d.iconColorArgb
                )
                upsert(account)
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

    private fun normalizeAmountInput(raw: String): String {
        val filtered = raw.filter { it.isDigit() || it == '.' || it == ',' }
        var s = filtered.replace(',', '.')
        val i = s.indexOf('.')
        if (i >= 0) {
            val intPart = s.substring(0, i).ifEmpty { "0" }
            val fracPart = s.substring(i + 1).take(2)
            s = if (fracPart.isEmpty()) "$intPart." else "$intPart.$fracPart"
        }
        return s
    }
}

private inline fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
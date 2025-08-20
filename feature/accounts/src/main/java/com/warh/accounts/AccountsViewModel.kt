package com.warh.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.commons.Strings
import com.warh.commons.get
import com.warh.domain.models.Account
import com.warh.domain.models.AccountType
import com.warh.domain.use_cases.CanDeleteAccountUseCase
import com.warh.domain.use_cases.DeleteAccountUseCase
import com.warh.domain.use_cases.GetAccountsUseCase
import com.warh.domain.use_cases.UpsertAccountUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency
import java.util.Locale

data class CurrencyTotalUi(
    val currency: String,
    val totalMinor: Long
)

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val draft: AccountDraft? = null,
    val totalsByCurrency: List<CurrencyTotalUi> = emptyList()
)

data class AccountDraft(
    val id: Long? = null,
    val name: String = "",
    val type: AccountType = AccountType.CASH,
    val currency: String = defaultCurrency(),
    val balanceText: String = ""
)

private fun defaultCurrency(): String =
    runCatching { Currency.getInstance(Locale.getDefault()).currencyCode }
        .getOrDefault("ARS")

class AccountsViewModel(
    private val getAccounts: GetAccountsUseCase,
    private val upsert: UpsertAccountUseCase,
    private val canDelete: CanDeleteAccountUseCase,
    private val delete: DeleteAccountUseCase,
    private val strings: Strings
) : ViewModel() {

    private val _ui = MutableStateFlow(AccountsUiState())
    val ui: StateFlow<AccountsUiState> = _ui

    init { refresh() }

    private fun refresh() = viewModelScope.launch {
        val accounts = getAccounts()
        val totals = accounts
            .groupBy { it.currency }
            .map { (code, list) ->
                CurrencyTotalUi(
                    currency = code,
                    totalMinor = list.sumOf { it.balanceMinor }
                )
            }
            .sortedBy { it.currency }

        _ui.update { it.copy(accounts = accounts, totalsByCurrency = totals) }
    }

    fun startAdd() = _ui.update { it.copy(draft = AccountDraft()) }

    fun startEdit(acc: Account) = _ui.update {
        it.copy(
            draft = AccountDraft(
                id = acc.id,
                name = acc.name,
                type = acc.type,
                currency = acc.currency,
                balanceText = formatMajor(acc.balanceMinor, acc.currency)
            )
        )
    }

    fun cancelEdit() = _ui.update { it.copy(draft = null) }

    fun onName(v: String)     = _ui.update { it.copy(draft = it.draft?.copy(name = v)) }
    fun onType(v: AccountType)= _ui.update { it.copy(draft = it.draft?.copy(type = v)) }
    fun onCurrency(v: String) = _ui.update {
        val d = it.draft ?: return@update it
        it.copy(draft = d.copy(currency = v))
    }
    fun onBalanceText(v: String) =
        _ui.update { it.copy(draft = it.draft?.copy(balanceText = normalizeAmountInput(v))) }

    fun saveEdit(onError: (String) -> Unit) {
        val d = _ui.value.draft ?: return
        val name = d.name.trim()
        if (name.isBlank()) {
            onError(strings[R.string.accounts_error_name_required])
            return
        }

        viewModelScope.launch {
            val account = Account(
                id = d.id ?: 0L,
                name = name,
                type = d.type,
                currency = d.currency,
                balanceMinor = parseMinor(d.balanceText, d.currency)
            )
            io { upsert(account) }
            _ui.update { it.copy(draft = null) }
            refresh()
        }
    }

    fun delete(acc: Account, onBlocked: (String) -> Unit) {
        viewModelScope.launch {
            val allowed = io { canDelete(acc.id) }
            if (!allowed) {
                onBlocked(strings[R.string.accounts_error_delete_blocked])
                return@launch
            }
            io { delete(acc.id) }
            refresh()
        }
    }

    // --- helpers
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

    private fun parseMinor(text: String, currency: String): Long {
        val digits = runCatching { Currency.getInstance(currency).defaultFractionDigits }
            .getOrDefault(2).coerceAtLeast(0)
        val bd = text.replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO
        return bd.movePointRight(digits).setScale(0, RoundingMode.HALF_UP).longValueExact()
    }

    private fun formatMajor(minor: Long, currency: String): String {
        val digits = runCatching { Currency.getInstance(currency).defaultFractionDigits }
            .getOrDefault(2).coerceAtLeast(0)
        return BigDecimal(minor).movePointLeft(digits).stripTrailingZeros().toPlainString()
    }

    private suspend fun <T> io(block: suspend () -> T): T =
        withContext(Dispatchers.IO) { block() }
}
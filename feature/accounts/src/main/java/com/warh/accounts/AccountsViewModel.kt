package com.warh.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.accounts.utils.BalanceUtils.formatMajor
import com.warh.accounts.utils.BalanceUtils.parseMinor
import com.warh.commons.Strings
import com.warh.commons.get
import com.warh.domain.models.Account
import com.warh.domain.models.AccountType
import com.warh.domain.use_cases.CanDeleteAccountUseCase
import com.warh.domain.use_cases.DeleteAccountUseCase
import com.warh.domain.use_cases.ObserveAccountsUseCase
import com.warh.domain.use_cases.UpsertAccountUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val balanceText: String = "",
    val iconIndex: Int = 1,
    val iconColorArgb: Long? = null
)

private fun defaultCurrency(): String =
    runCatching { Currency.getInstance(Locale.getDefault()).currencyCode }
        .getOrDefault("ARS")

class AccountsViewModel(
    observeAccounts: ObserveAccountsUseCase,
    private val upsert: UpsertAccountUseCase,
    private val canDelete: CanDeleteAccountUseCase,
    private val delete: DeleteAccountUseCase,
    private val strings: Strings
) : ViewModel() {

    private val draft = MutableStateFlow<AccountDraft?>(null)

    val ui: StateFlow<AccountsUiState> =
        combine(observeAccounts(), draft) { accounts, d ->
            val totals = accounts
                .groupBy { it.currency }
                .map { (code, list) -> CurrencyTotalUi(code, list.sumOf { it.balance }) }
                .sortedBy { it.currency }

            AccountsUiState(
                accounts = accounts,
                draft = d,
                totalsByCurrency = totals
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountsUiState()
        )

    fun startAdd() = draft.update { AccountDraft() }

    fun cancelEdit() = draft.update { null }

    fun onIconIndex(v: Int)    = draft.update { it?.copy(iconIndex = v) }
    fun onIconColor(v: Long?)  = draft.update { it?.copy(iconColorArgb = v) }
    fun onName(v: String)      = draft.update { it?.copy(name = v) }
    fun onType(v: AccountType) = draft.update { it?.copy(type = v) }
    fun onCurrency(v: String)  = draft.update { it?.copy(currency = v) }
    fun onBalanceText(v: String) =
        draft.update { it?.copy(balanceText = normalizeAmountInput(v)) }

    fun saveEdit(onError: (String) -> Unit) {
        val d = draft.value ?: return
        val name = d.name.trim()
        if (name.isBlank()) {
            onError(strings[R.string.accounts_error_name_required])
            return
        }

        viewModelScope.launch {
            val initial = parseMinor(d.balanceText, d.currency)
            val account = Account(
                id = d.id,
                name = name,
                type = d.type,
                currency = d.currency,
                initialBalance = initial,
                balance = initial,
                iconIndex = d.iconIndex,
                iconColorArgb = d.iconColorArgb
            )
            withContext(Dispatchers.IO) { upsert(account) }
            draft.update { null }
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
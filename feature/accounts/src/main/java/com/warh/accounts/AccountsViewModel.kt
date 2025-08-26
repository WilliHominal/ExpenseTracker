package com.warh.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.domain.models.Account
import com.warh.domain.models.AccountType
import com.warh.domain.use_cases.ObserveAccountsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Currency
import java.util.Locale

data class CurrencyTotalUi(
    val currency: String,
    val totalMinor: Long
)

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
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
) : ViewModel() {

    val ui: StateFlow<AccountsUiState> =
        observeAccounts().map { accounts ->
            val totals = accounts
                .groupBy { it.currency }
                .map { (code, list) -> CurrencyTotalUi(code, list.sumOf { it.balance }) }
                .sortedBy { it.currency }

            AccountsUiState(
                accounts = accounts,
                totalsByCurrency = totals
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountsUiState()
        )
}
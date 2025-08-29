package com.warh.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.domain.models.Account
import com.warh.domain.use_cases.DeleteAccountUseCase
import com.warh.domain.use_cases.ObserveAccountsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CurrencyTotalUi(
    val currency: String,
    val totalMinor: Long
)

data class AccountsUiState(
    val loading: Boolean = true,
    val accounts: List<Account> = emptyList(),
    val totalsByCurrency: List<CurrencyTotalUi> = emptyList()
)

class AccountsViewModel(
    observeAccounts: ObserveAccountsUseCase,
    private val deleteAccount: DeleteAccountUseCase
) : ViewModel() {

    private val _loading = MutableStateFlow(false)

    val ui: StateFlow<AccountsUiState> =
        combine(
            observeAccounts(),
            _loading
        ) { accounts, loading ->
            val sortedAccounts = accounts.sortedWith(
                compareBy<Account> { it.currency }
                    .thenByDescending { it.balance }
                    .thenBy { it.name.lowercase() }
                    .thenBy { it.id ?: Long.MAX_VALUE }
            )

            val totals = sortedAccounts
                .groupBy { it.currency }
                .map { (code, list) -> CurrencyTotalUi(code, list.sumOf { it.balance }) }
                .sortedBy { it.currency }

            AccountsUiState(
                loading = loading,
                accounts = sortedAccounts,
                totalsByCurrency = totals
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountsUiState()
        )

    fun delete(id: Long) {
        viewModelScope.launch {
            _loading.value = true
            withContext(Dispatchers.IO) { deleteAccount(id) }
            _loading.value = false
        }
    }
}
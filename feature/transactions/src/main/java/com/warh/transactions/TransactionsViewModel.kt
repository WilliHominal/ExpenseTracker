package com.warh.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.warh.domain.models.Account
import com.warh.domain.models.Category
import com.warh.domain.models.Transaction
import com.warh.domain.models.TransactionFilter
import com.warh.domain.use_cases.ObserveAccountsUseCase
import com.warh.domain.use_cases.GetCategoriesUseCase
import com.warh.domain.use_cases.GetTransactionsPagerUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class TransactionsViewModel(
    private val pagerUseCase: GetTransactionsPagerUseCase,
    private val observeAccounts: ObserveAccountsUseCase,
    private val getCategories: GetCategoriesUseCase,
) : ViewModel() {

    private val _filter = MutableStateFlow(TransactionFilter())
    val filter: StateFlow<TransactionFilter> = _filter

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _filtersVisible = MutableStateFlow(false)
    val filtersVisible: StateFlow<Boolean> = _filtersVisible

    init {
        viewModelScope.launch {
            observeAccounts().collect { accs ->
                _accounts.value = accs
                val validIds = accs.mapNotNull { it.id }.toSet()
                _filter.update { f -> f.copy(accountIds = f.accountIds.intersect(validIds)) }
            }
        }

        viewModelScope.launch {
            val cats = io { runCatching { getCategories() }.getOrDefault(emptyList()) }
            _categories.value = cats
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val paging: Flow<PagingData<Transaction>> =
        _filter
            .map { it.normalize() }
            .flatMapLatest { pagerUseCase(it) }
            .flowOn(Dispatchers.IO)
            .cachedIn(viewModelScope)

    fun setText(q: String) = _filter.update { it.copy(text = q) }
    fun setDateRange(from: LocalDateTime?, to: LocalDateTime?) = _filter.update { it.copy(from = from, to = to) }
    fun toggleAccount(id: Long) = _filter.update {
        val s = it.accountIds.toMutableSet().also { set -> if (!set.add(id)) set.remove(id) }
        it.copy(accountIds = s)
    }
    fun toggleCategory(id: Long) = _filter.update {
        val s = it.categoryIds.toMutableSet().also { set -> if (!set.add(id)) set.remove(id) }
        it.copy(categoryIds = s)
    }

    fun toggleFiltersVisible() { _filtersVisible.update { !it } }

    private fun TransactionFilter.normalize(): TransactionFilter =
        copy(text = text?.trim()?.lowercase()?.takeIf { it.isNotEmpty() })

    private suspend fun <T> io(block: suspend () -> T): T =
        withContext(Dispatchers.IO) { block() }
}
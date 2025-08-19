package com.warh.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.warh.domain.models.Transaction
import com.warh.domain.models.TransactionFilter
import com.warh.domain.use_cases.GetTransactionsPagerUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class TransactionsViewModel(
    private val pagerUseCase: GetTransactionsPagerUseCase
) : ViewModel() {
    fun paging(filter: TransactionFilter): Flow<PagingData<Transaction>> {
        val norm = filter.copy(text = filter.text?.trim()?.lowercase())

        return pagerUseCase(norm)
            .flowOn(Dispatchers.IO)
            .cachedIn(viewModelScope)
    }
}
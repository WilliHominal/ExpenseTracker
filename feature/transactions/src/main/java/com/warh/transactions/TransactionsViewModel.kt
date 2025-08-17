package com.warh.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.warh.domain.models.Transaction
import com.warh.domain.models.TransactionFilter
import com.warh.domain.use_cases.GetTransactionsPagerUseCase
import kotlinx.coroutines.flow.Flow

class TransactionsViewModel(
    private val pagerUseCase: GetTransactionsPagerUseCase
) : ViewModel() {
    fun paging(filter: TransactionFilter): Flow<PagingData<Transaction>> =
        pagerUseCase(filter).cachedIn(viewModelScope)
}
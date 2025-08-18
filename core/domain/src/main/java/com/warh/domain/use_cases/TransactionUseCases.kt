package com.warh.domain.use_cases

import androidx.paging.PagingData
import com.warh.domain.models.Transaction
import com.warh.domain.models.TransactionFilter
import com.warh.domain.repositories.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsPagerUseCase(private val repo: TransactionRepository) {
    operator fun invoke(filter: TransactionFilter): Flow<PagingData<Transaction>> = repo.pager(filter)
}

class AddTransactionUseCase(private val repo: TransactionRepository) {
    suspend operator fun invoke(tx: Transaction): Long = repo.add(tx)
}

class DeleteTransactionUseCase(private val repo: TransactionRepository) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}

class GetMerchantSuggestionsUseCase(private val repo: TransactionRepository) {
    suspend operator fun invoke(prefix: String): List<String> = repo.merchantSuggestions(prefix)
}

class GetAccountTransactionsUseCase(
    private val repo: TransactionRepository
) {
    suspend operator fun invoke(accountId: Long, filter: TransactionFilter): List<Transaction> {
        return repo.listByAccount(accountId, filter)
    }
}
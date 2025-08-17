package com.warh.domain.repositories

import androidx.paging.PagingData
import com.warh.domain.models.Transaction
import com.warh.domain.models.TransactionFilter
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun pager(filter: TransactionFilter): Flow<PagingData<Transaction>>
    suspend fun add(tx: Transaction): Long
    suspend fun delete(id: Long)
    suspend fun list(filter: TransactionFilter): List<Transaction>
    suspend fun merchantSuggestions(prefix: String): List<String>
}
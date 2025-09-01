package com.warh.domain.repositories

import androidx.paging.PagingData
import com.warh.domain.dto.CategorySpendDTO
import com.warh.domain.dto.MonthlySumDTO
import com.warh.domain.models.Transaction
import com.warh.domain.models.TransactionFilter
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.YearMonth

interface TransactionRepository {
    fun pager(filter: TransactionFilter): Flow<PagingData<Transaction>>
    suspend fun add(tx: Transaction): Long
    suspend fun delete(id: Long)
    suspend fun list(filter: TransactionFilter): List<Transaction>
    suspend fun merchantSuggestions(prefix: String): List<String>
    suspend fun listByAccount(accountId: Long, filter: TransactionFilter): List<Transaction>
    suspend fun sumsByMonth(from: LocalDateTime?, to: LocalDateTime?, accountId: Long?): List<MonthlySumDTO>
    suspend fun spentByCategory(ym: YearMonth, accountId: Long?): List<CategorySpendDTO>
    suspend fun get(id: Long): Transaction?
    suspend fun upsert(tx: Transaction): Long
}
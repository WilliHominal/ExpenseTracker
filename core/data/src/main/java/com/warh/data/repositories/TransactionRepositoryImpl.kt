package com.warh.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.warh.data.db.AppDatabase
import com.warh.data.mappers.toDomain
import com.warh.data.mappers.toEntity
import com.warh.domain.models.Transaction
import com.warh.domain.models.TransactionFilter
import com.warh.domain.repositories.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionRepositoryImpl(
    private val db: AppDatabase,
    private val io: CoroutineDispatcher
) : TransactionRepository {
    override fun pager(filter: TransactionFilter): Flow<PagingData<Transaction>> =
        Pager(
            config = PagingConfig(pageSize = 30, prefetchDistance = 60, enablePlaceholders = false),
            pagingSourceFactory = { db.transactionDao().paging(filter.from, filter.to, filter.text) }
        ).flow
            .map { it.map { e -> e.toDomain() } }
            .flowOn(io)

    override suspend fun add(tx: Transaction): Long = withContext(io) {
        db.transactionDao().upsert(tx.toEntity())
    }

    override suspend fun delete(id: Long) = withContext(io) {
        db.transactionDao().delete(id)
    }

    override suspend fun list(filter: TransactionFilter): List<Transaction> = withContext(io) {
        val df = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val fromStr = filter.from?.format(df)
        val toStr = filter.to?.format(df)
        val entities = db.transactionDao().list(fromStr, toStr, filter.text)
        entities.map { it.toDomain() }
    }

    override suspend fun merchantSuggestions(prefix: String): List<String> = withContext(io) {
        db.merchantSuggestDao().suggestions(prefix)
    }
}
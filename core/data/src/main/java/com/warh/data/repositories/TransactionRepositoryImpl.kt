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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.YearMonth

class TransactionRepositoryImpl(
    private val db: AppDatabase,
) : TransactionRepository {
    override fun pager(filter: TransactionFilter): Flow<PagingData<Transaction>> =
        Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 60,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                db.transactionDao().paging(filter.from, filter.to, filter.text)
            }
        ).flow
        .map { paging -> paging.map { it.toDomain() } }

    override suspend fun add(tx: Transaction): Long =
        db.transactionDao().upsert(tx.toEntity())

    override suspend fun delete(id: Long) =
        db.transactionDao().delete(id)

    override suspend fun list(filter: TransactionFilter): List<Transaction> {
        val df = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val fromStr = filter.from?.format(df)
        val toStr = filter.to?.format(df)
        val entities = db.transactionDao().list(fromStr, toStr, filter.text)
        return entities.map { it.toDomain() }
    }

    override suspend fun merchantSuggestions(prefix: String): List<String> =
        db.merchantSuggestDao().suggestions(prefix)

    override suspend fun listByAccount(accountId: Long, filter: TransactionFilter): List<Transaction> {
        val df = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val fromStr = filter.from?.format(df)
        val toStr = filter.to?.format(df)
        val entities = db.transactionDao().listByAccount(
            accountId = accountId,
            from = fromStr,
            to = toStr,
            text = filter.text
        )
        return entities.map { it.toDomain() }
    }

    override suspend fun sumsByMonth(from: LocalDateTime?, to: LocalDateTime?, accountId: Long?) =
        db.transactionDao().sumsByMonth(from, to, accountId)

    override suspend fun spentByCategory(ym: YearMonth, accountId: Long?) =
        db.transactionDao().spentByCategory(ym.toString(), accountId)
}
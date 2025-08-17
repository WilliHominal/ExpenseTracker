package com.warh.data.repositories

import com.warh.data.db.AppDatabase
import com.warh.data.mappers.toDomain
import com.warh.data.mappers.toEntity
import com.warh.domain.models.Account
import com.warh.domain.repositories.AccountRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AccountRepositoryImpl(
    private val db: AppDatabase,
    private val io: CoroutineDispatcher
) : AccountRepository {

    override suspend fun all(): List<Account> = withContext(io) {
        db.accountDao().all().map { it.toDomain() }
    }

    override suspend fun get(id: Long): Account? = withContext(io) {
        db.accountDao().get(id)?.toDomain()
    }

    override suspend fun upsert(account: Account): Long = withContext(io) {
        db.accountDao().upsert(account.toEntity())
    }

    override suspend fun delete(id: Long) = withContext(io) {
        db.accountDao().delete(id)
    }

    override suspend fun txCountForAccount(id: Long): Int = withContext(io) {
        db.accountDao().txCountForAccount(id)
    }
}
package com.warh.data.repositories

import com.warh.data.db.AppDatabase
import com.warh.data.mappers.toDomain
import com.warh.data.mappers.toEntity
import com.warh.domain.models.Account
import com.warh.domain.repositories.AccountRepository

class AccountRepositoryImpl(
    private val db: AppDatabase,
) : AccountRepository {

    override suspend fun all(): List<Account> =
        db.accountDao().all().map { it.toDomain() }

    override suspend fun get(id: Long): Account? =
        db.accountDao().get(id)?.toDomain()

    override suspend fun upsert(account: Account): Long =
        db.accountDao().upsert(account.toEntity())

    override suspend fun delete(id: Long) =
        db.accountDao().delete(id)

    override suspend fun txCountForAccount(id: Long): Int =
        db.accountDao().txCountForAccount(id)
}
package com.warh.data.repositories

import androidx.annotation.WorkerThread
import com.warh.data.daos.AccountDao
import com.warh.data.entities.AccountEntity
import com.warh.data.mappers.toDomain
import com.warh.domain.models.Account
import com.warh.domain.repositories.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class AccountRepositoryImpl(
    private val dao: AccountDao
) : AccountRepository {

    override fun all(): Flow<List<Account>> =
        dao.allFlow().map { list ->
            list
                .map { it.toDomain() }
                .sortedWith(
                    compareBy<Account> { it.currency }
                        .thenBy { it.type.ordinal }
                        .thenByDescending { it.balance }
                        .thenBy { it.name.trim().lowercase(Locale.ROOT) }
                )
        }

    override suspend fun get(id: Long): Account? =
        dao.get(id)?.toDomain()

    @WorkerThread
    override suspend fun upsert(account: Account): Long {
        val id = account.id
        val typeString = account.type.name

        if (id == null) {
            val entity = AccountEntity(
                id = null,
                name = account.name,
                type = typeString,
                currency = account.currency,
                initialBalance = account.initialBalance,
                balance = account.initialBalance,
                iconIndex = account.iconIndex,
                iconColorArgb = account.iconColorArgb
            )
            return dao.insert(entity)
        }

        val current = dao.get(id)
        if (current == null) {
            val entity = AccountEntity(
                id = null,
                name = account.name,
                type = typeString,
                currency = account.currency,
                initialBalance = account.initialBalance,
                balance = account.initialBalance,
                iconIndex = account.iconIndex,
                iconColorArgb = account.iconColorArgb
            )
            return dao.insert(entity)
        }

        if (current.initialBalance != account.initialBalance) {
            dao.applyInitialDelta(
                id = id,
                oldInitial = current.initialBalance,
                newInitial = account.initialBalance
            )
        }

        dao.updateAccountWithoutTouchingBalance(
            id = id,
            name = account.name,
            type = typeString,
            currency = account.currency,
            initialBalance = account.initialBalance,
            iconIndex = account.iconIndex,
            iconColorArgb = account.iconColorArgb
        )

        return id
    }

    override suspend fun delete(id: Long) =
        dao.delete(id)

    override suspend fun txCountForAccount(id: Long): Int =
        dao.txCountForAccount(id)
}
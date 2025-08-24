package com.warh.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.warh.data.entities.AccountEntity
import com.warh.domain.dto.CurrencyTotalRow
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY name")
    fun allFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun get(id: Long): AccountEntity?

    @Insert
    suspend fun insert(account: AccountEntity): Long

    @Query("""
        UPDATE accounts
        SET name = :name,
            type = :type,
            currency = :currency,
            initialBalance = :initialBalance,
            iconIndex = :iconIndex,
            iconColorArgb = :iconColorArgb
        WHERE id = :id
    """)
    suspend fun updateAccountWithoutTouchingBalance(
        id: Long,
        name: String,
        type: String,
        currency: String,
        initialBalance: Long,
        iconIndex: Int,
        iconColorArgb: Long?
    ): Int

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :id")
    suspend fun txCountForAccount(id: Long): Int

    @Query("""
       SELECT currency, SUM(balance) AS totalMinor
       FROM accounts
       GROUP BY currency
    """)
    suspend fun totalsByCurrency(): List<CurrencyTotalRow>

    @Query("""
        UPDATE accounts
        SET initialBalance = :newInitial,
            balance        = balance + (:newInitial - :oldInitial)
        WHERE id = :id
    """)
    suspend fun applyInitialDelta(
        id: Long,
        oldInitial: Long,
        newInitial: Long
    ): Int

    @Query("""
        UPDATE accounts
        SET balance = balance + :delta
        WHERE id = :id
    """)
    suspend fun applyTransactionDelta(
        id: Long,
        delta: Long
    ): Int
}
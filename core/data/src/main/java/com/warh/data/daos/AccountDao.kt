package com.warh.data.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.warh.data.entities.AccountEntity

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY name")
    suspend fun all(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun get(id: Long): AccountEntity?

    @Upsert
    suspend fun upsert(entity: AccountEntity): Long

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :id")
    suspend fun txCountForAccount(id: Long): Int
}
package com.warh.domain.repositories

import com.warh.domain.models.Account

interface AccountRepository {
    suspend fun all(): List<Account>
    suspend fun get(id: Long): Account?
    suspend fun upsert(account: Account): Long
    suspend fun delete(id: Long)
    suspend fun txCountForAccount(id: Long): Int
}
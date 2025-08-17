package com.warh.data.daos

import androidx.room.Dao
import androidx.room.Query

@Dao
interface MerchantSuggestDao {
    @Query("SELECT DISTINCT merchant FROM transactions WHERE merchant IS NOT NULL AND merchant LIKE :prefix || '%' ORDER BY merchant LIMIT 8")
    suspend fun suggestions(prefix: String): List<String>
}
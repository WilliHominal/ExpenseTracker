package com.warh.data.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.warh.data.entities.TransactionEntity
import com.warh.data.utils.CategorySpentRow
import java.time.LocalDateTime

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT * FROM transactions
        WHERE (:from IS NULL OR date >= :from)
          AND (:to   IS NULL OR date <= :to)
          AND (:text IS NULL OR note LIKE '%' || :text || '%' OR merchant LIKE '%' || :text || '%')
        ORDER BY date DESC
        """
    )
    fun paging(
        from: LocalDateTime?,
        to: LocalDateTime?,
        text: String?,
    ): PagingSource<Int, TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COALESCE(SUM(amountMinor),0) FROM transactions WHERE type='EXPENSE' AND categoryId=:categoryId AND substr(date,1,7)=:yearMonth")
    suspend fun spentForCategoryYearMonth(categoryId: Long, yearMonth: String): Long

    @Query("""
        SELECT categoryId AS categoryId, COALESCE(SUM(amountMinor), 0) AS spent
        FROM transactions
        WHERE type = 'EXPENSE'
          AND categoryId IN (:categoryIds)
          AND substr(date, 1, 7) = :yearMonth
        GROUP BY categoryId
    """)
    suspend fun spentForCategories(
        categoryIds: List<Long>,
        yearMonth: String
    ): List<CategorySpentRow>

    @Query("""
        SELECT * FROM transactions
        WHERE (:from IS NULL OR date >= :from)
          AND (:to IS NULL OR date < :to)
          AND (:text IS NULL OR (merchant LIKE '%'||:text||'%' OR note LIKE '%'||:text||'%'))
        ORDER BY date DESC
    """)
    suspend fun list(from: String?, to: String?, text: String?): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :id")
    suspend fun txCountForCategory(id: Long): Int
}
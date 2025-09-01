package com.warh.data.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.warh.domain.dto.CategorySpendDTO
import com.warh.domain.dto.MonthlySumDTO
import com.warh.data.entities.TransactionEntity
import com.warh.data.utils.CategorySpentRow
import java.time.LocalDateTime

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): TransactionEntity?

    @Query(
        """
            SELECT * FROM transactions
            WHERE (:from IS NULL OR date >= :from)
              AND (:to   IS NULL OR date  <  :to)
              AND (
                    :text IS NULL
                 OR  LOWER(merchant) LIKE '%'||:text||'%'
                 OR  LOWER(note)     LIKE '%'||:text||'%'
              )
              AND ( :accountIds IS NULL  OR accountId IN (:accountIds) )
              AND ( :categoryIds IS NULL OR (categoryId IS NOT NULL AND categoryId IN (:categoryIds)) )
            ORDER BY date DESC
        """
    )
    fun paging(
        from: LocalDateTime?,
        to: LocalDateTime?,
        text: String?,
        accountIds: List<Long>?,
        categoryIds: List<Long>?
    ): PagingSource<Int, TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("""
        SELECT COALESCE(SUM(amountMinor), 0)
        FROM transactions
        WHERE type = 'EXPENSE'
          AND categoryId = :categoryId
          AND date >= :from AND date < :to
    """)
    suspend fun spentForCategoryInRange(
        categoryId: Long,
        from: String,
        to: String
    ): Long

    @Query("""
        SELECT categoryId AS categoryId, COALESCE(SUM(amountMinor), 0) AS spent
        FROM transactions
        WHERE type = 'EXPENSE'
          AND categoryId IN (:categoryIds)
          AND date >= :from AND date < :to
        GROUP BY categoryId
    """)
    suspend fun spentForCategoriesInRange(
        categoryIds: List<Long>,
        from: String,
        to: String
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

    @Query("""
        SELECT * FROM transactions
        WHERE accountId = :accountId
          AND (:from IS NULL OR date >= :from)
          AND (:to   IS NULL OR date <  :to)
          AND (:text IS NULL OR (merchant LIKE '%'||:text||'%' OR note LIKE '%'||:text||'%'))
        ORDER BY date DESC
    """)
    suspend fun listByAccount(
        accountId: Long,
        from: String?,
        to: String?,
        text: String?
    ): List<TransactionEntity>

    @Query("""
        SELECT yearMonth,
               SUM(CASE WHEN type = 'INCOME'  THEN amountMinor ELSE 0 END) AS incomeMinor,
               SUM(CASE WHEN type = 'EXPENSE' THEN amountMinor ELSE 0 END) AS expenseMinor
        FROM transactions
        WHERE (:from IS NULL OR date >= :from)
          AND (:to   IS NULL OR date  < :to)
          AND (:accountId IS NULL OR accountId = :accountId)
        GROUP BY yearMonth
        ORDER BY yearMonth ASC
    """)
    suspend fun sumsByMonth(from: LocalDateTime?, to: LocalDateTime?, accountId: Long?): List<MonthlySumDTO>

    @Query("""
        SELECT categoryId AS categoryId,
               COALESCE(SUM(amountMinor),0) AS spentMinor
        FROM transactions
        WHERE type = 'EXPENSE'
          AND yearMonth = :ym
          AND (:accountId IS NULL OR accountId = :accountId)
        GROUP BY categoryId
        ORDER BY spentMinor DESC
    """)
    suspend fun spentByCategory(ym: String, accountId: Long?): List<CategorySpendDTO>
}
package com.warh.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.warh.data.entities.BudgetEntity

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(b: BudgetEntity)

    @Query("DELETE FROM budgets WHERE categoryId = :categoryId AND year = :year AND month = :month")
    suspend fun remove(categoryId: Long, year: Int, month: Int)

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month ORDER BY categoryId")
    suspend fun budgetsFor(year: Int, month: Int): List<BudgetEntity>

    @Query("SELECT COUNT(*) FROM budgets WHERE categoryId = :id")
    suspend fun budgetCountForCategory(id: Long): Int
}
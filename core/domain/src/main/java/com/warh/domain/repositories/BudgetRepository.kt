package com.warh.domain.repositories

import com.warh.domain.models.Budget
import com.warh.domain.models.BudgetProgress

interface BudgetRepository {
    suspend fun upsert(budget: Budget)
    suspend fun remove(categoryId: Long, year: Int, month: Int)
    suspend fun budgetsFor(year: Int, month: Int): List<Budget>
    suspend fun spentFor(categoryId: Long, year: Int, month: Int): Long
    suspend fun progressFor(year: Int, month: Int): List<BudgetProgress>
}
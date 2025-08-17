package com.warh.domain.use_cases

import com.warh.domain.models.Budget
import com.warh.domain.repositories.BudgetRepository

class GetBudgetProgressForMonthUseCase(private val repo: BudgetRepository) {
    suspend operator fun invoke(year: Int, month: Int) = repo.progressFor(year, month)
}

class UpsertBudgetUseCase(private val repo: BudgetRepository) {
    suspend operator fun invoke(budget: Budget) = repo.upsert(budget)
}

class RemoveBudgetUseCase(private val repo: BudgetRepository) {
    suspend operator fun invoke(categoryId: Long, year: Int, month: Int) = repo.remove(categoryId, year, month)
}
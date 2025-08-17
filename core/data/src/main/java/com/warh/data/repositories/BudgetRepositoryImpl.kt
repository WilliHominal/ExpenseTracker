package com.warh.data.repositories

import com.warh.commons.Strings
import com.warh.commons.get
import com.warh.data.R
import com.warh.data.db.AppDatabase
import com.warh.data.mappers.toDomain
import com.warh.data.mappers.toEntity
import com.warh.domain.models.Budget
import com.warh.domain.models.BudgetProgress
import com.warh.domain.models.Category
import com.warh.domain.repositories.BudgetRepository
import com.warh.domain.repositories.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Locale

class BudgetRepositoryImpl(
    private val db: AppDatabase,
    private val categoryRepo: CategoryRepository,
    private val io: CoroutineDispatcher,
    private val strings: Strings,
) : BudgetRepository {

    override suspend fun upsert(budget: Budget) = withContext(io) {
        db.budgetDao().upsert(budget.toEntity())
    }

    override suspend fun remove(categoryId: Long, year: Int, month: Int) = withContext(io) {
        db.budgetDao().remove(categoryId, year, month)
    }

    override suspend fun budgetsFor(year: Int, month: Int): List<Budget> = withContext(io) {
        db.budgetDao().budgetsFor(year, month).map { it.toDomain() }
    }

    override suspend fun spentFor(categoryId: Long, year: Int, month: Int): Long = withContext(io) {
        val ym = String.format(Locale.US, "%04d-%02d", year, month)
        db.transactionDao().spentForCategoryYearMonth(categoryId, ym)
    }

    override suspend fun progressFor(year: Int, month: Int): List<BudgetProgress> = withContext(io) {
        val ym = String.format(Locale.US, "%04d-%02d", year, month)

        val budgets = db.budgetDao().budgetsFor(year, month)
        if (budgets.isEmpty()) return@withContext emptyList()

        val cats = categoryRepo.all().associateBy { it.id }

        val categoryIds = budgets.map { it.categoryId }
        val spentRows = db.transactionDao().spentForCategories(categoryIds, ym)
        val spentMap = spentRows.associate { it.categoryId to it.spent }

        budgets.map { be ->
            val b = be.toDomain()
            val spent = spentMap[b.categoryId] ?: 0L
            val fallbackName = strings[R.string.category_unnamed]
            BudgetProgress(
                category = cats[b.categoryId]
                    ?: Category(b.categoryId, fallbackName, 0xFF9E9E9EL),
                year = year,
                month = month,
                limitMinor = b.limitMinor,
                spentMinor = spent,
            )
        }
    }
}
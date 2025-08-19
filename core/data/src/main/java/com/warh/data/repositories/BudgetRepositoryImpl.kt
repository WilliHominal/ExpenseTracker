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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BudgetRepositoryImpl(
    private val db: AppDatabase,
    private val categoryRepo: CategoryRepository,
    private val strings: Strings,
) : BudgetRepository {

    override suspend fun upsert(budget: Budget) =
        db.budgetDao().upsert(budget.toEntity())

    override suspend fun remove(categoryId: Long, year: Int, month: Int) =
        db.budgetDao().remove(categoryId, year, month)

    override suspend fun budgetsFor(year: Int, month: Int): List<Budget> =
        db.budgetDao().budgetsFor(year, month).map { it.toDomain() }

    override suspend fun spentFor(categoryId: Long, year: Int, month: Int): Long {
        val start = LocalDate.of(year, month, 1).atStartOfDay()
        val end = start.plusMonths(1)
        val df = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return db.transactionDao().spentForCategoryInRange(
            categoryId = categoryId,
            from = start.format(df),
            to = end.format(df)
        )
    }

    override suspend fun progressFor(year: Int, month: Int): List<BudgetProgress> {
        val start = LocalDate.of(year, month, 1).atStartOfDay()
        val end = start.plusMonths(1)
        val df = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val fromStr = start.format(df)
        val toStr = end.format(df)

        val budgets = db.budgetDao().budgetsFor(year, month)
        if (budgets.isEmpty()) return emptyList()

        val cats = categoryRepo.all().associateBy { it.id }

        val categoryIds = budgets.map { it.categoryId }
        val spentRows = db.transactionDao().spentForCategoriesInRange(categoryIds, fromStr, toStr)
        val spentMap = spentRows.associate { it.categoryId to it.spent }

        return budgets.map { be ->
            val b = be.toDomain()
            val spent = spentMap[b.categoryId] ?: 0L
            val fallbackName = strings[R.string.category_unnamed]
            BudgetProgress(
                category = cats[b.categoryId] ?: Category(b.categoryId, fallbackName, 0xFF9E9E9EL),
                year = year,
                month = month,
                limitMinor = b.limitMinor,
                spentMinor = spent,
            )
        }
    }
}
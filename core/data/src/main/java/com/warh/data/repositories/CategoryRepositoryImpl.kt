package com.warh.data.repositories

import com.warh.data.db.AppDatabase
import com.warh.data.mappers.toDomain
import com.warh.data.mappers.toEntity
import com.warh.domain.models.Category
import com.warh.domain.repositories.CategoryRepository

class CategoryRepositoryImpl(
    private val db: AppDatabase,
) : CategoryRepository {
    override suspend fun all(): List<Category> =
        db.categoryDao().all().map { it.toDomain() }

    override suspend fun get(id: Long): Category? =
        db.categoryDao().get(id)?.toDomain()

    override suspend fun upsert(c: Category): Long =
        db.categoryDao().upsert(c.toEntity())

    override suspend fun delete(id: Long) =
        db.categoryDao().delete(id)

    override suspend fun txCount(id: Long): Int =
        db.transactionDao().txCountForCategory(id)

    override suspend fun budgetCount(id: Long): Int =
        db.budgetDao().budgetCountForCategory(id)
}
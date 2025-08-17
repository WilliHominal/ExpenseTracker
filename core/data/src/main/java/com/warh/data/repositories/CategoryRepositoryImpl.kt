package com.warh.data.repositories

import com.warh.data.db.AppDatabase
import com.warh.data.mappers.toDomain
import com.warh.data.mappers.toEntity
import com.warh.domain.models.Category
import com.warh.domain.repositories.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CategoryRepositoryImpl(
    private val db: AppDatabase,
    private val io: CoroutineDispatcher
) : CategoryRepository {
    override suspend fun all(): List<Category> = withContext(io) {
        db.categoryDao().all().map { Category(it.id, it.name, it.colorArgb) }
    }

    override suspend fun get(id: Long): Category? = withContext(io) {
        db.categoryDao().get(id)?.toDomain()
    }

    override suspend fun upsert(c: Category): Long = withContext(io) {
        db.categoryDao().upsert(c.toEntity())
    }

    override suspend fun delete(id: Long) = withContext(io) {
        db.categoryDao().delete(id)
    }

    override suspend fun txCount(id: Long): Int = withContext(io) {
        db.transactionDao().txCountForCategory(id)
    }

    override suspend fun budgetCount(id: Long): Int = withContext(io) {
        db.budgetDao().budgetCountForCategory(id)
    }
}
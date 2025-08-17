package com.warh.domain.repositories

import com.warh.domain.models.Category

interface CategoryRepository {
    suspend fun all(): List<Category>
    suspend fun get(id: Long): Category?
    suspend fun upsert(c: Category): Long
    suspend fun delete(id: Long)
    suspend fun txCount(id: Long): Int
    suspend fun budgetCount(id: Long): Int
}
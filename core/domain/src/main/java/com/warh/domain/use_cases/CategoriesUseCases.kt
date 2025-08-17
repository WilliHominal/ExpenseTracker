package com.warh.domain.use_cases

import com.warh.domain.models.Category
import com.warh.domain.repositories.CategoryRepository

class GetCategoriesUseCase(private val repo: CategoryRepository) {
    suspend operator fun invoke(): List<Category> = repo.all()
}

class UpsertCategoryUseCase(private val repo: CategoryRepository) {
    suspend operator fun invoke(c: Category) = repo.upsert(c)
}

class DeleteCategoryUseCase(private val repo: CategoryRepository) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}

class CanDeleteCategoryUseCase(private val repo: CategoryRepository) {
    suspend operator fun invoke(id: Long): Boolean =
        repo.txCount(id) == 0 && repo.budgetCount(id) == 0
}
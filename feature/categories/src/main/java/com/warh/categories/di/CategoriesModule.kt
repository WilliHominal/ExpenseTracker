package com.warh.categories.di

import com.warh.categories.CategoriesViewModel
import com.warh.domain.use_cases.CanDeleteCategoryUseCase
import com.warh.domain.use_cases.DeleteCategoryUseCase
import com.warh.domain.use_cases.UpsertCategoryUseCase
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val categoriesModule = module {
    viewModelOf(::CategoriesViewModel)

    factoryOf(::UpsertCategoryUseCase)
    factoryOf(::DeleteCategoryUseCase)
    factoryOf(::CanDeleteCategoryUseCase)
}
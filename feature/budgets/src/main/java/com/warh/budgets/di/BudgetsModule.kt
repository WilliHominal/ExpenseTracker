package com.warh.budgets.di

import com.warh.budgets.BudgetsViewModel
import com.warh.domain.use_cases.GetBudgetProgressForMonthUseCase
import com.warh.domain.use_cases.RemoveBudgetUseCase
import com.warh.domain.use_cases.UpsertBudgetUseCase
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val budgetsModule = module {
    viewModelOf(::BudgetsViewModel)

    factoryOf(::GetBudgetProgressForMonthUseCase)
    factoryOf(::UpsertBudgetUseCase)
    factoryOf(::RemoveBudgetUseCase)
}
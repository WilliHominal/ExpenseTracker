package com.warh.transactions.di

import com.warh.domain.use_cases.AddTransactionUseCase
import com.warh.domain.use_cases.GetAccountUseCase
import com.warh.domain.use_cases.GetAccountsUseCase
import com.warh.domain.use_cases.GetCategoriesUseCase
import com.warh.domain.use_cases.GetMerchantSuggestionsUseCase
import com.warh.domain.use_cases.GetTransactionsPagerUseCase
import com.warh.transactions.AddEditTransactionViewModel
import com.warh.transactions.TransactionsExportViewModel
import com.warh.transactions.TransactionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val transactionsModule = module {
    viewModelOf(::TransactionsViewModel)
    viewModelOf(::AddEditTransactionViewModel)
    viewModelOf(::TransactionsExportViewModel)

    factoryOf(::GetTransactionsPagerUseCase)
    factoryOf(::GetAccountsUseCase)
    factoryOf(::GetAccountUseCase)
    factoryOf(::GetCategoriesUseCase)
    factoryOf(::AddTransactionUseCase)
    factoryOf(::GetMerchantSuggestionsUseCase)
}
package com.warh.transactions.di

import com.warh.domain.use_cases.AddTransactionUseCase
import com.warh.domain.use_cases.GetAccountTransactionsUseCase
import com.warh.domain.use_cases.GetAccountUseCase
import com.warh.domain.use_cases.ObserveAccountsUseCase
import com.warh.domain.use_cases.GetCategoriesUseCase
import com.warh.domain.use_cases.GetMerchantSuggestionsUseCase
import com.warh.domain.use_cases.GetMonthlyCategorySpendUseCase
import com.warh.domain.use_cases.GetMonthlySumsUseCase
import com.warh.domain.use_cases.GetTransactionUseCase
import com.warh.domain.use_cases.GetTransactionsPagerUseCase
import com.warh.domain.use_cases.UpsertTransactionUseCase
import com.warh.transactions.AddEditTransactionViewModel
import com.warh.transactions.TransactionsExportViewModel
import com.warh.transactions.TransactionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val transactionsModule = module {
    viewModelOf(::TransactionsViewModel)
    viewModelOf(::TransactionsExportViewModel)
    viewModel { (editingId: Long?) ->
        AddEditTransactionViewModel(
            editingId = editingId,
            getTransaction = get(),
            upsertTx = get(),
            observeAccounts = get(),
            getCategories = get(),
            getMerchantSuggestions = get(),
            strings = get()
        )
    }

    factoryOf(::GetTransactionUseCase)
    factoryOf(::UpsertTransactionUseCase)
    factoryOf(::GetTransactionsPagerUseCase)
    factoryOf(::ObserveAccountsUseCase)
    factoryOf(::GetAccountUseCase)
    factoryOf(::GetCategoriesUseCase)
    factoryOf(::AddTransactionUseCase)
    factoryOf(::GetMerchantSuggestionsUseCase)
    factoryOf(::GetAccountTransactionsUseCase)
    factoryOf(::GetMonthlySumsUseCase)
    factoryOf(::GetMonthlyCategorySpendUseCase)
}
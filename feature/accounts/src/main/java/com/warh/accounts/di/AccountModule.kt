package com.warh.accounts.di

import com.warh.accounts.AccountsViewModel
import com.warh.domain.use_cases.CanDeleteAccountUseCase
import com.warh.domain.use_cases.DeleteAccountUseCase
import com.warh.domain.use_cases.GetAccountUseCase
import com.warh.domain.use_cases.UpsertAccountUseCase
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val accountsModule = module {
    viewModelOf(::AccountsViewModel)

    factoryOf(::GetAccountUseCase)
    factoryOf(::UpsertAccountUseCase)
    factoryOf(::DeleteAccountUseCase)
    factoryOf(::CanDeleteAccountUseCase)
}
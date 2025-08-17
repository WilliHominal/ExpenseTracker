package com.warh.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.warh.data.repositories.AccountRepositoryImpl
import com.warh.data.repositories.BudgetRepositoryImpl
import com.warh.data.repositories.CategoryRepositoryImpl
import com.warh.data.repositories.TransactionRepositoryImpl
import com.warh.domain.repositories.AccountRepository
import com.warh.domain.repositories.BudgetRepository
import com.warh.domain.repositories.CategoryRepository
import com.warh.domain.repositories.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import com.warh.data.db.buildDatabase

val Context.userPrefs by preferencesDataStore(name = "user_prefs")

private fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
private fun provideUserPrefs(context: Context): DataStore<Preferences> = context.userPrefs

val dataModule = module {
    singleOf(::provideIoDispatcher) bind CoroutineDispatcher::class

    singleOf(::buildDatabase)
    singleOf(::provideUserPrefs)

    singleOf(::TransactionRepositoryImpl) bind TransactionRepository::class
    singleOf(::AccountRepositoryImpl) bind AccountRepository::class
    singleOf(::CategoryRepositoryImpl) bind CategoryRepository::class
    singleOf(::BudgetRepositoryImpl) bind BudgetRepository::class
}
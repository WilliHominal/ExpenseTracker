package com.warh.expensetracker

import android.app.Application
import com.warh.accounts.di.accountsModule
import com.warh.budgets.di.budgetsModule
import com.warh.categories.di.categoriesModule
import com.warh.commons.di.commonsModule
import com.warh.data.di.dataModule
import com.warh.transactions.di.transactionsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ExpenseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ExpenseApp)
            modules(
                dataModule,
                transactionsModule,
                budgetsModule,
                accountsModule,
                categoriesModule,
                commonsModule
            )
        }
    }
}
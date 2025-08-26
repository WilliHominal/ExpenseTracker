package com.warh.expensetracker

sealed interface Destinations {
    companion object {
        const val TRANSACTIONS = "tx/list"
        const val ADD_TRANSACTION = "tx/add"
        const val BUDGETS = "budgets"
        const val ACCOUNTS = "accounts"
        const val ACCOUNT_ADD = "accounts/add"
        const val ACCOUNT_DETAIL = "accounts/detail"
        const val CATEGORIES = "categories"
    }
}
package com.warh.expensetracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.warh.accounts.AccountsRoute
import com.warh.accounts.details.AccountDetailRoute
import com.warh.budgets.BudgetsRoute
import com.warh.categories.CategoriesRoute
import com.warh.transactions.AddEditTransactionRoute
import com.warh.transactions.TransactionsRoute

@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.TRANSACTIONS,
        modifier = modifier
    ) {
        composable(Destinations.TRANSACTIONS) {
            TransactionsRoute(onAddClick = { navController.navigate(Destinations.ADD_TRANSACTION) })
        }
        composable(Destinations.BUDGETS) { BudgetsRoute() }
        composable(Destinations.ADD_TRANSACTION) {
            AddEditTransactionRoute(onSaved = { navController.popBackStack() })
        }
        composable(Destinations.ACCOUNTS) { AccountsRoute(
            onAccountClick = { accountId ->
                navController.navigate("${Destinations.ACCOUNT_DETAIL}/$accountId")
            }
        ) }
        composable(Destinations.CATEGORIES) { CategoriesRoute() }

        composable(
            route = "${Destinations.ACCOUNT_DETAIL}/{accountId}",
            arguments = listOf(navArgument("accountId") { type = NavType.LongType })
        ) { entry ->
            val accountId = entry.arguments?.getLong("accountId") ?: 0L
            AccountDetailRoute(
                accountId = accountId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
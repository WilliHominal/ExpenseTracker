package com.warh.expensetracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import com.warh.accounts.AccountsRoute
import com.warh.accounts.details.AccountDetailRoute
import com.warh.budgets.BudgetsRoute
import com.warh.categories.CategoriesRoute
import com.warh.commons.bottom_bar.FabSpec
import com.warh.expensetracker.utils.composableAnimated
import com.warh.expensetracker.utils.composableNoAnim
import com.warh.transactions.AddEditTransactionRoute
import com.warh.transactions.TransactionsRoute

@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    setFab: (FabSpec?) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.TRANSACTIONS,
        modifier = modifier
    ) {
        composableNoAnim(Destinations.TRANSACTIONS) {
            TransactionsRoute(
                setFab = setFab,
                onAddClick = { navController.navigate(Destinations.ADD_TRANSACTION) },
            )
        }
        composableNoAnim(Destinations.BUDGETS) {
            BudgetsRoute(
                setFab = setFab
            )
        }
        composableNoAnim(Destinations.CATEGORIES) {
            CategoriesRoute(
                setFab = setFab
            )
        }
        composableNoAnim(Destinations.ACCOUNTS) {
            AccountsRoute(
                setFab = setFab,
                onAccountClick = { accountId ->
                    navController.navigate("${Destinations.ACCOUNT_DETAIL}/$accountId")
                }
            )
        }

        composableAnimated(Destinations.ADD_TRANSACTION) {
            AddEditTransactionRoute(
                setFab = setFab,
                onSaved = { navController.popBackStack() },
                onBack = { navController.navigateUp() }
            )
        }

        composableAnimated(
            route = "${Destinations.ACCOUNT_DETAIL}/{accountId}",
            arguments = listOf(navArgument("accountId") { type = NavType.LongType })
        ) { entry ->
            val accountId = entry.arguments?.getLong("accountId") ?: 0L
            AccountDetailRoute(
                accountId = accountId,
                onBack = { navController.navigateUp() }
            )
        }
    }
}
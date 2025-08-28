package com.warh.expensetracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import com.warh.accounts.AccountsRoute
import com.warh.accounts.add.AccountAddRoute
import com.warh.accounts.details.AccountDetailRoute
import com.warh.budgets.BudgetsRoute
import com.warh.categories.CategoriesRoute
import com.warh.commons.bottom_bar.FabSpec
import com.warh.expensetracker.utils.composableAnimated
import com.warh.expensetracker.utils.composableNoAnim
import com.warh.transactions.AddEditTransactionRoute
import com.warh.transactions.TransactionsRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
                },
                onNavigateToAdd = { id: Long? ->
                    if (id != null)
                        navController.navigate("${Destinations.ACCOUNT_ADD}?id=$id")
                    else
                        navController.navigate(Destinations.ACCOUNT_ADD)
                }
            )
        }

        composableAnimated(
            route = "${Destinations.ACCOUNT_ADD}?id={id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entry ->
            val idArg = entry.arguments?.getLong("id") ?: -1L
            val editingId: Long? = if (idArg > 0) idArg else null

            AccountAddRoute(
                vm = koinViewModel(parameters = { parametersOf(editingId) }),
                setFab = setFab,
                onBack = { navController.navigateUp() },
            )
        }

        composableAnimated(Destinations.ADD_TRANSACTION) {
            AddEditTransactionRoute(
                setFab = setFab,
                onSaved = { navController.navigateUp() },
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
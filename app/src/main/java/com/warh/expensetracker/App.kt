package com.warh.expensetracker

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.warh.designsystem.ExpenseTheme
import com.warh.designsystem.SyncSystemBarsWithTheme

//TODO: Acomodar un poco el diseño segun el de v0

@Composable
fun App() {
    val nav = rememberNavController()
    ExpenseTheme {
        SyncSystemBarsWithTheme()

        val backStackEntry by nav.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        val topLevelRoutes = setOf(
            Destinations.TRANSACTIONS,
            Destinations.BUDGETS,
            Destinations.ACCOUNTS,
            Destinations.CATEGORIES,
        )

        val showBottomBar = currentRoute in topLevelRoutes

        Scaffold(
            contentWindowInsets = WindowInsets(0),
            bottomBar = { if (showBottomBar) BottomBar(nav, currentRoute) }
        ) { padding ->
            MainNavHost(
                modifier = Modifier.padding(padding),
                navController = nav
            )
        }
    }
}
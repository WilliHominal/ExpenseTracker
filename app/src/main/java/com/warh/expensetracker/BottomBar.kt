package com.warh.expensetracker

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

@Composable
fun BottomBar(nav: NavHostController, currentRoute: String?) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        NavigationBarItem(
            selected = currentRoute == Destinations.TRANSACTIONS,
            onClick = {
                nav.navigate(Destinations.TRANSACTIONS) {
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text(stringResource(R.string.bottom_menu_transactions)) }
        )

        NavigationBarItem(
            selected = currentRoute == Destinations.BUDGETS,
            onClick = {
                nav.navigate(Destinations.BUDGETS) {
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.PieChart, contentDescription = null) },
            label = { Text(stringResource(R.string.bottom_menu_budgets)) }
        )

        NavigationBarItem(
            selected = currentRoute == Destinations.CATEGORIES,
            onClick = {
                nav.navigate(Destinations.CATEGORIES) {
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true; restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Category, null) },
            label = { Text(stringResource(R.string.bottom_menu_categories)) }
        )

        NavigationBarItem(
            selected = currentRoute == Destinations.ACCOUNTS,
            onClick = {
                nav.navigate(Destinations.ACCOUNTS) {
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true; restoreState = true
                }
            },
            icon = { Icon(Icons.Default.AccountBalance, null) },
            label = { Text(stringResource(R.string.bottom_menu_accounts)) }
        )
    }
}
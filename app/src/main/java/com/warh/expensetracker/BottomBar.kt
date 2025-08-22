package com.warh.expensetracker

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.warh.designsystem.bottom_bar.BottomBarDesign

@Composable
fun BottomBar(nav: NavHostController, currentRoute: String?) {
    NavigationBar(
        containerColor = BottomBarDesign.containerColor(),
        contentColor   = BottomBarDesign.contentColor(),
        windowInsets   = NavigationBarDefaults.windowInsets
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
            label = { Text(stringResource(R.string.bottom_menu_transactions)) },
            colors = BottomBarDesign.itemColors()
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
            label = { Text(stringResource(R.string.bottom_menu_budgets)) },
            colors = BottomBarDesign.itemColors()
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
            label = { Text(stringResource(R.string.bottom_menu_categories)) },
            colors = BottomBarDesign.itemColors()
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
            label = { Text(stringResource(R.string.bottom_menu_accounts)) },
            colors = BottomBarDesign.itemColors()
        )
    }
}
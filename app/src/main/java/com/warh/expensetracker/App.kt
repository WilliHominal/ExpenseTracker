package com.warh.expensetracker

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.warh.commons.bottom_bar.FabSpec
import com.warh.commons.bottom_bar.LocalBottomBarBehavior
import com.warh.commons.scroll_utils.rememberHideOnScrollState
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

        var fabSpec by remember { mutableStateOf<FabSpec?>(null) }

        val hideBar = rememberHideOnScrollState()
        val hideFab  = hideBar.offsetY > 0f

        LaunchedEffect(currentRoute) { hideBar.reset() }

        CompositionLocalProvider(LocalBottomBarBehavior provides hideBar) {
            Scaffold(
                modifier = Modifier.nestedScroll(hideBar.connection),
                contentWindowInsets = WindowInsets(0),
                bottomBar = {
                    if (showBottomBar) {
                        BottomBar(
                            nav = nav,
                            currentRoute = currentRoute,
                            offsetY = hideBar.offsetY,
                            onMeasuredHeight = hideBar::setMeasuredHeight,
                            isSettling = hideBar.isSettling,
                        )
                    }
                },
                floatingActionButton = {
                    fabSpec?.takeIf { it.visible && !hideFab }?.let { spec ->
                        FloatingActionButton(
                            onClick = spec.onClick,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) { spec.content() }
                    }
                },
                floatingActionButtonPosition = FabPosition.End
            ) { padding ->
                MainNavHost(
                    modifier = Modifier.padding(padding),
                    navController = nav,
                    setFab = { fabSpec = it }
                )
            }
        }
    }
}
package com.warh.expensetracker.utils

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

private val noEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = { EnterTransition.None }
private val noExit:  AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?  = { ExitTransition.None }

fun NavGraphBuilder.composableNoAnim(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = noEnter,
        exitTransition = noExit,
        popEnterTransition = noEnter,
        popExitTransition = noExit,
        content = content
    )
}

fun NavGraphBuilder.composableAnimated(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    val duration = 220
    composable(
        route = route,
        arguments = arguments,
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(duration),
                initialOffsetX = { it / 3 }
            ) + fadeIn(tween(duration))
        },
        exitTransition = {
            fadeOut(tween(duration / 2))
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = tween(duration),
                initialOffsetX = { -it / 3 }
            ) + fadeIn(tween(duration))
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(duration),
                targetOffsetX = { it / 3 }
            ) + fadeOut(tween(duration))
        },
        content = content
    )
}
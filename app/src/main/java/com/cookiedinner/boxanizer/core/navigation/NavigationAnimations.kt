package com.cookiedinner.boxanizer.core.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

private fun AnimatedContentTransitionScope<NavBackStackEntry>.getDirection(): AnimatedContentTransitionScope.SlideDirection {
    val initialScreen = NavigationScreens.fromRoute(this.initialState.destination.route)
    val targetScreen = NavigationScreens.fromRoute(this.targetState.destination.route)
    return if (initialScreen.isMainScreen == targetScreen.isMainScreen) {
        if (initialScreen.ordinal < targetScreen.ordinal)
            AnimatedContentTransitionScope.SlideDirection.Left
        else
            AnimatedContentTransitionScope.SlideDirection.Right
    } else {
        if (initialScreen.ordinal < targetScreen.ordinal)
            AnimatedContentTransitionScope.SlideDirection.Down
        else
            AnimatedContentTransitionScope.SlideDirection.Up
    }
}
private fun AnimatedContentTransitionScope<NavBackStackEntry>.getEnterTransition(): EnterTransition {
    val direction = getDirection()
    return if (direction in listOf(AnimatedContentTransitionScope.SlideDirection.Left, AnimatedContentTransitionScope.SlideDirection.Right)) {
        slideIntoContainer(
            towards = direction,
            animationSpec = tween(easing = EaseOut),
        )
    } else {
        scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(250, easing = EaseOut)
        ) + slideIntoContainer(
            towards = direction,
            animationSpec = tween(250, easing = EaseOut),
            initialOffset = {
                it / 16
            }
        )
    } + fadeIn(tween(350, easing = EaseOut))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.getExitTransition(): ExitTransition {
    val direction = getDirection()
    return if (direction in listOf(AnimatedContentTransitionScope.SlideDirection.Left, AnimatedContentTransitionScope.SlideDirection.Right)) {
        slideOutOfContainer(
            towards = direction,
            animationSpec = tween(easing = EaseIn)
        )
    } else {
        scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(250, easing = EaseIn)
        ) + slideOutOfContainer(
            towards = direction,
            animationSpec = tween(250, easing = EaseIn),
            targetOffset = {
                it / 16
            }
        )
    } + fadeOut(tween(100, easing = EaseIn))
}

fun NavGraphBuilder.customNavigationComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    this.composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = { getEnterTransition() },
        exitTransition = { getExitTransition() },
        popEnterTransition = { getEnterTransition() },
        popExitTransition = { getExitTransition() },
        content = content
    )
}
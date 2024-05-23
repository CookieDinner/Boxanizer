package com.cookiedinner.boxanizer.core.navigation

import android.annotation.SuppressLint
import androidx.navigation.NavHostController

class Navigator {
    var navController: NavHostController? = null
        private set

    fun setNavController(navHostController: NavHostController) {
        navController = navHostController
    }

    fun changeNavigationScreen(route: String) {
        if (NavigationScreens.isSubScreen(route))
            throw RuntimeException("Route $route is not one of the main navigation screens!")

        navController?.navigate(route) {
            popUpTo(NavigationScreens.BoxesScreen.route) {
                saveState = true
            }
            restoreState = true
            launchSingleTop = true
        }
    }

    fun navigateToScreen(
        route: String,
        singleTop: Boolean = false,
        popUpTo: String? = null
    ) {
        navController?.navigate(route) {
            if (popUpTo != null)
                popUpTo(popUpTo)
            launchSingleTop = singleTop
        }
    }

    @SuppressLint("RestrictedApi")
    fun navigateToDeeperScreen(route: String) {
        val backstack = navController?.currentBackStack?.value?.map { NavigationScreens.fromRoute(it.destination.route) } ?: return
        if (backstack.contains(NavigationScreens.fromRoute(route)))
            return
        navController?.navigate(route)
    }

    fun popBackStack() {
        navController?.popBackStack()
    }
}
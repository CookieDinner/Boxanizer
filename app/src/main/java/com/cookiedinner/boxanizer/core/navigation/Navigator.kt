package com.cookiedinner.boxanizer.core.navigation

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
            popUpTo(NavigationScreens.BoxesScreen.route)
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

    fun popBackStack() {
        navController?.popBackStack()
    }
}
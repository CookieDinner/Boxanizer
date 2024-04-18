package com.cookiedinner.boxanizer.core.navigation

enum class NavigationScreens(val route: String) {
    BoxesScreen("boxes"),
    ItemsScreen("items"),
    SettingsScreen("settings"),
    BoxDetailsScreen("box_details");

    companion object {
        private const val MAIN_SCREENS_COUNT = 3
        fun fromRoute(route: String?): NavigationScreens = when (route?.split("/", "?")?.first()) {
            BoxesScreen.route -> BoxesScreen
            ItemsScreen.route -> ItemsScreen
            SettingsScreen.route -> SettingsScreen
            BoxDetailsScreen.route -> BoxDetailsScreen
            null -> BoxesScreen
            else -> throw IllegalArgumentException("Wrong navigation route: $route")
        }

        fun isSubScreen(route: String?): Boolean {
            return if (route != null) {
                fromRoute(route).ordinal >= MAIN_SCREENS_COUNT
            } else false
        }
    }
}
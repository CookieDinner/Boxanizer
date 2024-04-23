package com.cookiedinner.boxanizer.core.navigation

enum class NavigationScreens(val route: String, val isMainScreen: Boolean) {
    BoxesScreen("boxes", true),
    ItemsScreen("items", true),
    SettingsScreen("settings", true),
    BoxDetailsScreen("box_details", false),
    AddBoxScreen("add_box", false);

    companion object {
        fun fromRoute(route: String?): NavigationScreens = when (route?.split("/", "?")?.first()) {
            BoxesScreen.route -> BoxesScreen
            ItemsScreen.route -> ItemsScreen
            SettingsScreen.route -> SettingsScreen
            BoxDetailsScreen.route -> BoxDetailsScreen
            AddBoxScreen.route -> AddBoxScreen
            null -> BoxesScreen
            else -> throw IllegalArgumentException("Wrong navigation route: $route")
        }

        fun isSubScreen(route: String?): Boolean {
            return if (route != null) {
                !fromRoute(route).isMainScreen
            } else false
        }
    }
}
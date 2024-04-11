package com.cookiedinner.boxanizer.core.models

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val name: String,
    val route: String,
    val nestedRoutes: List<String>? = null,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
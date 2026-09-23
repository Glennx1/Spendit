package com.spendsplit.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object People : Screen("people", "People & IOUs", Icons.Default.People)
    object Categories : Screen("categories", "Categories", Icons.Default.Category)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Add : Screen("add", "Add Spend", Icons.Default.AddCircle)
}

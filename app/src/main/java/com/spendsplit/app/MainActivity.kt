package com.spendsplit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spendsplit.app.ui.add.AddTransactionScreen
import com.spendsplit.app.ui.add.AddTransactionViewModel
import com.spendsplit.app.ui.add.AddTransactionViewModelFactory
import com.spendsplit.app.ui.categories.CategoriesScreen
import com.spendsplit.app.ui.categories.CategoriesViewModel
import com.spendsplit.app.ui.categories.CategoriesViewModelFactory
import com.spendsplit.app.ui.dashboard.DashboardScreen
import com.spendsplit.app.ui.dashboard.DashboardViewModel
import com.spendsplit.app.ui.dashboard.DashboardViewModelFactory
import com.spendsplit.app.ui.navigation.Screen
import com.spendsplit.app.ui.people.PeopleScreen
import com.spendsplit.app.ui.people.PeopleViewModel
import com.spendsplit.app.ui.people.PeopleViewModelFactory
import com.spendsplit.app.ui.settings.SettingsDialog
import com.spendsplit.app.ui.theme.AccentBlack
import com.spendsplit.app.ui.theme.CardBorder
import com.spendsplit.app.ui.theme.CharcoalSecondary
import com.spendsplit.app.ui.theme.MutedSurface
import com.spendsplit.app.ui.theme.ObsidianBlack
import com.spendsplit.app.ui.theme.SpendSplitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as SpendSplitApp).repository

        setContent {
            val themePref by repository.themeFlow.collectAsState(initial = "SYSTEM")
            val isDark = when (themePref) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            SpendSplitTheme(darkTheme = isDark) {
                MainAppContent(repository)
            }
        }
    }
}

@Composable
fun MainAppContent(repository: com.spendsplit.app.data.repository.FinanceRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showSettingsDialog by remember { mutableStateOf(false) }

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(repository)
    )
    val addTransactionViewModel: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModelFactory(repository)
    )
    val peopleViewModel: PeopleViewModel = viewModel(
        factory = PeopleViewModelFactory(repository)
    )
    val categoriesViewModel: CategoriesViewModel = viewModel(
        factory = CategoriesViewModelFactory(repository)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val borderColor = CardBorder
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            ) {
                val navColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = ObsidianBlack,
                    indicatorColor = AccentBlack,
                    unselectedIconColor = CharcoalSecondary,
                    unselectedTextColor = CharcoalSecondary
                )

                // Dashboard
                NavigationBarItem(
                    selected = currentRoute == Screen.Dashboard.route,
                    onClick = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)) },
                    colors = navColors
                )

                // Add Spend (Center action)
                NavigationBarItem(
                    selected = currentRoute == Screen.Add.route,
                    onClick = {
                        navController.navigate(Screen.Add.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Spend"
                        )
                    },
                    label = { Text("New", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)) },
                    colors = navColors
                )

                // People / IOUs
                NavigationBarItem(
                    selected = currentRoute == Screen.People.route,
                    onClick = {
                        navController.navigate(Screen.People.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.People, contentDescription = "Contacts") },
                    label = { Text("People", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)) },
                    colors = navColors
                )

                // Categories
                NavigationBarItem(
                    selected = currentRoute == Screen.Categories.route,
                    onClick = {
                        navController.navigate(Screen.Categories.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
                    label = { Text("Categories", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)) },
                    colors = navColors
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onOpenSettings = { showSettingsDialog = true }
                    )
                }
                composable(Screen.Add.route) {
                    AddTransactionScreen(
                        viewModel = addTransactionViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(Screen.People.route) {
                    PeopleScreen(
                        viewModel = peopleViewModel
                    )
                }
                composable(Screen.Categories.route) {
                    CategoriesScreen(
                        viewModel = categoriesViewModel
                    )
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            repository = repository,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

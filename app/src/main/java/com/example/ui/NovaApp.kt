package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val selectedIcon: @Composable () -> Unit, val unselectedIcon: @Composable () -> Unit, val label: String) {
    object Home : Screen("home", { Icon(Icons.Filled.Home, "Home") }, { Icon(Icons.Outlined.Home, "Home") }, "Home")
    object Explore : Screen("explore", { Icon(Icons.Filled.Explore, "Explore") }, { Icon(Icons.Outlined.Explore, "Explore") }, "Explore")
    object Profile : Screen("profile", { Icon(Icons.Filled.Person, "Profile") }, { Icon(Icons.Outlined.Person, "Profile") }, "Profile")
}

@Composable
fun NovaApp() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Explore, Screen.Profile)

    Scaffold(
        containerColor = Color(0xFF0A0A0A),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            if (currentDestination?.route != "detail") {
                NavigationBar(containerColor = Color(0xFF121212)) {
                    items.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            label = { Text(screen.label, color = if (isSelected) Color.White else Color.Gray) },
                            selected = isSelected,
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFF2C56D1).copy(alpha = 0.3f),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { 
                HomeScreen(onNavigateToDetail = { id -> navController.navigate("detail/$id") }) 
            }
            composable("detail/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                WallpaperDetailScreen(
                    wallpaperId = id,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Explore.route) { 
                ExploreScreen(onNavigateToDetail = { id -> navController.navigate("detail/$id") }) 
            }
            composable(Screen.Profile.route) { SettingsScreen(onNavigateToAdmin = { navController.navigate("admin_studio") }) }
            composable("admin_studio") { AdminStudioScreen(onBackClick = { navController.popBackStack() }, viewModel = org.koin.androidx.compose.koinViewModel()) }
        }
    }
}

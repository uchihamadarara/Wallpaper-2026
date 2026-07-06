package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NovaApp() {
    val navController = rememberNavController()
    Scaffold(
        containerColor = Color(0xFF0A0A0A)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "admin_studio",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("admin_studio") { AdminStudioScreen(onBackClick = {  }, viewModel = org.koin.androidx.compose.koinViewModel()) }
        }
    }
}

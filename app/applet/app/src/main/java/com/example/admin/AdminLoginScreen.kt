package com.example.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AdminLoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AdminAuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val loginState by viewModel.adminLoginState.collectAsState()
    
    LaunchedEffect(loginState) {
        if (loginState is AdminLoginState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Admin Panel Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (loginState is AdminLoginState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.loginAdmin(email, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login to Admin Dashboard")
            }
        }
        
        if (loginState is AdminLoginState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (loginState as AdminLoginState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

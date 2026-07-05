package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WallpaperEntity
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudioScreen(
    onBackClick: () -> Unit,
    viewModel: AdminStudioViewModel = org.koin.androidx.compose.koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Anime") }
    var url by remember { mutableStateOf("") }
    
    var includeHome by remember { mutableStateOf(false) }
    var homeUrl by remember { mutableStateOf("") }
    
    var includeLock by remember { mutableStateOf(false) }
    var lockUrl by remember { mutableStateOf("") }
    
    var includeCharging by remember { mutableStateOf(false) }
    var chargingUrl by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0A0A0A),
        topBar = {
            TopAppBar(
                title = { Text("Creator Studio", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0A))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Add New Wallpaper Package", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4285F4),
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
            
            item {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4285F4),
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
            
            item {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Thumbnail Image URL") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4285F4),
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = includeHome,
                        onCheckedChange = { includeHome = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4285F4))
                    )
                    Text("Include Home Screen Video", color = Color.White)
                }
                if (includeHome) {
                    OutlinedTextField(
                        value = homeUrl,
                        onValueChange = { homeUrl = it },
                        label = { Text("Home Video URL (e.g., GDrive Direct Link)") },
                        modifier = Modifier.fillMaxWidth().padding(start = 32.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            }
            
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = includeLock,
                        onCheckedChange = { includeLock = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4285F4))
                    )
                    Text("Include Lock Screen Video", color = Color.White)
                }
                if (includeLock) {
                    OutlinedTextField(
                        value = lockUrl,
                        onValueChange = { lockUrl = it },
                        label = { Text("Lock Screen Video URL (e.g., GDrive Direct Link)") },
                        modifier = Modifier.fillMaxWidth().padding(start = 32.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            }
            
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = includeCharging,
                        onCheckedChange = { includeCharging = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4285F4))
                    )
                    Text("Include Charging Animation Video", color = Color.White)
                }
                if (includeCharging) {
                    OutlinedTextField(
                        value = chargingUrl,
                        onValueChange = { chargingUrl = it },
                        label = { Text("Charging Video URL (e.g., GDrive Direct Link)") },
                        modifier = Modifier.fillMaxWidth().padding(start = 32.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (title.isBlank() || url.isBlank()) {
                                snackbarHostState.showSnackbar("Title and Thumbnail are required")
                                return@launch
                            }
                            
                            val entity = WallpaperEntity(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                type = "PACKAGE",
                                category = category,
                                url = url,
                                homeVideoUrl = if (includeHome && homeUrl.isNotBlank()) homeUrl else null,
                                lockVideoUrl = if (includeLock && lockUrl.isNotBlank()) lockUrl else null,
                                chargingVideoUrl = if (includeCharging && chargingUrl.isNotBlank()) chargingUrl else null
                            )
                            viewModel.saveWallpaper(entity)
                            
                            snackbarHostState.showSnackbar("Wallpaper Package Saved!")
                            title = ""
                            url = ""
                            homeUrl = ""
                            lockUrl = ""
                            chargingUrl = ""
                            includeHome = false
                            includeLock = false
                            includeCharging = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                ) {
                    Text("Save to Database", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

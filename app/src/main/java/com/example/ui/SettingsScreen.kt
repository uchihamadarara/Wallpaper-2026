package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel(), onNavigateToAdmin: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    val googleAccount by viewModel.googleAccount.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val highQuality by viewModel.highQuality.collectAsState()
    val support120Hz by viewModel.support120Hz.collectAsState()
    val particleEffects by viewModel.particleEffects.collectAsState()
    val autoApply by viewModel.autoApply.collectAsState()
    val batterySaver by viewModel.batterySaver.collectAsState()
    
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    
    LaunchedEffect(Unit) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            viewModel.setGoogleAccount(account)
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.setGoogleAccount(account)
        } catch (e: ApiException) {
            // handle error
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .verticalScroll(scrollState)
            .padding(top = 48.dp, bottom = 24.dp)
    ) {
        // Profile Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = googleAccount?.photoUrl ?: "https://i.pravatar.cc/150?img=11"
                ),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .clickable { 
                        if (googleAccount == null) {
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = googleAccount?.displayName ?: "Sign in with Google",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.clickable {
                        if (googleAccount == null) {
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    }
                )
                if (googleAccount != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color(0xFF4285F4),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = googleAccount?.email ?: "Tap to sign in",
                fontSize = 14.sp,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (googleAccount != null) {
                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.clickable { 
                        // TODO: Implement Google Account Management Intent
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = rememberAsyncImagePainter("https://upload.wikimedia.org/wikipedia/commons/5/53/Google_%22G%22_Logo.svg"),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Manage Google Account",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(Icons.Filled.Favorite, "128", "Favorite\nWallpapers", Color(0xFF4285F4))
            StatItem(Icons.Filled.CheckCircle, "56", "Applied\nWallpapers", Color(0xFF4285F4))
            StatItem(Icons.Filled.Timer, "24h", "Active\nTime", Color(0xFF4285F4))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Settings Grid (2 columns)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Column 1
            Column(modifier = Modifier.weight(1f)) {
                SettingsSection("APPEARANCE") {
                    SettingsSwitchItem(Icons.Outlined.DarkMode, "Dark Mode", "Enable dark theme", darkMode, { viewModel.setDarkMode(it) })
                    SettingsSwitchItem(Icons.Outlined.Palette, "Dynamic Material", "Apply system color to app", dynamicColor, { viewModel.setDynamicColor(it) })
                    SettingsColorItem(Icons.Outlined.FormatPaint, "Accent Color", "Customize app accent color", Color(0xFF4285F4))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsSection("PERFORMANCE") {
                    SettingsSwitchItem(Icons.Outlined.HighQuality, "High Quality", "Enable high quality rendering", highQuality, { viewModel.setHighQuality(it) })
                    SettingsSwitchItem(Icons.Outlined.Speed, "120Hz Support", "Enable smooth 120Hz experience", support120Hz, { viewModel.setSupport120Hz(it) })
                    SettingsSwitchItem(Icons.Outlined.Memory, "GPU Optimization", "Use GPU for better performance", true, {  })
                    SettingsArrowItem(Icons.Outlined.Delete, "Cache Management", "Clear cached files")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsSection("SUPPORT") {
                    SettingsArrowItem(Icons.Outlined.Shield, "Privacy Policy", "Read our privacy policy")
                    SettingsArrowItem(Icons.Outlined.Description, "Terms of Service", "Read our terms of service")
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAdmin() }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Outlined.Build, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Admin Studio", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                            Text(text = "Upload custom package", fontSize = 12.sp, color = Color.Gray)
                        }
                        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            // Column 2
            Column(modifier = Modifier.weight(1f)) {
                SettingsSection("WALLPAPER") {
                    SettingsArrowItem(Icons.AutoMirrored.Filled.ShowChart, "Animation Speed", "Set wallpaper animation speed")
                    SettingsArrowItem(Icons.Outlined.Layers, "Parallax Strength", "Adjust parallax effect intensity")
                    SettingsSwitchItem(Icons.Outlined.BlurOn, "Particle Effects", "Enable particle effects", particleEffects, { viewModel.setParticleEffects(it) })
                    SettingsSwitchItem(Icons.Outlined.EnergySavingsLeaf, "Battery Saver", "Optimize for battery life", batterySaver, { viewModel.setBatterySaver(it) })
                    SettingsSwitchItem(Icons.Outlined.CheckCircle, "Auto Apply", "Automatically apply new wallpapers", autoApply, { viewModel.setAutoApply(it) })
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsSection("ACCOUNT") {
                    SettingsArrowItem(Icons.Outlined.Sync, "Google Sync", "Sync your preferences")
                    SettingsArrowItem(Icons.Outlined.CloudUpload, "Cloud Backup", "Backup your data to cloud")
                    SettingsArrowItem(Icons.Outlined.SettingsBackupRestore, "Restore Data", "Restore from backup")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsSection(title = null) { // Continued from Support
                    SettingsArrowItem(Icons.AutoMirrored.Filled.HelpOutline, "Help & Feedback", "Get help or send feedback")
                    SettingsArrowItem(Icons.Outlined.StarOutline, "Rate App", "Rate us on Google Play")
                    SettingsArrowItem(Icons.Outlined.Info, "About Nova", "App version 8.1.0")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (googleAccount != null) {
            // Sign Out Button
            Button(
                onClick = { 
                    googleSignInClient.signOut().addOnCompleteListener {
                        viewModel.setGoogleAccount(null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C56D1)), // Blue matching
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, value: String, label: String, iconTint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.LightGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun SettingsSection(title: String?, content: @Composable ColumnScope.() -> Unit) {
    Column {
        if (title != null) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
        }
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsArrowItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SettingsSwitchItem(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4285F4),
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color(0xFF333333),
                uncheckedBorderColor = Color.Transparent
            ),
            modifier = Modifier.scale(0.8f) // Scale down to match design
        )
    }
}

@Composable
fun SettingsColorItem(icon: ImageVector, title: String, subtitle: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
    }
}

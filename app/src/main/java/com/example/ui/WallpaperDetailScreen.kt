package com.example.ui

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NovaWallpaperService
import com.example.ui.components.OpenGLPreview
import org.koin.androidx.compose.koinViewModel

@Composable
fun WallpaperDetailScreen(wallpaperId: String, onBackClick: () -> Unit, viewModel: WallpaperDetailViewModel = koinViewModel()) {
    val context = LocalContext.current
    
    LaunchedEffect(wallpaperId) {
        viewModel.loadWallpaper(wallpaperId)
    }
    
    val wallpaper by viewModel.wallpaper.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        // 1. OpenGL Live Preview Background
        OpenGLPreview(modifier = Modifier.fillMaxSize())

        if (wallpaper != null) {
            val wp = wallpaper!!
            // 2. Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = wp.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        if (wp.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, 
                        contentDescription = "Favorite", 
                        tint = if (wp.isFavorite) Color.Red else Color.White
                    )
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = Color.White)
                }
            }

            // 3. Bottom Sheet / Info Card
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Surface(
                    color = Color(0xFF1E1E1E).copy(alpha = 0.95f),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Title and Badge row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = wp.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                color = Color(0xFF2C56D1).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = wp.category,
                                    color = Color(0xFF8AB4F8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Stats Row
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Left Column
                            Column(modifier = Modifier.weight(1f)) {
                                DetailItem(icon = Icons.Outlined.Image, title = "Type", subtitle = wp.type)
                                Spacer(modifier = Modifier.height(16.dp))
                                DetailItem(icon = Icons.Outlined.Layers, title = "Animation", subtitle = "Flow")
                                Spacer(modifier = Modifier.height(16.dp))
                                DetailItem(icon = Icons.Outlined.Smartphone, title = "Parallax", subtitle = "Supported")
                            }
                            
                            // Right Column
                            Column(modifier = Modifier.weight(1f)) {
                                DetailItem(icon = Icons.Outlined.BrightnessMedium, title = "AMOLED", subtitle = "Optimized")
                                Spacer(modifier = Modifier.height(16.dp))
                                DetailItem(icon = Icons.Outlined.Update, title = "Status", subtitle = "Active")
                                Spacer(modifier = Modifier.height(16.dp))
                                DetailItem(icon = Icons.Outlined.CheckCircle, title = "Quality", subtitle = "High")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Buttons Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    viewModel.applyWallpaper(wp.id)
                                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                                        putExtra(
                                            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                            ComponentName(context, NovaWallpaperService::class.java)
                                        )
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Apply Wallpaper", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            CircularIconButton(if (wp.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, onClick = { viewModel.toggleFavorite() })
                            Spacer(modifier = Modifier.width(12.dp))
                            CircularIconButton(Icons.Outlined.Info, onClick = {})
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Bottom Badges Row (Using FlowRow equivalent since BadgeItems can wrap)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BadgeItem(Icons.Outlined.ViewInAr, "2.5D")
                            BadgeItem(Icons.Outlined.WifiTethering, "Live")
                            BadgeItem(Icons.Outlined.ScreenRotation, "Gyroscope")
                            BadgeItem(Icons.Outlined.EnergySavingsLeaf, "Battery Friendly")
                            BadgeItem(Icons.Outlined.Speed, "120Hz")
                            BadgeItem(Icons.Outlined.Hd, "HD")
                        }
                    }
                }
            }
        } else {
            // Loading State
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4285F4))
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 12.sp, color = Color.Gray)
            Text(text = subtitle, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CircularIconButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .border(1.dp, Color.DarkGray, CircleShape)
            .background(Color.Transparent, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun BadgeItem(icon: ImageVector, text: String) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF8AB4F8), modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

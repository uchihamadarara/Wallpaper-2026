#!/bin/bash
set -e

echo "Cleaning..."
gradle clean

echo "Building User App (com.nova.wallpaper)..."
gradle assembleRelease

echo "Copying User APK..."
cp app/build/outputs/apk/release/app-release.apk Nova_Wallpaper/Nova_Wallpaper.apk

echo "Setting up Admin App..."
cp app/src/main/java/com/example/ui/NovaApp.kt /tmp/NovaApp.kt.backup
cp app/build.gradle.kts /tmp/build.gradle.kts.backup
cp app/src/main/res/values/strings.xml /tmp/strings.xml.backup

cat << 'INNER_EOF' > app/src/main/java/com/example/ui/NovaApp.kt
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
INNER_EOF

sed -i 's/applicationId = "com.nova.wallpaper"/applicationId = "com.nova.admin"/' app/build.gradle.kts
sed -i 's/<string name="app_name">Nova<\/string>/<string name="app_name">Nova Admin<\/string>/' app/src/main/res/values/strings.xml

echo "Cleaning before Admin build..."
gradle clean

echo "Building Admin App (com.nova.admin)..."
gradle assembleRelease

echo "Copying Admin APK..."
cp app/build/outputs/apk/release/app-release.apk Nova_Admin/Nova_Admin.apk

echo "Restoring backups..."
mv /tmp/NovaApp.kt.backup app/src/main/java/com/example/ui/NovaApp.kt
mv /tmp/build.gradle.kts.backup app/build.gradle.kts
mv /tmp/strings.xml.backup app/src/main/res/values/strings.xml

echo "Zipping..."
python3 -c "import zipfile; z = zipfile.ZipFile('Nova_Apps.zip', 'w', zipfile.ZIP_DEFLATED); z.write('Nova_Wallpaper/Nova_Wallpaper.apk', 'Nova_Wallpaper.apk'); z.write('Nova_Admin/Nova_Admin.apk', 'Nova_Admin.apk'); z.close()"

echo "Done building both APKs!"

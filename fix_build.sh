#!/bin/bash
sed -i '/storeFile = file(keystorePath)/d' app/build.gradle.kts
sed -i '/storePassword = System.getenv("STORE_PASSWORD") ?: "android"/d' app/build.gradle.kts
sed -i '/keyAlias = "upload"/d' app/build.gradle.kts
sed -i '/keyPassword = System.getenv("KEY_PASSWORD") ?: "android"/d' app/build.gradle.kts

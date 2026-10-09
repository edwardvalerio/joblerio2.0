plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

import java.io.File
import java.time.LocalDate
import java.util.Properties

// Optional release signing. Create keystore.properties next to this file:
//   storeFile=/absolute/path/to/upload-keystore.jks
//   storePassword=***
//   keyAlias=***
//   keyPassword=***
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.evmcstudios.joblerio"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.evmcstudios.joblerio"
        minSdk = 24
        targetSdk = 37

        val today = LocalDate.now()
        val versionFile = File(projectDir, ".version_counter")
        val currentCount = if (versionFile.exists()) versionFile.readText().trim().toIntOrNull() ?: 0 else 0
        val newCount = currentCount + 1
        versionFile.writeText(newCount.toString())
        versionCode = newCount
        versionName = "1.0.${today.year % 100}${today.monthValue.toString().padStart(2, '0')}${today.dayOfMonth.toString().padStart(2, '0')}.$newCount"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile", ""))
                storePassword = keystoreProperties.getProperty("storePassword", "")
                keyAlias = keystoreProperties.getProperty("keyAlias", "")
                keyPassword = keystoreProperties.getProperty("keyPassword", "")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.location)
    implementation(libs.coil.compose)
    implementation(libs.install.referrer)
    implementation(libs.firebase.config)
    implementation(libs.work.runtime)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

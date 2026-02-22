import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services) // ✅ Added Firebase Plugin
}

// Modern property loading using the layout API
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.layout.projectDirectory.file("local.properties").asFile
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.app.officegrid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.app.officegrid"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Injecting secrets from local.properties safely
        buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("SUPABASE_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""}\"")
    }

    buildTypes {
        debug {
            // ✅ Removed applicationIdSuffix = ".debug" because google-services.json 
            // only contains the 'com.app.officegrid' package name.
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Note: Signing configuration should be handled via environment variables or local keystore
            // signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Modern Kotlin configuration inside the Android block (AGP 8.x best practice)
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
            freeCompilerArgs.add("-Xopt-in=kotlin.RequiresOptIn")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes.add("META-INF/AL2.0")
            excludes.add("META-INF/LGPL2.1")
            excludes.add("META-INF/LICENSE.md")
            excludes.add("META-INF/LICENSE-notice.md")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.ui)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // Hilt Worker
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Logging
    implementation(libs.timber)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.realtime)
    implementation(libs.ktor.client.okhttp)


    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    // Paging
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Coil for Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("app.cash.turbine:turbine:1.0.0")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation("org.mockito:mockito-android:5.8.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
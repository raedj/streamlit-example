import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Android application. Built only with -Ppolyglot.android=true (needs Google Maven + Android SDK).
// Kotlin is compiled by AGP 9's built-in Kotlin support, so the Kotlin Android plugin is not applied.
plugins {
    id("com.android.application")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.polyglot.academy.android"
    // Compose 1.12 libraries require compiling against API 37; the app still targets API 35.
    compileSdk = 37
    defaultConfig {
        applicationId = "com.polyglot.academy"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Tesseract's native code is about 8 MB per ABI. 32-bit x86 exists only on old emulators, so it is
        // left out; phones use the ARM ABIs and current emulators x86_64.
        ndk { abiFilters += setOf("arm64-v8a", "armeabi-v7a", "x86_64") }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Signing: configure via environment/keystore outside the repository (docs/BUILD_AND_RELEASE.md).
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
    // Compressed native libraries keep the APK download small (they are extracted at install).
    packaging { jniLibs.useLegacyPackaging = true }
    packaging { resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "META-INF/INDEX.LIST", "META-INF/io.netty.versions.properties") }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":ui"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.sqldelight.android.driver)
    implementation(libs.tesseract4android)
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

val androidEnabled = providers.gradleProperty("polyglot.android").orNull == "true"
if (androidEnabled) {
    apply(plugin = "com.android.library")
    extra["androidNamespace"] = "com.polyglot.academy.core"
    apply(from = rootProject.file("gradle/android-library.gradle"))
}

kotlin {
    jvm("desktop") {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    if (androidEnabled) androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        val desktopMain by getting {
            dependencies { implementation(libs.ktor.client.okhttp) }
        }
        if (androidEnabled) {
            getByName("androidMain").dependencies { implementation(libs.ktor.client.okhttp) }
        }
    }
}

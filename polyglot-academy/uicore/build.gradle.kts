import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Platform-neutral presentation logic (strings, labels, design tokens). Kept free of Compose so it
// can be unit-tested on the JVM without Google Maven artifacts.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

val androidEnabled = providers.gradleProperty("polyglot.android").orNull == "true"
if (androidEnabled) {
    apply(plugin = "com.android.kotlin.multiplatform.library")
    extra["androidNamespace"] = "com.polyglot.academy.uicore"
    apply(from = rootProject.file("gradle/android-library.gradle"))
}

kotlin {
    jvm("desktop") { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    sourceSets {
        commonMain.dependencies { api(project(":core")) }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}

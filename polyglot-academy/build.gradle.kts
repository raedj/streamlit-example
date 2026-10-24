buildscript {
    // AGP is only put on the classpath when Android targets are enabled, so that the
    // JVM/desktop build works without access to Google Maven.
    if (providers.gradleProperty("polyglot.android").orNull == "true") {
        repositories {
            google()
            mavenCentral()
        }
        dependencies {
            classpath(libs.android.gradle.plugin)
        }
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.sqldelight) apply false
}

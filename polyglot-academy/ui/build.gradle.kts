import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

val androidEnabled = providers.gradleProperty("polyglot.android").orNull == "true"
if (androidEnabled) {
    apply(plugin = "com.android.kotlin.multiplatform.library")
    extra["androidNamespace"] = "com.polyglot.academy.ui"
    extra["androidResourcesEnabled"] = true
    apply(from = rootProject.file("gradle/android-library.gradle"))
}

kotlin {
    jvm("desktop") {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":data"))
            api(project(":uicore"))
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.material.icons.extended)
            api(libs.compose.ui)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("desktopMain").dependencies {
            implementation(libs.compose.desktop)
            implementation(libs.kotlinx.coroutines.swing)
        }
        if (androidEnabled) {
            getByName("androidMain").dependencies { implementation(libs.androidx.activity.compose) }
        }
        getByName("desktopTest").dependencies {
            implementation(libs.sqldelight.sqlite.driver)
            implementation(compose.desktop.currentOs)
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.junit)
        }
    }
}

tasks.withType<Test>().configureEach {
    // Screenshots are written to docs/screenshots so they can be reviewed with the documentation.
    systemProperty("screenshotDir", rootProject.file("docs/screenshots").absolutePath)
    maxHeapSize = "3g"
}

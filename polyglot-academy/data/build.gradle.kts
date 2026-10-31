import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

val androidEnabled = providers.gradleProperty("polyglot.android").orNull == "true"
if (androidEnabled) {
    apply(plugin = "com.android.kotlin.multiplatform.library")
    extra["androidNamespace"] = "com.polyglot.academy.data"
    apply(from = rootProject.file("gradle/android-library.gradle"))
}

kotlin {
    jvm("desktop") {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            api(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("desktopMain").dependencies { implementation(libs.sqldelight.sqlite.driver) }
        if (androidEnabled) {
            getByName("androidMain").dependencies { implementation(libs.sqldelight.android.driver) }
        }
    }
}

sqldelight {
    databases {
        create("PolyglotDatabase") {
            packageName.set("com.polyglot.academy.data.db")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)
        }
    }
}

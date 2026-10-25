// Opt-in partial verification of Android sources; see README.md. Not part of the product build.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
}

val androidJar = providers.gradleProperty("polyglot.androidTypecheck").get()
val root = rootProject.projectDir

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    sourceSets["main"].kotlin.srcDirs(
        "stubs/kotlin",
        root.resolve("androidApp/src/main/kotlin"),
        root.resolve("core/src/androidMain/kotlin"),
        root.resolve("data/src/androidMain/kotlin"),
        root.resolve("ui/src/androidMain/kotlin"),
    )
}
java { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }

dependencies {
    compileOnly(files(androidJar))
    implementation(project(":ui"))
    implementation(libs.ktor.client.okhttp)
    implementation(libs.sqldelight.runtime)
}

// `actual` declarations need their `expect` in the same compilation, which only exists inside the
// multiplatform modules; these two one-line files are therefore excluded here.
sourceSets["main"].kotlin.exclude("**/HttpClientFactory.android.kt", "**/PlatformBackHandler.android.kt")

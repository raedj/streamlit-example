rootProject.name = "polyglot-academy"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// The Android targets need the Android Gradle Plugin and AndroidX from Google Maven.
// Enable them with -Ppolyglot.android=true (or in gradle.properties) once Google Maven
// and an Android SDK are reachable. See docs/BUILD_AND_RELEASE.md.
val androidEnabled = providers.gradleProperty("polyglot.android").orNull == "true"

include(":core")
include(":data")
include(":ui")
include(":desktopApp")
if (androidEnabled) include(":androidApp")

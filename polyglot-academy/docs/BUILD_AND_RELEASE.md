# Build and Release

## Identity
- Application id: `com.polyglot.academy` · versionCode 1 · versionName 0.1.0
- minSdk 26 · targetSdk 35 · compileSdk 35 · JVM target 17
- Toolchain: Gradle 8.14.3 (wrapper), Kotlin 2.1.21, Compose Multiplatform 1.7.3, AGP 8.7.3 (declared, unverified)

## Prerequisites
- JDK 17+ (21 used here)
- For Android: Android SDK (platform 35, build-tools 35) and access to Google Maven.

## JVM modules (works without Google Maven)
```bash
cd polyglot-academy
./gradlew build -x :ui:desktopTest           # compile all JVM targets, run core/data/uicore tests, verify DB migration
```

## Android type-check (partial, works without Google Maven)
```bash
curl -o /tmp/android-all.jar https://repo1.maven.org/maven2/org/robolectric/android-all/15-robolectric-12650502/android-all-15-robolectric-12650502.jar
./gradlew -Ppolyglot.androidTypecheck=/tmp/android-all.jar :android-typecheck:compileKotlin
```
Compiles the Android sources against real framework classes and stubbed AndroidX signatures. No APK; see
`tools/android-typecheck/README.md` for what it does and does not prove.

## Full build (requires Google Maven)
```bash
echo "sdk.dir=/path/to/Android/sdk" > local.properties
./gradlew :ui:desktopTest                                        # Compose UI flow tests
./gradlew -Ppolyglot.android=true :androidApp:assembleDebug      # debug APK
./gradlew -Ppolyglot.android=true :androidApp:testDebugUnitTest
# Output: androidApp/build/outputs/apk/debug/androidApp-debug.apk
```
Expect some first-compile issues in `androidApp` and `androidMain` (type-checked against the framework, never built with AGP/AndroidX).

## Install
```bash
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

## Release signing (not configured)
Create a keystore outside the repository and pass it through Gradle properties or environment
variables, e.g. in `~/.gradle/gradle.properties`:
```
POLYGLOT_STORE_FILE=/secure/polyglot.jks
POLYGLOT_STORE_PASSWORD=…
POLYGLOT_KEY_ALIAS=polyglot
POLYGLOT_KEY_PASSWORD=…
```
then add a `signingConfigs.release` block reading those properties. Never commit keystores
(`*.jks`, `*.keystore` are git-ignored). Build with `./gradlew -Ppolyglot.android=true :androidApp:bundleRelease`.

## Desktop verification harness
```bash
./gradlew :desktopApp:run     # needs the Compose runtime → Google Maven (see BLOCKERS B-02)
```

## Editing interface strings
Edit `ui/strings/*.strings` (`KEY ¦ English ¦ Arabic`), then `python3 tools/gen_strings.py`.

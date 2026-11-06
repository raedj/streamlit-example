# Build and Release

## Identity
- Application id: `com.polyglot.academy` · versionCode 1 · versionName 0.1.0
- minSdk 26 · targetSdk 35 · compileSdk 37 · JVM target 17
- Toolchain: Gradle 9.8.2 (wrapper), Kotlin 2.4.21, Compose Multiplatform 1.12.2, AGP 9.5.0 (verified 2026-10-31: build, tests, debug and release APKs)

## Prerequisites
- JDK 17+ (21 used here)
- For Android: Android SDK (platform 35, build-tools 35) and access to Google Maven.
- For Android since 2026-11-01: NDK 28.2.13676358 and CMake 4.1.2 (`sdkmanager "ndk;28.2.13676358" "cmake;4.1.2"`),
  for the on-device library (whisper.cpp and llama.cpp, one `libpolyglot_ml.so`) built from `androidApp/src/main/cpp`. The native code is always
  built optimised, also in debug builds (unoptimised it is far too slow to use).

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

## Building on your own computer
1. Install JDK 17+ and the Android SDK (platform 35, build-tools 35), e.g. through Android Studio.
2. Create `polyglot-academy/local.properties`:
   - Windows: `sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk`
   - macOS: `sdk.dir=/Users/<you>/Library/Android/sdk` · Linux: `sdk.dir=/home/<you>/Android/Sdk`
3. Command line: `./gradlew -Ppolyglot.android=true :androidApp:assembleDebug` (Windows: `gradlew.bat ...`).
4. Android Studio: set `polyglot.android=true` in `gradle.properties` before syncing, otherwise the
   `androidApp` module is not included (the default `false` keeps JVM-only builds working without Google Maven).

### First-build troubleshooting
- *Inconsistent JVM-target*: every Android target is set to JVM 17; use JDK 17 or newer as the Gradle JDK.
- *SDK location not found*: `local.properties` is missing or `sdk.dir` is wrong.
- *Could not resolve com.android.tools.build:gradle*: Google Maven is not reachable from that machine.
- Compile errors in `androidApp` or `androidMain`: these sources were type-checked against Android 15
  framework classes but never compiled with AGP/AndroidX; report the error text so it can be fixed.
- A local AI server on another computer in your network needs HTTPS (cleartext is allowed only for
  `127.0.0.1`, `localhost` and the emulator host `10.0.2.2`, see `network_security_config.xml`).

## Trial build
`./gradlew -Ppolyglot.android=true -Ppolyglot.trial=true :androidApp:assembleDebug` builds the edition for the first testers (`AppEdition.ENGLISH_TRIAL`): English is the only language to learn, the language switcher and "add a language" are hidden, and onboarding asks only for the app language, level, daily time and privacy. Without the property the full app is built. A property change alone does not clear data on a device, so install trial builds on a clean profile.

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

## Google Play testing requirement
New personal developer accounts must run a closed test with at least 12 testers who stay opted in for 14 days in a row before they can apply for production access (check the current rule in Play Console). The Data safety answers are drafted in docs/PLAY_DATA_SAFETY.md and the trial plan in docs/TRIAL_PLAN_AR.md.

## Desktop verification harness
```bash
./gradlew :desktopApp:run     # needs the Compose runtime → Google Maven (see BLOCKERS B-02)
```

## Editing interface strings
Edit `ui/strings/*.strings` (`KEY ¦ English ¦ Arabic`), then `python3 tools/gen_strings.py`.

# Android type-check (partial verification without Google Maven)

Compiles the Android-specific sources (`androidApp`, every `androidMain`) against:

* the real Android 15 framework classes (`org.robolectric:android-all:15-robolectric-12650502`, Maven Central),
* the real shared modules (JVM variants of `core`, `data`, `uicore`, `ui`),
* **hand-written signature stubs** for the few AndroidX / SQLDelight-Android symbols the code uses (`stubs/`).

This catches misuse of Android framework APIs and of the shared modules. It does **not**
validate AndroidX usage beyond the stubbed signatures, resources, the manifest, or runtime
behaviour, and it produces no APK. A real build needs Google Maven (docs/BLOCKERS_REGISTER.md B-01).

```bash
curl -o /tmp/android-all.jar https://repo1.maven.org/maven2/org/robolectric/android-all/15-robolectric-12650502/android-all-15-robolectric-12650502.jar
./gradlew -Ppolyglot.androidTypecheck=/tmp/android-all.jar :android-typecheck:compileKotlin
```

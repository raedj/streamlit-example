# Blockers Register

| ID | Blocker | Effect | Attempted remedies | Next action (owner) |
|---|---|---|---|---|
| B-01 | Network policy blocks `dl.google.com` (Google Maven + SDK repository). `maven.google.com` redirects there. | No Android Gradle Plugin, AndroidX, aapt2 or Android SDK → Android app cannot be compiled, tested or packaged | Checked Maven Central, Gradle Plugin Portal, Ubuntu archive (only SDK platform 23 + build-tools 29), Robolectric android-all (framework classes only). Did **not** use third-party mirrors of Google Maven, to respect the policy | **User:** allow `dl.google.com` (and `maven.google.com`) in the environment's network settings, then run the commands in BUILD_AND_RELEASE.md |
| B-02 | Compose runtime on every CMP version needs `androidx.collection` / `androidx.lifecycle` (Google Maven only) | UI cannot run on the desktop harness → no UI tests, no screenshots, no visual verification | Tried CMP 1.6.x/1.7.x/1.9.x metadata; all depend on androidx at runtime. Moved strings/labels/routes/colour tokens to a compose-free module so they are tested | Same as B-01, then run `:ui:desktopTest` and add screenshot tests |
| B-03 | No KVM / emulator in the container | No instrumented tests, no TalkBack, no on-device speech/TTS verification | — | Run on a physical device or a CI emulator |
| B-04 | No AI provider API keys; provider hosts not in allow-list | Adapters verified only with mocked HTTP | MockEngine tests of request/response formats and error classification | Learner/QA supplies keys; run connection tests in-app |
| B-05 | Maven Central through the proxy returns spurious 404/429 | Flaky dependency resolution | Local retrying cache proxy + Gradle init script **outside the repository** (`~/.gradle/init.d`) | None for the repo; environment-only |
| B-06 | No release signing keystore | Release APK cannot be signed | — | Owner creates a keystore and configures signing via environment (BUILD_AND_RELEASE.md) |

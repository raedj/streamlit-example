# Polyglot Academy

An original AI-assisted language-learning app for Android: English, Mandarin Chinese, Spanish,
French and Arabic, with an Arabic (RTL) and English interface. Built with Kotlin Multiplatform
and Compose Multiplatform, so the learning engine, data layer and UI are shared between the
Android app and a desktop verification harness.

> **Status (honest summary).** The learning engine, AI provider layer, data layer and
> presentation logic are implemented and covered by **174 passing automated tests**. The Compose
> UI (≈40 screens) **compiles**. The Android sources are **type-checked against the Android 15
> framework** but have **not been built** with the Android Gradle Plugin, and the UI **has not been run or screenshotted**: the build environment used so
> far blocks Google Maven (`dl.google.com`), which hosts the Android Gradle Plugin, AndroidX and
> parts of the Compose runtime. See [`docs/BLOCKERS_REGISTER.md`](docs/BLOCKERS_REGISTER.md)
> and [`docs/PROJECT_STATE.json`](docs/PROJECT_STATE.json).

## Modules

| Module | What it contains | Verified here |
|---|---|---|
| `core` | Domain model, curricula (5 languages), spaced repetition, adaptive engine, placement test, planner, analytics, AI providers + privacy-aware routing, tutors, offline scripted tutor, speech state machine, Mandarin pinyin tools, media/text extraction, application services | Compiled, 127 unit tests |
| `data` | SQLDelight schema (v2 with a tested v1→v2 migration), repositories, export, restore, deletion | Compiled, 35 tests (incl. service integration tests) |
| `uicore` | Localized strings (983 keys, EN/AR, CLDR plurals), enum labels, routes, colour tokens + WCAG contrast | Compiled, 12 tests |
| `ui` | Compose design system, components, illustrations, charts, navigation, all screens | Compiled; UI tests written, **not run** |
| `desktopApp` | JVM verification harness (not a product) | Compiled; **not run** |
| `androidApp` | Android activity, Keystore secrets, speech/TTS adapters, reminders, file import | Type-checked against Android 15 framework + stubs; **not built** (needs Google Maven) |

## Build

See [`docs/BUILD_AND_RELEASE.md`](docs/BUILD_AND_RELEASE.md). In short:

```bash
# JVM modules + tests (works without Google Maven)
./gradlew build -x :ui:desktopTest

# Everything, including the Android app (needs Google Maven and an Android SDK)
./gradlew -Ppolyglot.android=true :androidApp:assembleDebug :ui:desktopTest
```

## Documentation

All project documents are in [`docs/`](docs): requirements, architecture, design system,
screen specifications, tutor personas, AI provider matrix, privacy and security, database
schema, test strategy and results, traceability, known issues, blockers and release readiness.

# Test Strategy

| Level | Where | Tooling | Runs without Google Maven? |
|---|---|---|---|
| Unit (domain, AI adapters with Ktor MockEngine, curriculum integrity) | `core/src/commonTest` | kotlin.test, kotlinx-coroutines-test | Yes |
| Integration (SQLite repositories, migration, application services with fake speech/AI) | `data/src/desktopTest` | SQLDelight JDBC driver | Yes |
| Presentation logic (strings, plurals, labels, routes, WCAG contrast) | `uicore/src/commonTest` | kotlin.test | Yes |
| UI flows (onboarding, lesson, mistake, offline conversation, consent, Arabic, empty states, provider status, dark theme) | `ui/src/desktopTest/UiFlowTest.kt` | Compose desktop UI test | **No** (Compose runtime needs androidx.collection) |
| Android instrumented / TalkBack / screenshots on devices | not yet written | Espresso/Compose test, Accessibility Scanner | No |

Principles: fakes over mocks; deterministic clocks (`FixedTimeSource`) and id generators; no
network in tests; every bug fix gets a regression test (e.g. conversation cascade deletion,
spoken-turn drop, case-only corrections, pinyin "èr", contrast).

Commands:
```bash
./gradlew :core:desktopTest :data:desktopTest :uicore:desktopTest   # here
./gradlew :ui:desktopTest                                         # needs Google Maven
./gradlew -Ppolyglot.android=true :androidApp:testDebugUnitTest    # needs Google Maven + SDK
```

Defect severity follows the master specification (critical/high/medium/low). Release is
blocked while any critical or high defect is open.

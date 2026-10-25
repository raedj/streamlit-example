# Test Results

Run on 2026-10-25 in the cloud build container (JDK 21, Gradle 8.14.3, Linux x64) with
`./gradlew build -x :ui:desktopTest`. **Result: BUILD SUCCESSFUL, 151 tests, 0 failures.**

| Module | Test class | Tests | Failures |
|---|---|---|---|
| core | AdapterTest (OpenAI, Azure, Gemini, Anthropic, DeepSeek, local; error classification; timeouts; no key leakage) | 10 | 0 |
| core | AiTest (privacy policy, router, retries, fallback rules, redaction) | 18 | 0 |
| core | AssessmentPlannerAnalyticsTest | 13 | 0 |
| core | CurriculumIntegrityTest (all 5 curricula, incl. track integrity) | 7 | 0 |
| core | LearningEngineTest | 13 | 0 |
| core | MiscCoreTest (pinyin, captions, extraction, import policy, breathing, device/local models) | 12 | 0 |
| core | PdfTextTest (reportlab and LibreOffice fixtures in Latin, Chinese and Arabic; object streams; CID fonts; Differences; form XObjects; encrypted/scanned/non-PDF; truncated files) | 7 | 0 |
| core | SchedulerTest | 9 | 0 |
| core | TextAndAnswersTest | 9 | 0 |
| core | TutorAndVoiceTest | 10 | 0 |
| data | SqlStoreTest (incl. real v1→v2 migration) | 9 | 0 |
| data | ServicesIntegrationTest (lesson, review, plan, assessment, providers, offline/AI conversation, privacy, writing, materials, delete-all, restore re-applies reminder, PDF import) | 17 | 0 |
| data | ConversationIntegrityTest | 1 | 0 |
| data | RestoreTest (v2 round trip of every record type, cloud AI forced off, invalid/newer files rejected without changes, v1 best-effort) | 4 | 0 |
| uicore | LocalizationTest | 8 | 0 |
| uicore | ContrastTest | 4 | 0 |
| **Total** | | **151** | **0** |

Also executed: SQLDelight `verifyCommonMainPolyglotDatabaseMigration` (pass); compilation of
`ui` and `desktopApp` for the JVM (pass, no warnings after fixes).

**Android type-check** (`./gradlew -Ppolyglot.androidTypecheck=<android-all.jar> :android-typecheck:compileKotlin`,
see `tools/android-typecheck/README.md`): `androidApp` and all `androidMain` sources compile against the
real Android 15 framework classes plus hand-written AndroidX signature stubs. **Pass, no warnings.**
This is not an Android build: resources, manifest, AndroidX behaviour and runtime are unverified.

## Not executed
- `ui` desktop UI tests (10 tests written): dependency resolution fails — `androidx.collection:collection:1.4.0`, `androidx.annotation:annotation:1.8.0`, `androidx.lifecycle:lifecycle-{common,runtime,viewmodel}:2.8.5`, `androidx.arch.core:core-common:2.2.0` are only on Google Maven.
- Android build, unit/instrumented tests, APK assembly: AGP unavailable.
- Visual verification (screenshots, emulator, TalkBack): not possible — see BLOCKERS B-02.
- Live AI provider calls: no API keys; provider hosts not reachable from this environment.

## Defects found by tests and fixed
| ID | Severity | Defect | Fix / regression test |
|---|---|---|---|
| D-01 | Critical (data loss) | Ending a conversation deleted all its turns (`INSERT OR REPLACE` + `ON DELETE CASCADE`) | Insert-or-ignore + update; `ConversationIntegrityTest` |
| D-02 | High | Spoken reply ignored: recognizer set state to Processing, then `sendText` rejected it as busy | Spoken path bypasses busy check; `offlineConversationWithVoiceAndSummary` |
| D-03 | Medium | Case-only corrections (capitalisation) dropped from AI feedback | Exact-string comparison; `writingFeedbackOfflineAndAi` |
| D-04 | Medium | Pinyin "èr" split into two syllables | Erhua rule fix; Mandarin vocabulary test |
| D-05 | Low | Vocabulary candidates tie-broken alphabetically, hiding multi-character words | Prefer longer terms on ties |
| D-06 | High (crash) | `getSystemService(ActivityManager)` dereferenced without null check (device info) | Safe call; found by Android type-check |
| D-07 | High (crash) | `NotificationManager` dereferenced without null check (reminder channel) | Safe call; found by Android type-check |
| D-08 | Medium (crash) | Nullable `AlarmManager` used directly when scheduling reminders | Early return; found by Android type-check |
| D-06 | Medium (a11y) | Light success colour 4.36:1 on its container | Darkened to #116A31; `ContrastTest` |
| D-07 | Low | Home showed "self-selected" for test-based levels (flag inverted) | Flag semantics fixed |
| D-08 | Medium | Lesson player could lose its Continue button after process death | Answer state no longer saveable on its own |

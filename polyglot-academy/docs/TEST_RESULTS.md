# Test Results

Run on 2026-10-25 in the cloud build container (JDK 21, Gradle 8.14.3, Linux x64) with
`./gradlew build -x :ui:desktopTest`. **Result: BUILD SUCCESSFUL, 217 tests, 0 failures.**

Re-run on 2026-10-25 on Windows 11 (JDK 17, Gradle 8.14.3) after adding Turkish, Urdu and Malay:
**BUILD SUCCESSFUL, 217 tests, 0 failures** (core 161, data 44, uicore 12). The curriculum,
story and tutor tests iterate over every language, so they now cover all eight curricula.

| Module | Test class | Tests | Failures |
|---|---|---|---|
| core | AdapterTest (OpenAI, Azure, Gemini, Anthropic, DeepSeek, local; image parts per vendor; error classification; timeouts; no key leakage; Mistral, Groq, xAI, OpenRouter, Qwen and custom OpenAI-compatible endpoints; GPT-5/o-series parameters; model lists from OpenAI-compatible, Anthropic and Gemini) | 14 | 0 |
| core | AiTest (privacy policy, router, retries, fallback rules, redaction, custom provider needs a model, catalog completeness) | 19 | 0 |
| core | AssessmentPlannerAnalyticsTest | 13 | 0 |
| core | CurriculumIntegrityTest (all 8 curricula, incl. track integrity and story glossaries) | 8 | 0 |
| core | LearningEngineTest | 13 | 0 |
| core | MiscCoreTest (pinyin, captions, extraction, import policy, breathing, device/local models) | 12 | 0 |
| core | PdfTextTest (reportlab and LibreOffice fixtures in Latin, Chinese and Arabic; object streams; CID fonts; Differences; form XObjects; encrypted/scanned/non-PDF; truncated files; Arabic with numbers, Latin and brackets; mixed-direction lines) | 9 | 0 |
| core | HanziTest (character data coverage and spot checks; scorer on synthetic shapes; stroke-count hints; alpha masks) | 5 | 0 |
| core | HandwritingFontTest (scoring against glyphs rendered by Noto Sans CJK SC) | 1 | 0 |
| core | SpeakingPathTest (mission ladder A1–C1, prompt and completion flag, word counts, pass criteria, fluency report, progress) | 6 | 0 |
| core | VisemesTest (mouth shapes from spelling; TTS offsets to words) | 2 | 0 |
| core | FreeTalkTest (free-topic prompt, correction instruction, topic sanitising) | 2 | 0 |
| core | CoachTest (coach protocol in the prompt, track question and exam names, warm-up/error log/frustration, four-part correction parsing, frustration detection, error-log grouping, warm-up item choice, ordered titles) | 8 | 0 |
| core | PinyinPunctuationTest | 1 | 0 |
| core | DailyLoopTest (XP from stored activity, stable daily quests, streak freezes used only when enough, earned every 7 days up to 2, levels) | 5 | 0 |
| core | StoriesTest (graded stories at A1/A2/B1 in all languages; phrase tapping; French elision, Arabic prefixes, Mandarin segmentation; AI story validation) | 4 | 0 |
| core | VoiceServicesTest (WAV encoding; Azure detailed response down to phonemes; request headers and reference text via mock HTTP; region/locale rules; OpenAI Realtime event parsing, current and beta names) | 5 | 0 |
| core | AvatarMotionTest (expression from the last learner turn; subtle motion; loudness-driven mouth) | 2 | 0 |
| core | SyncTest (AES-GCM envelope round trip; wrong passphrase and tampering rejected; fingerprint; HTTPS/LAN rule; WebDAV GET/MKCOL/PUT, If-Match conflicts, 401 via mock HTTP) | 4 | 0 |
| core | SchedulerTest | 9 | 0 |
| core | TextAndAnswersTest | 9 | 0 |
| core | TutorAndVoiceTest | 10 | 0 |
| data | SqlStoreTest (incl. real v1→v2 and v2→v3 migrations) | 10 | 0 |
| data | ServicesIntegrationTest (lesson, review, plan, assessment, providers, offline/AI conversation, privacy, writing, materials, delete-all, restore re-applies reminder, PDF import, AI and offline speaking missions, vision OCR with privacy gate, chosen-topic AI conversation with corrections, free talk offline notice, coach asks for and saves the track, error log, warm-up once per session, daily loop stores a streak freeze, AI story stored/backed up and words saved, pronunciation scoring gated by key and voice consent, live voice call with fake WebSocket/microphone/speaker and transcript corrections, two-device encrypted sync incl. conflict, a different provider per feature) | 29 | 0 |
| data | ConversationIntegrityTest | 1 | 0 |
| data | RestoreTest (v2 round trip of every record type, cloud AI forced off, invalid/newer files rejected without changes, v1 best-effort) | 4 | 0 |
| uicore | LocalizationTest | 8 | 0 |
| uicore | ContrastTest | 4 | 0 |
| **Total** | | **217** | **0** |

Also executed: SQLDelight `verifyCommonMainPolyglotDatabaseMigration` (pass); compilation of
`ui` and `desktopApp` for the JVM (pass, no warnings after fixes).

**Android type-check** (`./gradlew -Ppolyglot.androidTypecheck=<android-all.jar> :android-typecheck:compileKotlin`,
see `tools/android-typecheck/README.md`): `androidApp` and all `androidMain` sources compile against the
real Android 15 framework classes plus hand-written AndroidX signature stubs. **Pass, no warnings.**
This is not an Android build: resources, manifest, AndroidX behaviour and runtime are unverified.

## Android build and emulator test (2026-10-25)
- `./gradlew -Ppolyglot.android=true :androidApp:assembleDebug` on Windows 11 (JDK 17, Android SDK 35): **BUILD SUCCESSFUL**, no compile errors. APK about 20 MB, debug-signed, minSdk 26, targetSdk 35, ABIs arm64-v8a/armeabi-v7a/x86/x86_64.
- Installed on an Android 15 emulator (x86_64, AOSP image without Google services or a TTS engine) and driven by hand through `adb`:
  - Arabic interface: welcome screen, 12-step onboarding (incl. the notification permission request), home, a complete 18-exercise lesson (flashcards, matching, multiple choice, listening, speaking self-check, fill-in, translation), lesson summary, exit and resume, all five tabs, offline tutor conversation. No crash, no ANR from the app.
  - Turkish, Urdu and Malay: onboarding with each, home, lesson overview and flashcards. Urdu renders right-to-left with transliteration; Turkish and Malay left-to-right inside the Arabic interface. No crash.
- Bugs found on the device and fixed (re-tested on the emulator): D-10..D-13 below.
- Not verified on the device: English interface in depth, TalkBack, real TTS voices and speech recognition (the image has none), reminders firing, live AI/Azure/Realtime/WebDAV with real keys, physical phones.

## Not executed
- `ui` desktop UI tests (10 tests written): not run in this pass.
- Android unit/instrumented tests: not run.
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
| D-09 | Low | Pinyin splitter counted punctuation (?, !) as syllables | Letters only; `PinyinPunctuationTest` |
| D-06 | Medium (a11y) | Light success colour 4.36:1 on its container | Darkened to #116A31; `ContrastTest` |
| D-07 | Low | Home showed "self-selected" for test-based levels (flag inverted) | Flag semantics fixed |
| D-08 | Medium | Lesson player could lose its Continue button after process death | Answer state no longer saveable on its own |
| D-10 | Medium | "Practice again" on the lesson summary did nothing (same route, so the player kept its finished state) | Player keyed by attempt; re-tested twice in a row on the emulator |
| D-11 | Medium | English sentences in the Arabic interface showed their full stop at the start (paragraph direction followed the interface) | `LearningText` and the answer field use the learned language's direction; checked on the emulator |
| D-12 | Low | Arabic strings: "5 دقيقة" and "لـالإنجليزية / بـالإنجليزية" | Plural entry and rephrased strings; checked on the emulator |
| D-13 | Low | Course list showed "best 0%" for a lesson that was only started | Shows the resume point until completed; checked on the emulator |

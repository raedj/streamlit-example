# Test Results

Run on 2026-10-25 in the cloud build container (JDK 21, Gradle 8.14.3, Linux x64) with
`./gradlew build -x :ui:desktopTest`. **Result: BUILD SUCCESSFUL, 217 tests, 0 failures.**

Re-run on 2026-10-25 on Windows 11 (JDK 17, Gradle 8.14.3) after adding Turkish, Urdu and Malay:
**BUILD SUCCESSFUL, 217 tests, 0 failures** (core 161, data 44, uicore 12). The curriculum,
story and tutor tests iterate over every language, so they now cover all eight curricula.

After adding the word-list programme (English level 1): **BUILD SUCCESSFUL, 227 tests, 0
failures** (core 171 incl. 10 in `WordListTest`, data 44, uicore 12); `:androidApp:assembleDebug`
succeeds. `WordListTest` was also shown to reject words outside the list on the real data, and
After Spanish level 1, French level 1, Mandarin level 1, Arabic level 1, Turkish level 1, Urdu level 1 and again after Malay level 1 the same build passes (227 tests, 0 failures); for Malay the matcher gained plain me- (memasak), the be- form of ber- (bekerja) and the -ku/-mu clitics; a mirroring script found no unknown word in the 600 examples (one, "biru", was found and the example rewritten), and the 42 forms accepted through affix rules were checked by hand; for Urdu the matcher gained gender/number endings and verb stems from the infinitive (before, Urdu had no inflection rules at all); a mirroring script found no unknown word in the 600 examples, and the 197 forms accepted only through rules were listed and checked by hand (one, گئیں, had matched the wrong word and was added as a form of جانا); for Turkish the matcher was made stricter: a word now counts as a list word only if it is a list stem followed by a complete chain of known suffixes (before, any word starting with a list word of two letters counted, e.g. "bulmak" via "bu"); a mirroring script found no unknown word in the 600 examples, and the 512 forms accepted only through suffix rules were listed and checked by hand; for Arabic, a separate script mirroring the matcher found no unknown word in the 600 examples, and every word accepted only through an inflection rule (433 distinct forms) was listed and checked by hand; the Arabic matcher gained present-tense prefixes (أ/ت/ن → يـ), stacked endings, the feminine ـة and broken plurals from the plural field, covered by a test; for Mandarin, a separate script also confirmed that every Han character of all 600 examples segments into list words (the test alone allows up to 15% unknown words); writing it fixed name removal in unspaced Chinese text (now covered by a test); for French, every example word accepted only through an inflection rule (not an exact term or form) was listed and checked by hand — all were real plurals or feminine forms; writing it exposed and fixed a matcher bug (a proper name was also removed from inside longer words, e.g. "Ana" in "hermana"), now covered by a test. `tools/gen_wordlists.py` was run on a deliberately broken copy (wrong rank, invalid part of
speech, missing example) and refused to generate, naming each line.

Level 2 (ranks 301–600) was then added for all eight languages, one commit each. After each one
the same build passed (227 tests, 0 failures: core 171, data 44, uicore 12). `tools/scan_wordlist.py`
mirrors the app's word matcher for all eight languages. For every language it reports
0 unknown tokens, 0 tokens beyond rank+300 and 0 examples missing their own word, for both level 1
and level 2. Each language's forms that were accepted only through inflection or affix rules were
listed and checked by hand. Level 2 counts:

| Language | Accepted via rules | Links to curriculum vocab |
|---|---|---|
| en | 119 | 23 |
| es | 112 | 26 |
| fr | 72 | 24 |
| zh | 0 (segmentation only) | 10 |
| ar | 628 | 14 |
| tr | 763 | 23 |
| ur | 266 | 27 |
| ms | 70 | 33 |

To keep level 1 clean, one Mandarin level-1 example (rank 57) had to be rewritten. The new
level-2 word 早饭 segmented as a single word, and it was more than 300 ranks beyond that example.
These are checks on the word lists, not language review: no native speaker has reviewed levels 1–2.

Level 3 (ranks 601–900) was then added for all eight languages, one commit each. After each one
the same build passed (227 tests, 0 failures: core 171, data 44, uicore 12). `tools/scan_wordlist.py`
reports 0 unknown tokens, 0 tokens beyond rank+300 and 0 examples missing their own word for every
language at levels 1, 2 and 3 (levels 1–2 were rescanned after level 3 was added). Forms accepted
only through rules were listed and checked by hand; where a form had matched the wrong word
(e.g. Arabic كلامك, Turkish doğayı/kara/gecikti, Malay masakan/memulangkan) the example was
rewritten. Level 3 counts:

| Language | Accepted via rules | Links to curriculum vocab |
|---|---|---|
| en | 150 | 33 |
| es | 129 | 20 |
| fr | 88 | 24 |
| zh | 0 (segmentation only) | 24 |
| ar | 753 | 28 |
| tr | 844 | 30 |
| ur | 266 | 23 |
| ms | 145 | 22 |

Two notes from level 3. The Spanish level-3 commit first went out without its generated Kotlin:
one id contained "ñ", the generator refused it, and the helper script did not stop. A follow-up
commit fixed the id and added the code, and the script now stops on any generator problem. In
Mandarin, the new words 搬家, 过去, 钱包 and 最近 would have re-segmented five level-2 examples,
so those examples were reworded. These are checks on the word lists, not language review: no
native speaker has reviewed levels 1–3.

Levels 4–10 (ranks 901–3000) were then added for all eight languages, one commit per language
per level (56 commits, e.g. level 10: en bcf8874, es 437beee, fr deeea86, zh a9a3480, ar 7d58ba5,
tr 22c16d0, ur e7e3835, ms 0d23579). Before each commit `./gradlew build -x :ui:desktopTest`
passed with 227 tests and 0 failures (core 171, data 44, uicore 12). Each language now has 3000
entries and 3900 examples. A final rescan of every language (all ten levels together) with
`tools/scan_wordlist.py` reports:

| Language | Unknown tokens | Beyond rank+300 | Examples without their word | Accepted via rules (all levels) |
|---|---|---|---|---|
| en | 0 | 0 | 0 | 1049 |
| es | 0 | 0 | 0 | 438 |
| fr | 0 | 0 | 0 | 358 |
| zh | 0 | 0 | 0 | 0 (segmentation only) |
| ar | 0 | 0 | 0 | 2952 |
| tr | 0 | 0 | 0 | 3983 |
| ur | 0 | 0 | 0 | 797 |
| ms | 0 | 0 | 0 | 709 |

Limits of these checks for levels 4–10. Unlike levels 1–3, the forms accepted only through
inflection or affix rules were **not** all listed and checked by hand, so a form may match the
wrong list word without being noticed. Where the matcher could not see a valid form, the example
was reworded instead of changing the matcher. For example, Urdu compound verbs (… کرنا) match only
present, imperative and infinitive forms, and Turkish, Arabic and Malay sometimes needed simpler
inflections. In Mandarin, some new words re-segmented older examples, and those examples were
reworded (noted in the commits). Examples have 4–12 words (Mandarin 4–24 characters) and one
example per word. These are checks on the data only: no native speaker has reviewed levels 4–10,
ranks are approximate, and Mandarin HSK levels were not checked against an official syllabus.

**Word lists in the app (2026-10-30).** The lists are now used in the app: Practice → "3000 most
common words" shows the ten levels, each level's words (searchable), a page per word (pronunciation,
part of speech, grammar fields, forms, examples with audio), adding words to the review queue ten at
a time, and a level test (20 multiple-choice questions, pass at 80%, missed words go into reviews).
Review cards and "My vocabulary" now also find word-list words. New tests:
`WordLevelServiceTest` (data, 4 tests: rank-order adding and review cards, quiz options in all eight
languages, best score and missed words, test references), plus route round-trips and part-of-speech
labels in `LocalizationTest`. `./gradlew build -x :ui:desktopTest`: 231 tests, 0 failures (core 171,
data 48, uicore 12). The debug APK was built (from a copy of the repository outside OneDrive, because
OneDrive locked build files) and run on the Android 15 emulator in the Arabic interface with English
as the learning language: levels screen, level 1, adding 10 words, a word page, a full level test
with its result, and the review queue showing word-list cards were checked by hand. Not checked: the
English interface, other learning languages on the device, TalkBack, and large font sizes.

**Small fixes K-24, K-25, K-26 (2026-10-30).** New `LessonFixesTest` (data, 5 tests): a resumed lesson
keeps the results from before the interruption, attempts from an earlier completed run are ignored,
the first-try score matches the player, the home card shows the next lesson's own minutes, and only
audio-only exercises need a voice. `./gradlew build -x :ui:desktopTest`: 236 tests, 0 failures (core
171, data 53, uicore 12). The APK was rebuilt and started on the emulator (home screen, no crash). Not
checked on a device: the resumed-lesson score. (An earlier version of this note said the emulator has
an English voice; that was wrong, it has none, which made the K-24 check below possible.)

**Word tests and home card (2026-10-30).** Level tests now offer word to meaning, meaning to word,
listening and mixed questions (listening only when the device has a voice), and home shows the first
level not yet passed. Word lists are now parsed off the main thread: the first build of the home card
did it on the main thread and the emulator showed "isn't responding"; after the change and a separate
loader for the card, home appeared after about 10 s on this (slow) emulator with no ANR. New tests in
`WordLevelServiceTest`: modes cycle and options have distinct words and meanings in all eight
languages; the current level is the first not passed. `./gradlew build -x :ui:desktopTest`: 238
tests, 0 failures (core 171, data 55, uicore 12). Checked by hand on the emulator (Arabic interface,
English, no English voice installed): the home card, the test type choice with listening hidden and
its notice, a meaning-to-word question with feedback, the K-24 warning and Skip button in Listening
practice and inside the Greetings lesson (question 13 of 18; Skip moved to 14).

**Generated word-list lessons (2026-10-30).** `WordLessons` builds 30 lessons of 10 words per level
(2400 in all). New `WordLessonsTest` (core, 3 tests) builds every one of the 2400 lessons and checks
ids, unique exercise ids, at least 18 exercises, four distinct options (also in Arabic) with a valid
answer, flashcards that resolve, 4-8 tokens for word order, and that generation is deterministic.
`WordLevelServiceTest` gained a test that completing a lesson puts its 10 words into reviews and
moves to the next lesson. Build and tests were run in a copy of the repository outside OneDrive
(OneDrive locked `ui/build`): 242 tests, 0 failures (core 174, data 56, uicore 12). On the emulator
(Arabic interface, English, no English voice) lesson 1 of level 1 was played to the end by a script
plus one manual word-order answer: flashcards, matching, both choice directions, word order, the
three audio exercises skipped with the K-24 warning, summary "15 of 20 correct" (23 exercises minus
the 3 skipped), and a "Next lesson: words 11-20" button. Home also showed "Continue: Greetings · 6
minutes", confirming K-25 on the device.

**Release build, English interface, small fixes (2026-10-30).** `:androidApp:assembleRelease` (R8
minify and resource shrinking on) failed in `lintVitalRelease`: the backup rules listed `<exclude>`
paths outside the only `<include>` (files/backup/). With an `<include>` present nothing else is backed
up, so the excludes were redundant; they were removed and the comments explain why keys and the
database still never leave the device. The release APK then built (5.1 MB unsigned; R8 took about
12 minutes on this machine). Signed with the local debug key for testing only, it ran on the emulator
with no crash or ANR: home in about 9 s, word levels and a level, and the interrupted Greetings lesson
resumed at exercise 14 and finished with "12 of 16 correct" (18 exercises minus 2 skipped audio ones),
which also confirms K-26 on a device. In the English interface the settings, Me, Practice, word
levels, a level and a generated lesson were checked; Arabic instructions (explanations in Arabic)
wrapped at a third of the width in the LTR layout and now take the full width with their own
direction. Five Arabic strings used the ASCII "%" and showed "%5"; they now use "٪" inside a
left-to-right isolate like the rest of the app. Build: 242 tests, 0 failures. Not checked: TalkBack,
large fonts, other learning languages on the device. One ANR was seen on an idle summary screen while
R8 was using the whole machine; it did not recur afterwards.

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

# Architecture

```
androidApp ──► ui ──► uicore ──► core
                 └──► data ───────┘
desktopApp ──► ui          (JVM verification harness)
```

| Layer | Module / package | Responsibility |
|---|---|---|
| Presentation | `ui` (`designsystem`, `feature/*`, `app`, `navigation`) | Compose screens, components, theming, navigation back stack |
| Presentation logic | `uicore` (`i18n`, `navigation/Routes`, `designsystem/PaletteTokens`) | Strings, plural rules, enum labels, route encoding, colour tokens — no Compose |
| Application services | `core/service` | Use-case orchestration: study recording, reviews, dashboard, plans, assessment, providers, conversation controller, writing, materials, achievements |
| Domain | `core/model`, `srs`, `learning`, `assessment`, `planner`, `analytics`, `tutor`, `speech`, `mandarin`, `media`, `relax`, `device`, `localai` | Pure Kotlin rules, fully unit-testable |
| Content | `core/content` | Built-in curricula (original content) + integrity validation |
| AI | `core/ai` | Provider interface, HTTP adapters (Ktor), privacy policy, router, retry/fallback service |
| Data | `data` | SQLDelight schema and the `LearningStore` implementation |
| Platform | `androidApp`, `ui/platform` (desktop) | Speech, TTS, Keystore secrets, connectivity, reminders, file picker |

## Key flows
- **Answering an exercise:** exercise view → `Answer` → `StudyService.recordExercise` (attempt + de-duplicated mistake) → `DifficultyAdjuster` adapts hints/explanations → lesson completion updates `LessonProgress`, introduces vocabulary into the review queue, evaluates achievements.
- **Conversation:** `ConversationController.start()` asks `ProviderService.route()` which provider may serve CONVERSATION_TEXT. If none, the `OfflineTutorEngine` drives the scenario. Each learner turn is persisted, sent (AI) or matched (offline); AI failure switches to offline with a notice and never to another cloud provider unless allowed.
- **Voice:** recognizer flow events → `VoiceStateMachine.reduce` → UI. Leaving the screen cancels the flow, which stops the microphone.
- **Privacy:** every `ChatRequest` declares `dataCategories`; `ProviderRouter` evaluates `PrivacyPolicy` per candidate before any network call.

## State
UI state is explicit: `Load.Loading / Ready / Failed` for screen data, `VoiceState` for speech,
`ConversationState` for conversations. Profile and settings are observed from the database so
language/theme/accessibility changes apply app-wide immediately. The navigation back stack is
saved with `rememberSaveable` (routes encode to strings) to survive process death.

## Threading
All database work runs on `Dispatchers.IO` inside `SqlStore`; network calls run inside
`withTimeout`; Android speech APIs run on the main thread via `flowOn(Dispatchers.Main)`.

## Word lists
The 3000-word programme (10 levels of 300 lemmas per language) keeps its data as text, not code:
`content/wordlists/<lang>.words.tsv` and `<lang>.examples.tsv` (format in
`content/wordlists/README.md`). `tools/gen_wordlists.py` validates the files and generates one
Kotlin object per language and level (`core/.../wordlist/generated/<Lang>LevelNN.kt`, at most 300
entries each, rows kept as tab-separated string literals) plus an index. This keeps every
generated method far below the JVM's 64 KB method limit and lets each level load on its own.

At run time `WordLists.of(language)` parses a language's levels on first use only (a `lazy` per
language), so start-up cost does not depend on the number of lists. `WordMatcher` finds list
words in sentences per language (case and script normalisation, French elision, Arabic clitics,
Turkish suffixes by stem, Malay affixes, Chinese longest-match segmentation); the same matcher
backs the validation tests and is meant for building exercises (for example, blanking the word in
its example). `WordEntry.toVocabularyItem()` maps an entry to the existing vocabulary model so
reviews use the normal spaced-repetition queue; entries linked to a curriculum word (`vocab_id`)
keep that word's id, so existing review state is not lost.

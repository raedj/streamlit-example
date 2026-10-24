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

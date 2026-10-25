# Requirements Traceability

Status key: **V** implemented and verified by automated tests · **I** implemented, compiled, not verified at runtime · **P** partial · **B** blocked · **N** not implemented.

| Requirement | Implementation | Tests | Status |
|---|---|---|---|
| SR-LE-01 Spaced repetition | `srs/SpacedRepetitionScheduler` | SchedulerTest, ServicesIntegrationTest.reviewQueueAndRating | V |
| SR-LE-02 Mastery | `learning/MasteryTracker` | LearningEngineTest | V |
| SR-LE-03 Difficulty | `learning/DifficultyAdjuster`, LessonPlayer | LearningEngineTest | V (engine) / I (UI use) |
| SR-LE-04 Sequencing | `learning/LessonSequencer` | LearningEngineTest, ServicesIntegrationTest | V |
| SR-LE-05 Recommendation | `NextActivityRecommender`, `DashboardService` | LearningEngineTest, ServicesIntegrationTest | V |
| SR-LE-06 Placement | `assessment/DiagnosticSession`, `AssessmentService` | AssessmentPlannerAnalyticsTest, ServicesIntegrationTest | V |
| SR-LE-07 Answer checking | `AnswerChecker`, `TextNormalizer` | TextAndAnswersTest, CurriculumIntegrityTest | V |
| SR-PL-01 Daily plan | `StudyPlanner`, `PlanService` | AssessmentPlannerAnalyticsTest, ServicesIntegrationTest | V |
| SR-AN-01/02 Analytics, active-time cap | `AnalyticsCalculator`, `StudyService` | AssessmentPlannerAnalyticsTest, ServicesIntegrationTest | V |
| SR-AI-01 Adapters | `ai/Adapters.kt` | AdapterTest (mocked HTTP) | V (mocked) / B (live) |
| SR-AI-02/03 Errors, retries | `HttpSupport`, `AiService` | AdapterTest, AiTest | V |
| SR-AI-04 Verified connection | `ProviderService.testConnection` | ServicesIntegrationTest | V |
| SR-PV-01..04 Privacy | `PrivacyPolicy`, `ProviderRouter`, Privacy settings UI | AiTest, ServicesIntegrationTest | V (logic) / I (UI) |
| SR-SP-01 Voice states | `VoiceStateMachine` | TutorAndVoiceTest | V |
| SR-SP-02/03 Recording stops, unavailable speech | `ConversationController`, `AndroidSpeechRecognizer` | ServicesIntegrationTest (fake recognizer) | V (logic) / B (Android) |
| SR-DB-01..04 Data | `SqlStore`, migrations | SqlStoreTest, ConversationIntegrityTest | V |
| SR-L10N-01 Strings | `ui/strings`, `uicore/i18n` | LocalizationTest | V |
| SR-L10N-02 RTL, locale hints | `PolyglotTheme`, `LearningText` | UiFlowTest.arabicInterfaceIsLocalized (not run) | I |
| SR-A11Y-01 Contrast | `PaletteTokens` | ContrastTest | V |
| SR-A11Y-02/03 Semantics, reduced motion | Components | not run | I |
| PR-01 Onboarding | `feature/onboarding` | UiFlowTest (not run) | I |
| PR-04 Exercise types | `feature/lesson/Exercises.kt` | UiFlowTest partial (not run) | I |
| PR-08/09 Conversation & scenarios | `ConversationController`, `OfflineTutorEngine`, `feature/tutor` | TutorAndVoiceTest, ServicesIntegrationTest | V (logic) / I (UI) |
| PR-13 Writing feedback | `WritingService`, `WritingScreen` | ServicesIntegrationTest | V (logic) / I (UI) |
| PR-15 Mandarin | `mandarin/Pinyin`, tone exercises, Tones screen | MiscCoreTest, CurriculumIntegrityTest | V (logic) / I (UI) |
| PR-18 Professional tracks | Curriculum tracks (all five languages) | CurriculumIntegrityTest | V (content) / I (UI) |
| PR-19 Own material | `MaterialService`, Library | ServicesIntegrationTest | P (text/subtitles only) |
| PR-20 Relaxation | `relax/Relaxation`, RelaxScreen | MiscCoreTest | V (logic) / I (UI) |
| PR-23 Export/restore/delete | `SqlStore.exportJson/importJson`, `AppServices.deleteEverything`, Settings → Your data | SqlStoreTest, RestoreTest, ServicesIntegrationTest | V (logic); UI not run |
| PR-24 Reminders | `Reminders.kt` (Android) | — | B |
| Local models | `localai/LocalModels`, LocalModelsPage | MiscCoreTest | P (compatibility only; no runtime) |
| Visual verification (§4.14) | — | — | B |
| APK / release | `androidApp` | — | B |

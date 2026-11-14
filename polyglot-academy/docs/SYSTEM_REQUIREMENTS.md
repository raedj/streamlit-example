# System Requirements (SRS)

Requirement IDs are referenced from tests and `REQUIREMENTS_TRACEABILITY.md`.

## Learning engine
- **SR-LE-01 Spaced repetition.** SM-2 with learning steps (1 min, 10 min), graduating interval 1 day, easy interval 4 days, ease ∈ [1.3, 3.0], lapse → interval × 0.5 + relearning step 10 min, max interval 365 days, deterministic (no fuzz). (`SpacedRepetitionScheduler`)
- **SR-LE-02 Mastery estimate.** Difficulty-weighted EMA (α = 0.25) of scores per skill; hints reduce credit 15 %; confidence = 1 − 1/(1 + n/6); < 0.3 shown as "not enough data". (`MasteryTracker`)
- **SR-LE-03 Difficulty adjustment.** Window 8 attempts, minimum 4: accuracy ≥ 0.85 and median response < 20 s → +1; accuracy < 0.6 → −1 with hints and detailed explanations; bounded 1–5. (`DifficultyAdjuster`)
- **SR-LE-04 Sequencing.** Next lesson = first unmastered lesson at/above the starting level whose prerequisites are mastered (or below the starting level). Professional tracks are excluded from the general sequence. (`LessonSequencer`)
- **SR-LE-05 Recommendation.** Priority: placement → interrupted lesson → ≥10 due reviews → ≥5 open mistakes → goal met → interleave after a lesson (weakest skill / conversation) → next lesson. Each recommendation carries a reason shown to the learner. (`NextActivityRecommender`)
- **SR-LE-06 Placement.** Adaptive staircase (2 correct ↑, 1 wrong ↓), 8–14 items, rotates skills, reports estimate + range; never repeats an item. (`DiagnosticSession`)
- **SR-LE-07 Answer checking.** Normalises case, punctuation (Latin, Arabic, CJK), Arabic diacritics/alef/ya variants; accent-only slips count as correct with a note; similarity ≥ 0.85 → "almost". (`AnswerChecker`, `TextNormalizer`)

## Planning and analytics
- **SR-PL-01 Daily plan.** Reviews ≤ 40 % of budget (≥ 3 min if any due); one lesson (≤ 50 %); conversation if budget ≥ 15 min; remainder to weakest skill; relaxation only if enabled; missed day does not enlarge the plan. (`StudyPlanner`)
- **SR-AN-01 Analytics.** Minutes/day, weekly accuracy, streaks, retention buckets (model estimate, labelled), 7-day review forecast — all from stored events. (`AnalyticsCalculator`)
- **SR-AN-02 Active time** per session is capped at 120 s per completed item so an idle screen does not inflate study time. (`StudyService`)

## AI and privacy
- **SR-AI-01** Provider abstraction with adapters: OpenAI, Azure OpenAI, Google Gemini, Anthropic, DeepSeek, OpenAI-compatible local server; on-device slot reports "no runtime bundled".
- **SR-AI-02** Errors are classified (auth, quota, rate limit with Retry-After, timeout, network, server, invalid response, content filtered, not configured, blocked by privacy); messages never contain secrets.
- **SR-AI-03** Retries only for retryable errors, ≤ 2 per provider, exponential backoff 0.8 s → 8 s cap.
- **SR-AI-04** A provider is marked *verified* only after a real successful test request; saving a new key resets verification.
- **SR-PV-01** Cloud AI is off by default and requires accepting the current consent version.
- **SR-PV-02** Per-category control (lesson content, conversation, voice audio, writing, notes, imported material, learning history, work material); sensitive categories off by default; imported material can be allowed per item.
- **SR-PV-03** The router never uses a provider denied by the privacy policy. If the preferred provider fails or is denied, other *cloud* providers are used only when "allow fallback" is on; local/on-device providers are always allowed.
- **SR-PV-04** Raw audio is never sent to AI providers in this version.

## Speech
- **SR-SP-01** Voice states: Idle, Listening, Processing, Speaking, Paused, Failed, Completed; every state has an exit; cancel always returns to Idle. (`VoiceStateMachine`)
- **SR-SP-02** Recording stops when the learner leaves the conversation (flow cancellation → `SpeechRecognizer.cancel/destroy`).
- **SR-SP-03** Unavailable recognition/TTS is reported with a notice; typing always works.

## Data
- **SR-DB-01** SQLite via SQLDelight, schema v2, migrations verified at build time and by a test that upgrades a real v1 file.
- **SR-DB-02** Updating a conversation must not delete its turns (no `INSERT OR REPLACE` on FK parents).
- **SR-DB-03** Export as JSON (conversations/notes optional); API keys never exported.
- **SR-DB-04** "Delete all" removes database content, secrets and the reminder.

## Localization and accessibility
- **SR-L10N-01** Every key has English and Arabic text; placeholders match; plurals use CLDR forms (EN 2, AR 6).
- **SR-L10N-02** Arabic interface switches layout to RTL; target-language text carries a locale hint (correct Han glyphs, Arabic shaping).
- **SR-A11Y-01** Text/surface colour pairs meet WCAG AA (4.5:1); tested for light, dark and high-contrast palettes.
- **SR-A11Y-02** Touch targets ≥ 48 dp; charts expose text descriptions; status never by colour alone (icon + text).
- **SR-A11Y-03** Reduced-motion setting disables decorative animation.

## Platform
- Android minSdk 26, target/compile SDK 35; Kotlin 2.1.21; JVM target 17.

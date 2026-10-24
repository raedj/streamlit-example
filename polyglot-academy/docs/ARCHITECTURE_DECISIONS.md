# Architecture Decision Records

## ADR-001 Kotlin Multiplatform with an Android target and a JVM desktop target
**Context:** The product is an Android app. The authoring environment cannot reach Google Maven,
so neither AGP nor AndroidX can be resolved, and no emulator is available.
**Decision:** Put all logic and UI in KMP modules with `androidTarget()` (enabled by
`-Ppolyglot.android=true`) and a `jvm("desktop")` target that builds from Maven Central only.
**Consequences:** Logic and data can be compiled and tested now; the Android target is unverified
until built where Google Maven is reachable. The desktop target doubles as a verification harness.

## ADR-002 Compose Multiplatform 1.7.3 and Kotlin 2.1.21
**Decision:** CMP 1.7.3 is the newest line whose *compile-time* desktop artifacts resolve from
Maven Central. Kotlin 2.1.21 matches it. **Note:** at *runtime* every CMP release still needs
`androidx.collection` and `androidx.lifecycle`, which are Google-only — so the UI could be
compiled but not executed here. Upgrade to current CMP/Kotlin once Google Maven is available.

## ADR-003 SQLDelight instead of Room
**Decision:** SQLDelight 2.0.2 (Maven Central, multiplatform) with explicit `.sqm` migrations
verified at build time. Room is Google-Maven-only and KSP-dependent. Schema SQL avoids UPSERT
because Android API 26 ships SQLite 3.18.

## ADR-004 Manual dependency injection
**Decision:** One `AppServices` graph built in `PolyglotApplication`. Hilt needs AndroidX/KSP
and adds annotation processing to every module; the graph is small and easy to construct in
tests with fakes.

## ADR-005 Strings in Kotlin generated from a single source table
**Decision:** `ui/strings/*.strings` (KEY ¦ English ¦ Arabic) → `tools/gen_strings.py` →
`uicore/.../i18n/{Keys,EnglishStrings,ArabicStrings}.kt`. Keeps both languages aligned in one
row, works on every KMP target, and tests enforce completeness, placeholder parity and CLDR
plural counts. Android `strings.xml` is used only for system-visible text (app name, reminder).

## ADR-006 SM-2 spaced repetition with learning steps
**Decision:** SM-2 is documented, simple and deterministic. Retention figures shown to learners
are model estimates (exponential forgetting calibrated to 90 % at due time) and labelled so.
FSRS could replace it later behind the same interface.

## ADR-007 Rule-based adaptation; AI is never the only decision maker
**Decision:** Progression, difficulty and recommendations use transparent rules with reasons.
AI is used for conversation and feedback only.

## ADR-008 Offline scripted tutor
**Decision:** Without an allowed provider, conversations still work via keyword-matched
scenario beats, hints and model answers. The UI labels it "scripted, not AI".

## ADR-009 Privacy enforcement at a single point
**Decision:** Requests declare data categories; `PrivacyPolicy` + `ProviderRouter` evaluate
every candidate. Cloud fallback to a different vendor is opt-in.

## ADR-010 No bundled on-device LLM runtime
**Decision:** Ship compatibility checks and a catalogue but no runtime (llama.cpp/MediaPipe are
not available here and would add large native binaries). A learner-run local OpenAI-compatible
server is supported instead.

## ADR-011 Original vector illustrations drawn in code
**Decision:** Tutor avatars and illustrations are drawn with Compose Canvas from specs — no
third-party artwork, no image-generation dependency, theme-aware, tiny.

## ADR-012 Inexact daily reminders
**Decision:** `AlarmManager.setInexactRepeating` avoids the exact-alarm permission and saves
battery; the receiver enforces selected days and quiet hours.

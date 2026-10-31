# Database Schema (SQLDelight, version 3)

Files: `data/src/commonMain/sqldelight/com/polyglot/academy/data/db/*.sq`, migration
`migrations/1.sqm`, snapshot `databases/1.db`.

| Table | Key | Main columns | Indexes |
|---|---|---|---|
| kv_store | key | value (JSON: profile, settings, preferred provider) | — |
| exercise_attempt | id | lesson_id, exercise_id, language, skill, difficulty, correct, score, response_ms, hints_used, answered_at, user_answer (≤500 chars) | (language, answered_at) |
| study_session | id | language, kind, reference_id, started_at, ended_at, active_seconds, items_completed, items_correct, completed | (language, started_at) |
| lesson_progress | lesson_id | language, best_score, completions, last_completed_at, resume_index | (language) |
| mistake | id | language, skill, source_id, prompt, user_answer, correct_answer, explanation, severity, category, occurred_at, resolved, occurrences | (language, resolved), (language, source_id) v2 |
| assessment_result | id | language, kind, taken_at, estimated_level, level_low, level_high, skill_scores_json, questions_answered, correct_answers | (language, taken_at) |
| achievement | id | unlocked_at | — |
| daily_plan | (language, date) | plan_json | — |
| review_state | item_id | language, phase, ease, interval_days, learning_step, repetitions, lapses, last_reviewed_at, due_at, totals | (language, due_at) |
| review_log | id (auto) | item_id, language, reviewed_at, rating, previous/new interval, response_ms | (item_id) |
| user_vocabulary | id | language, item_json, created_at | (language) |
| saved_phrase | id | language, text, meaning, note, source_conversation_id, created_at | (language) |
| conversation | id | language, tutor_id, scenario_id, started_at, ended_at, summary | (language, started_at) |
| conversation_turn | id | conversation_id → conversation ON DELETE CASCADE, speaker, text, translation, corrections/alternatives/vocabulary JSON, created_at, produced_by, was_spoken, speaking_ms (v3) | (conversation_id, created_at) |
| imported_material | id | language, title, type, mime_type, size_bytes, imported_at, status, extracted_text, error_message, generated_vocabulary_json, cloud_processing_allowed | (language) |
| note | id | language, title, body, created/updated | — |
| bookmark | id, UNIQUE(kind, reference_id) | created_at, label (v2) | — |
| provider_config | id | config_json (no secrets) | — |

Built-in curriculum content is code, not data; review states reference built-in ids
(`en_hello`) or user ids (`uv_…`, `phrase_…`).

## Migrations
- v1 → v2: `ALTER TABLE bookmark ADD COLUMN label`; index `mistake_lang_source`.
- v2 → v3: `ALTER TABLE conversation_turn ADD COLUMN speaking_ms INTEGER` (approximate speaking time of spoken learner turns).
- Speaking missions are stored as conversations with `scenario_id = "mission:<id>"`; a passed mission adds `mission_passed=true` to the summary (no extra table).
- Verified by `verifyCommonMainPolyglotDatabaseMigration`, `SqlStoreTest.migrationFromVersion1PreservesData` and `migrationFromVersion2KeepsTurnsAndAddsSpeakingTime`.

## Rules
- Never `INSERT OR REPLACE` on rows referenced by foreign keys (REPLACE deletes first → cascade). Conversations use insert-or-ignore + update (regression test `ConversationIntegrityTest`).
- No UPSERT syntax (Android API 26 = SQLite 3.18).

## Retention
Data is kept until the learner deletes it. User answers are truncated to 500 characters.
Audio recordings are never stored.

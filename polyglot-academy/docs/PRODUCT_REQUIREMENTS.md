# Product Requirements (PRD)

## Vision
An original language-learning app that combines structured courses, adaptive review, and
conversation practice with patient virtual tutors — usable fully offline, with cloud AI as an
explicit, learner-controlled option. Interface in Arabic (RTL) and English; learning languages:
English, Mandarin Chinese, Spanish, French, Arabic.

## Target learners
- Arabic-speaking learners of English/Spanish/French/Mandarin (primary).
- English-speaking learners of Arabic and the other languages.
- Beginners (Pre-A1) to upper-intermediate (B2) for built-in content; advanced learners via AI
  tutors and professional tracks.

## Product requirements
| ID | Requirement | Status |
|---|---|---|
| PR-01 | Onboarding: interface/native/learning language, variety, level (test or self-select), goals, daily time, style, correction preference, voice disclosure, privacy choice, reminders, plan summary; optional steps skippable | Implemented (UI compiled, not run) |
| PR-02 | Placement test with adaptive difficulty that reports a level *range*, labelled as an estimate | Implemented + tested |
| PR-03 | Structured course per language (units → lessons) with prerequisites and mastery (≥80 %) | Implemented + tested |
| PR-04 | Ten exercise types: flashcard, multiple choice, fill-in, sentence order, matching, translation, dictation, speak-and-repeat, tone choice, free writing | Implemented (UI compiled, not run) |
| PR-05 | Spaced-repetition review of lesson words, imported words and saved phrases | Implemented + tested |
| PR-06 | Mistake log with de-duplication and resolution | Implemented + tested |
| PR-07 | Ten original tutor personas whose personality changes instructional behaviour | Implemented + tested (prompt content) |
| PR-08 | Conversation practice by text or voice; AI when allowed, otherwise an offline scripted partner that is clearly labelled as not AI | Implemented + tested (service level) |
| PR-09 | Role-play scenarios (café, directions, small talk, negotiation, loan review, bakery, tea shop…) | Implemented + tested |
| PR-10 | Grammar topics with rules, contrasting examples and common mistakes in the learner's explanation language | Implemented |
| PR-11 | Reading with sentence audio/translation, highlighted vocabulary and comprehension questions | Implemented |
| PR-12 | Listening/dictation with speed control | Implemented |
| PR-13 | Writing with offline checks and optional AI corrections that separate errors from stylistic options | Implemented + tested |
| PR-14 | Pronunciation practice using speech recognition, with an explicit disclaimer that it is not phonetic scoring | Implemented (UI) |
| PR-15 | Mandarin: pinyin with tone marks, tone drills, tone sandhi note, simplified/traditional toggle | Implemented + tested (pinyin) |
| PR-16 | Daily plan that fits the learner's time budget and does not grow after a missed day | Implemented + tested |
| PR-17 | Progress analytics from real events, separating observed metrics from estimates; no fabricated data | Implemented + tested |
| PR-18 | Optional professional tracks (business, banking/finance, credit risk & NPL, legal, career) with "not professional advice" disclaimers | Implemented: English (5 tracks); Spanish, French, Mandarin, Arabic ("for work" track) |
| PR-19 | Import of own material (text, subtitles) → extracted text review → word candidates → review deck | Implemented + tested; PDF/OCR/audio transcription not implemented |
| PR-20 | Optional relaxation/breathing with evidence-conscious disclaimer | Implemented |
| PR-21 | AI provider management with own API keys, verified connection tests, preferred provider | Implemented + tested (mocked HTTP) |
| PR-22 | Privacy controls per data category, consent before cloud AI, no silent fallback to another cloud provider | Implemented + tested |
| PR-23 | Export and full deletion of user data | Implemented + tested |
| PR-24 | Non-coercive reminders and achievements | Implemented (Android reminder code not compiled) |
| PR-25 | Accessibility: semantics, scalable text, high contrast, reduced motion, WCAG AA colours | Implemented; contrast tested; TalkBack not tested |

## Success metrics (proposed targets — not measured)
No real users have used the app, so there are no measured values. Proposed targets for a
first pilot: onboarding completion ≥ 80 %; ≥ 60 % of lesson starts completed; day-7 review
retention (GOOD/EASY rate) ≥ 80 %; crash-free sessions ≥ 99.5 %. The app records the events
needed to compute these locally (sessions, attempts, reviews); no analytics leave the device.

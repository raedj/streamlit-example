# Product Requirements (PRD)

Status as of 2026-11-01. "Tested" means automated tests; "on the emulator" means checked by hand on
an Android 15 emulator (AOSP image: no Google services, no TalkBack, no English or Chinese voice).
Nothing has been tried on a physical phone or by real learners.

## Vision
An original language-learning app that combines structured courses, adaptive review, and
conversation practice with patient virtual tutors — usable fully offline, with cloud AI as an
explicit, learner-controlled option. Interface in Arabic (the default, right to left) and English;
interface, explanation and learning languages are chosen separately. Learning languages: English,
Mandarin Chinese, Spanish, French, Arabic, Turkish, Urdu and Malay. Platform: Android. A desktop
build exists only as a test harness; there is no iOS app.

## Target learners
- Arabic-speaking learners of the other seven languages (primary), and English-speaking learners
  of Arabic and the others.
- Built-in content: beginners to intermediate. The general course has most lessons at A1–A2,
  fewer at B1 and B2. English and Mandarin also have a first C1 unit (four lessons, two long texts);
  the other languages have **no C1 course**, and no language has a **C2 course** (PR-26).
- Advanced learners: tutor conversations, speaking missions up to C1, the harder word-list levels
  and their own imported material.

## Product requirements
| ID | Requirement | Status |
|---|---|---|
| PR-01 | Onboarding: interface/native/learning language, variety, level (test or self-select), goals, daily time, style, correction preference, voice disclosure, privacy choice, reminders, plan summary; optional steps skippable | Implemented; UI test and on the emulator |
| PR-02 | Placement test with adaptive difficulty that reports a level *range*, labelled as an estimate | Implemented + tested |
| PR-03 | Structured course per language (units → lessons) with prerequisites and mastery (≥80 %) | Implemented + tested; content depth per level in PR-26 |
| PR-04 | Exercise types: flashcard, multiple choice, fill-in, sentence order, matching, translation, dictation, speak-and-repeat, tone choice, free writing; retry after a wrong answer (the score keeps the first try) | Implemented; UI tests and on the emulator |
| PR-05 | Spaced-repetition review of lesson words, imported words and saved phrases; the learner can postpone an item (a day, a week) or remove it | Implemented + tested |
| PR-06 | Mistake log with de-duplication and resolution | Implemented + tested |
| PR-07 | Ten original tutor personas whose personality changes instructional behaviour | Implemented + tested (prompt content) |
| PR-08 | Conversation practice by text or voice; AI when allowed, otherwise an offline scripted partner that is clearly labelled as not AI | Implemented + tested (service level); live voice calls not tried with a real key |
| PR-09 | Role-play scenarios (café, directions, small talk, negotiation, loan review, bakery, tea shop…) | Implemented + tested |
| PR-10 | Grammar topics with rules, contrasting examples and common mistakes in the learner's explanation language | Implemented |
| PR-11 | Reading with sentence audio/translation, highlighted vocabulary and comprehension questions; graded stories A1–B1 | Implemented |
| PR-12 | Listening/dictation with speed control | Implemented |
| PR-13 | Writing with offline checks and optional AI corrections that separate errors from stylistic options | Implemented + tested |
| PR-14 | Pronunciation practice: text similarity from speech recognition, clearly labelled as not phonetic scoring; optional Azure scoring with the learner's key | Implemented; Azure not tried with a real key |
| PR-15 | Mandarin: pinyin with tone marks, tone drills, tone sandhi (不, 一, third tones) and neutral-tone drills, hearing close words apart, simplified/traditional toggle, hiding pinyin for known words, character writing with stroke order and shape checks | Implemented + tested (drills built from the word list; checked on the emulator, which has no Chinese voice) |
| PR-16 | Daily plan that fits the learner's time budget and does not grow after a missed day; a reason for the suggested activity and the choice of another | Implemented + tested; on the emulator |
| PR-17 | Progress analytics from real events, separating observed metrics from estimates; no fabricated data | Implemented + tested |
| PR-18 | Optional professional tracks (business, banking/finance, credit risk & NPL, legal, career) with "not professional advice" disclaimers | Implemented: English (5 tracks); the other seven languages have "for work", "banking and finance", "credit and risk" and legal tracks (smaller than the English ones) |
| PR-19 | Import of own material (text, subtitles, text-based PDF, photos, scanned PDFs) → extracted text review → word candidates → review deck | Implemented + tested; photos read on the device for all eight languages (Tesseract), or with AI on request; audio and video transcribed on the device (Whisper base via whisper.cpp, model downloaded on request) |
| PR-20 | Optional relaxation/breathing with evidence-conscious disclaimer | Implemented |
| PR-21 | AI provider management with own API keys, verified connection tests, preferred provider; on Android also chat models that run on the phone (llama.cpp, Qwen2.5 0.5B/1.5B downloaded on request) | Implemented + tested (mocked HTTP); no real keys tried |
| PR-22 | Privacy controls per data category, consent before cloud AI, no silent fallback to another cloud provider; secret fields hidden from the keyboard; an in-app page on what is stored, what can leave the device, retention and deletion | Implemented + tested; page checked on the emulator. The legal privacy policy is not written or hosted (owner) |
| PR-23 | Export and full deletion of user data | Implemented + tested |
| PR-24 | Non-coercive reminders and achievements; points, quests and streak can be hidden | Implemented; reminder checked on the emulator |
| PR-25 | Accessibility: semantics, scalable text, high contrast, reduced motion, WCAG AA colours | Implemented; contrast tested; accessibility tree of ten screens and 200 % text checked on the emulator; TalkBack itself not run |
| PR-26 | Level coverage stated honestly: the Learn tab shows what each level offers, counted from the content; levels above the course are marked in onboarding, and a learner placed above it is told so | Implemented + tested; on the emulator. A first C1 unit for English and Mandarin only (AI-written, not reviewed by native speakers); no C2 course |
| PR-27 | Photos for import taken with the system camera app (no camera permission), with crop, rotate and retake | Implemented + tested; on the emulator's virtual camera, not a real one |
| PR-28 | End-to-end encrypted sync through the learner's own WebDAV server; optional background sync (off by default, unmetered network and battery not low, never settles a conflict on its own) | Implemented + tested; on the emulator with a local test server, not a real Nextcloud |
| PR-29 | Optional home-screen widget (never on the lock screen): today's progress, next step, a word to review that can be hidden | Implemented + tested; on the emulator's launcher |
| PR-31 | Writing the isolated Arabic and Urdu letters (Naskh) with separate checks for shape, dots (count and place; a dash for two dots and a caret for three accepted) and direction; demonstration, guide, letter picker, position kept | Implemented + tested; on the emulator. Templates not reviewed by a teacher; letters inside words (initial, medial, final forms) not covered |
| PR-30 | Content accuracy: report an error in lessons, reviews and word pages; per-item human review record; no second correct answer in single-answer questions | Implemented + tested; no human review has been done yet (the record is empty) |

## Not covered
- C1 courses for languages other than English and Mandarin, and C2 courses (PR-26).
- Arabic and Urdu letters inside words (initial, medial and final forms, joining); Nastaliq letter shapes for Urdu.
- Interface languages other than Arabic and English (each needs a reviewer).
- iOS.

## Success metrics (proposed targets — not measured)
No real users have used the app, so there are no measured values. Proposed targets for a
first pilot: onboarding completion ≥ 80 %; ≥ 60 % of lesson starts completed; day-7 review
retention (GOOD/EASY rate) ≥ 80 %; crash-free sessions ≥ 99.5 %. The app records the events
needed to compute these locally (sessions, attempts, reviews); no analytics leave the device.

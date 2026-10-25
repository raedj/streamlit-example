# Screen Specifications

All screens are implemented in `ui/src/commonMain/.../feature/`. **Verification status for every
screen: compiled; not run, not screenshotted, not tested with TalkBack** (see BLOCKERS B-02).
Common behaviour: loading → `LoadingPlaceholder`; failure → `StateMessage` with Retry; Arabic
→ RTL; offline → banner on Home and offline-capable features keep working.

| Screen (route) | Purpose / primary task | Key states | Notes |
|---|---|---|---|
| Onboarding (13 steps) | Configure languages, level, goals, time, style, correction, voice disclosure, privacy, reminders, plan | Step progress, skippable optional steps, saving | Choices saved at the end; interface language switches live |
| Home (`TabRoot HOME`) | "What now / how am I doing / what to review / start fast" | New-learner guidance (no stats), recommendation with reason, goal ring, reviews due, streak, quick actions, tutor shortcut, week chart, skills, achievements | Language switcher, search |
| Study plan (`Plan`) | Today's plan items, mark done, change daily time, rebuild | Missed-day note | Explains how the plan is built |
| Courses (`TabRoot LEARN`, `Courses(track)`) | Browse units/lessons, professional tracks | Locked / mastered / best score / below-level | Track disclaimer |
| Lesson overview | Summary, grammar focus, words with audio, start/resume/restart, bookmark | Resume index | |
| Lesson player | Exercises one by one with feedback, adaptation banner, exit confirmation, summary | Answered, correct/accent/almost/incorrect, summary with mastery & new words | Resume point saved on every step |
| Practice hub | Entry to review, mistakes, scenarios, grammar, reading, listening, writing, pronunciation, tones (Mandarin), vocabulary, relax | | |
| Review | SRS session with interval preview per rating | Empty "all caught up", session complete, leech hint | |
| Mistakes | Open mistakes by frequency; resolve | Empty, resolved list | |
| Grammar list / topic | Rules, examples (incorrect marked by icon + label), common mistakes | | Links to lesson |
| Reading list / passage | Tap sentence → audio + translation; word list → add to reviews; questions | Result banner | |
| Listening | Dictation with speed slider | Feedback, next | |
| Writing | Prompt choice, editor with word count, basic checks, AI feedback | AI blocked/failed banners, corrections vs optional style, drafts | |
| Pronunciation | Speak-and-repeat with recognizer match % | Recognizer unavailable → self-rating | Disclaimer: not phonetic scoring |
| Tone trainer | Tone-choice drills, two-syllable words with tone numbers, traditional toggle | Not-Mandarin note | Sandhi note |
| Character writing | 田字格 writing box (finger/mouse), faded guide toggle, undo/clear/check, stroke count, components with meaning/sound roles, mnemonic hint, word + audio | Not-Mandarin note; "draw first" | Shape-only checking disclosed; stroke-count hint |
| Relax | Choose breathing pattern and rounds, guided circle | Running, done, reflection | Disclaimer; stop any time |
| Tutors / tutor detail | Persona cards; bio, correction style, focus, select, customise, scenarios | Selected | |
| Scenarios | Role-play list sorted by closeness to level | | |
| Travel phrasebook | Six situations (essentials, emergencies, restaurant, hotel, transport, sightseeing), phrase + reading + meaning, audio, add to reviews | — | Built from the travel lessons |
| Speaking path | Speaking measurements (median words/turn, longest turn, words per minute, spoken share, vocabulary variety, corrections, weekly chart), next mission, 5 levels × 6 missions with status | No data yet; all passed | Missions start a tutor conversation |
| Tutors | "Talk about anything" card (topic field + 8 suggestions, starts with the selected tutor), tutor cards | — | Needs-AI note |
| Conversation | Topic pill and "needs an AI tutor" notice with a Set up AI action for free talk offline; transcript, mic/typing, translations toggle, speed, save phrase, mission panel (goal, tasks, target), end → summary (incl. mission result and gaps) | Engine label (AI via X / offline scripted + reason), Listening/Thinking/Speaking/Failed, notices | Recording stops on leave |
| Placement test | Intro → adaptive questions (I don't know) → result range → accept/choose | History | Disclaimer |
| Progress | Totals, study time (7/28 d), consistency heatmap, weekly accuracy, skill accuracy, memory estimate, review forecast, placement history, definitions | Empty state | Observed vs estimated labelled |
| Me / profile | Name, languages (switch/add), goals, links | | |
| History | Completed sessions by day | Empty | |
| Vocabulary | Words (status, next review, audio, add/remove) and phrasebook; search; due filter | Empty | |
| Library | Import file / paste, processing progress, materials, support table; notes; bookmarks | Rejected (size/type/empty) toasts | Rights notice |
| Material | Edit extracted text, read aloud, choose candidates (+ meanings), add to reviews, delete | Unsupported/failed | Deletes derived words too |
| Search | Words, lessons, grammar, readings, notes | Start hint, no results | |
| Settings + 13 pages | Appearance, languages (incl. variety, level, pinyin, traditional), learning, speech, accessibility, privacy (consent dialog, categories), AI providers, provider detail (key, model, URL, deployment, test, preferred, usage), on-device models (device check), reminders (time, days, quiet hours), data (export, restore with confirmation, delete), permissions, help, about | | |

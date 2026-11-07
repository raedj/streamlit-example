# Tutor Personas

Original characters (`core/.../tutor/Tutors.kt`). Personality changes the AI system prompt
(behaviour, correction strategy) and, for the offline partner, the scenario/opening. Avatar
implementation level 2 (animated 2D vector); no lip-sync, no photorealism.

| ID | Name | Languages | Style | Correction strategy | Levels | Behaviour (prompt summary) |
|---|---|---|---|---|---|---|
| emma | Emma | English | Supportive | Recast | Pre-A1–B2 | Warm, recasts, one follow-up question, 1–3 sentences |
| oliver | Oliver | English | Structured | Explicit with rule | A1–C1 | Names the rule + one example, ≤ 2 corrections per turn |
| liwei | Li Wei | Mandarin | Structured | Prompt self-correction | Pre-A1–B2 | Always hanzi + tone-marked pinyin; points to the syllable |
| sofia | Sofía | Spanish | Supportive | Recast | Pre-A1–B2 | Notes Spain/LatAm differences; flags gender agreement |
| claire | Claire | French | Socratic | Prompt self-correction | Pre-A1–C1 | Guiding questions; liaison and silent letters |
| alex | Alex | Any | Role-play | Recast | A1–C1 | Stays in character; steps out only for misunderstandings |
| morgan | Professor Morgan | Any | Structured | Explicit with rule | B1–C2 | Contrasts near-synonyms; separates errors from style |
| maya | Maya | Any | Storytelling | Recast | A1–C1 | Story in parts, 1–2 new words per turn |
| daniel | Daniel | Any | Professional | Explicit with rule | A2–C2 | Professional register; "not financial/legal advice" |
| nora | Nora | Any | Confidence coach | Recast | Pre-A1–C2 | Reuses recent words; specific, non-exaggerated praise |

All prompts additionally instruct: use the learner's CEFR estimate, the chosen correction
intensity, separate stylistic alternatives, admit uncertainty, general-audience content, and
return a JSON object (reply, translation, corrections, alternatives, vocabulary).

## Free conversation
Any tutor can hold an open conversation: the learner types a topic (or picks one of eight suggestions) on the
Tutors tab, and the AI tutor opens the conversation on it, follows topic changes, corrects the learner in every
reply (shown under the learner's sentence) and keeps its own turns short. The topic is sanitised (one line, no
quotes, 120 characters) before it is quoted in the prompt. Without an AI provider the offline tutor can only follow
a short script, and the conversation screen says so with a link to set up a provider.

## AI language-coach protocol
Every AI conversation (free talk, chosen topics, role-plays and missions) follows the coach protocol
(`core/.../tutor/Coach.kt`); the offline tutor only follows its scripts.

| Protocol part | How it is done |
|---|---|
| Welcome and track choice | In the first free session without a track, the coach opens the conversation, welcomes the learner and asks for a track: everyday fluency, business and emails, travel and hospitality, or exam preparation (IELTS/TOEFL, DELE/SIELE, DELF/DALF, HSK, or Arabic proficiency exams). The reply's `track` field is saved to the profile. The learner can also choose or change it on the Tutors tab. The learning language comes from the profile, not from the chat |
| Adaptive warm-up | At the start of a free session, up to three studied words (due for review first, then the weakest) are given to the coach for one or two quiz questions. This happens only on the first request of a session |
| Micro-learning and role-play | The prompt allows at most one new point or three new words per reply, and short role-plays where they fit |
| Socratic four-part feedback | Each correction has the original, the correction, the reason (in the explanation language), the register (formal, informal or neutral) and an error type. For minor errors the coach gives a guiding question (`hint`); the app shows "Try to fix it yourself" and reveals the answer only on request |
| Hidden error log | Every non-stylistic correction is recorded as a mistake grouped by error type (`conversation:<type>`). Types that recur (2 or more, unresolved) are sent back to the coach with an example, to re-test from time to time |
| Frustration handling | If the learner's last message sounds frustrated (EN/AR/ES/FR/ZH phrases) or each of the last three turns had 2 or more errors, the coach is told to stop quizzing, encourage, simplify and use an everyday example |
| Titles | First Steps → Word Explorer (25 words in review) → Conversation Starter (5 conversations of 4+ turns) → Phrasing Pro (10 conversations, ≤0.3 corrections per turn over the last 10+ turns) → Mission Master (10 missions) → Fluent Voice (20 missions, median 12+ words per turn). Titles are earned in order and shown on the Speaking path |
| Ending | Every reply must end with one specific question or one short task, in an encouraging tone |

Exam-style tasks are written by the AI model and are not official exam material; any score is called an estimate.

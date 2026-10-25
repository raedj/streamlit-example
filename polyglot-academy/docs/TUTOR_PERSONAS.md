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

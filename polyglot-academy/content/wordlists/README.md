# Word lists

Frequency word lists: up to 3000 lemmas per language in 10 levels of 300 (level 1 = ranks
1–300, level 10 = ranks 2701–3000). After editing, run `python3 tools/gen_wordlists.py` and
then `./gradlew :core:desktopTest --tests "*WordListTest*"`.

## `<lang>.words.tsv`
Tab-separated, UTF-8, one entry per line, header required. Trailing empty columns may be omitted.

| Column | Content |
|---|---|
| `rank` | 1, 2, 3 … with no gaps; the level is derived from it (`(rank-1)/300+1`) |
| `id` | `<lang>_w_<lowercase_ascii>`; stable even if the rank changes |
| `term` | Lemma (dictionary form). Arabic with full vowel marks; Mandarin in simplified characters |
| `pos` | `noun verb adj adv pron prep conj interj num phrase part measure det` |
| `topic` | Lowercase topic code (`food`, `time`, `function`, …) |
| `en`, `ar` | Meaning in English and Arabic |
| `pron` | English: IPA between slashes. Mandarin: pinyin with tone marks (a neutral tone is written with a leading middle dot: 的 ·de, 妈妈 māma). Arabic, Urdu: Latin transliteration. Others: optional IPA |
| `forms` | Comma-separated inflected or irregular forms (`went, gone`; `fue`; `l'`). Also used to find the word in example sentences |
| `extra` | `key=value; key=value` — language-specific fields (below) |
| `vocab_id` | Id of the same word in the hand-written curriculum (keeps learners' review state) |

Language-specific `extra` keys (required ones in bold):
- **es/fr nouns:** **`gender`** (`m`, `f`, `mf`), **`article`** (el/la, le/la/l'), `plural` (irregular plural); verbs: `irregular`.
- **ar nouns:** **`gender`**, **`plural`** (broken plural where it applies), **`root`**; adjectives: **`root`**; verbs: **`root`**, **`masdar`**, **`present`**.
- **zh nouns:** **`measure`** (classifier); any: `trad` (when different), `hsk` (HSK level when it applies).
- **ur nouns:** **`gender`**; `plural`.
- **tr:** `stem` (if the verb stem is not the infinitive minus -mek/-mak).
- **en/ms:** `irregular`.

## `<lang>.examples.tsv`
| Column | Content |
|---|---|
| `rank` | Rank of the word the example illustrates |
| `sentence` | 4–12 words (Mandarin: 4–24 characters), everyday and natural, using the word in its most common sense |
| `en`, `ar` | Translations (for English the `en` column, for Arabic the `ar` column repeats the sentence) |
| `names` | Comma-separated proper names in the sentence; they are ignored by the checks |

Levels 1–3 need at least two examples per word, higher levels at least one.

## Checks
`tools/gen_wordlists.py` rejects a file with the line and reason for: wrong headers or column
counts, gaps in ranks, incomplete levels, empty required fields, invalid ids or parts of speech,
missing language-specific fields, pinyin without tone marks, Arabic without vowel marks,
duplicate entries, example length, and too few examples.

`WordListTest` (Gradle) additionally checks with the app's own matcher that each example
contains its word or a registered form, and that at least 85% of the example's words (names and
numbers excluded) have a rank of at most the word's rank + 300. In level 1 this means examples
can use only the 300 words of the level itself.

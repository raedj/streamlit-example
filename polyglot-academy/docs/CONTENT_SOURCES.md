# Content Sources and Review Status

## Origin of the built-in content
All built-in lessons, vocabulary entries, example sentences, translations, grammar notes,
role-plays, graded stories and placement questions were written for this app. Nothing is copied
from dictionaries, textbooks, course books or other apps. The content was drafted by an AI model
(Claude) and checked by automated tests for structure only (ids, answers accepted by the
checker, glossary terms present in the text, exercise validity). The tests do **not** check
grammar, naturalness, or the accuracy of translations, IPA or transliteration.

No external word-frequency list or other third-party data set is bundled at present.

## Review status
| Language | Built-in content | Native-speaker review |
|---|---|---|
| English | Course, tracks, stories | Not reviewed |
| Mandarin Chinese | Course, tracks, stories, character data | Not reviewed |
| Spanish | Course, tracks, stories | Not reviewed |
| French | Course, tracks, stories | Not reviewed |
| Arabic | Course, tracks, stories | Not reviewed |
| Turkish | Starter course A1–B2, tracks, 3 stories (added 2026-10-25) | Not reviewed |
| Urdu | Starter course A1–B2 with romanized transliteration, tracks, 3 stories (added 2026-10-25) | Not reviewed |
| Malay (Bahasa Melayu, Malaysia) | Starter course A1–B2, tracks, 3 stories (added 2026-10-25) | Not reviewed |

**Recommendation:** have a native speaker (ideally a teacher) review each language before a
public release, starting with Turkish, Urdu and Malay, and record the reviewer and date here.

## Notes per language
- **Turkish:** IPA is given for most entries; vowel-harmony and consonant-assimilation forms in
  exercises follow standard Istanbul Turkish.
- **Urdu:** Urdu script with a simple romanization (macrons for long vowels, ṭ/ḍ/ṛ for
  retroflex consonants, ṇ for nasalisation). Gendered forms are given as m./f. pairs where the
  speaker's gender matters. On Android, Urdu learning content uses the bundled Noto Nastaliq
  Urdu font (see "Fonts" below); other text uses the system Naskh-style font.
- **Malay:** Standard Malaysian Malay (Bahasa Melayu). Indonesian (Bahasa Indonesia) differs in
  vocabulary and spelling and is not covered.

## Frequency word lists (3000 words, 10 levels)
**Ranking source: approximate.** No frequency list with a licence that clearly allows bundling
in this app has been verified yet, so the ranks are an approximate ordering by an AI model
(Claude) from general knowledge of everyday, spoken-language frequency, with function words
(articles, pronouns, prepositions, conjunctions) placed according to their very high frequency.
Ranks are by lemma (go = go, went, gone). Treat rank differences of a few dozen places as noise.
If a properly licensed list is adopted later (for example one derived from subtitle corpora or
Wiktionary, or HSK lists for Mandarin), record here its source, version, licence, attribution and
any share-alike obligations, and re-rank.

All meanings, translations and example sentences in the word lists are original and AI-written.
Mandarin pinyin, measure words (classifiers), traditional forms and HSK levels are also written from
general knowledge, not copied from an HSK syllabus file; the HSK levels follow the older 6-level
scheme and are approximate. A neutral tone is written with a leading middle dot (的 ·de).
Arabic terms carry full vowel marks; roots, broken plurals, masdars and present forms are written
from general knowledge. Arabic example sentences are unvowelled (as in everyday print) and their
`ar` column repeats the sentence, like the `en` column of the English list. The clitics و، ف، بـ،
كـ، سـ are not separate entries (they are always attached); لِـ is, with its pronoun forms (لي، لك…).
Turkish entries have no pronunciation column (Turkish spelling is phonemic); irregular stems are given
as forms (bana ← ben, oğlu ← oğul, yiyor ← yemek).
Urdu terms are written without vowel marks (as in normal print), with a Latin transliteration
(ā, ī, ū for long vowels, ṭ ḍ ṛ for retroflex consonants, a tilde for nasal vowels); verbs are
listed by infinitive, and irregular forms (گیا ← جانا، کیا ← کرنا، مجھے ← میں) are given as forms.
Malay is Bahasa Melayu as used in Malaysia (standard spelling, a few common spoken forms such as tak,
nak and tapi listed as forms). Most verbs are listed by root (beli, tulis); affixed forms (membeli,
menulis, bermain) are recognised by rule, and belajar and bekerja are listed as they are usually taught.
CEFR equivalents of the 10 levels are approximate (1–2 ≈ A1, 3–4 ≈ A2, 5–6 ≈ B1, 7–8 ≈ B2,
9–10 ≈ B2+ to C1). No claim is made about how much of real text these words cover.

| Language | Levels written | Entries | Examples | Native-speaker review |
|---|---|---|---|---|
| English | 1–10 | 3000 / 3000 | 3900 | Not reviewed |
| Mandarin Chinese | 1–10 | 3000 / 3000 | 3900 | Not reviewed |
| Spanish | 1–10 | 3000 / 3000 | 3900 | Not reviewed |
| French | 1–10 | 3000 / 3000 | 3900 | Not reviewed |
| Arabic | 1–10 | 3000 / 3000 | 3900 | Not reviewed |
| Turkish | 1–10 | 3000 / 3000 | 3900 | Not reviewed |
| Urdu | 1–10 | 3000 / 3000 | 3900 | Not reviewed |
| Malay | 1–10 | 3000 / 3000 | 3900 | Not reviewed |

Size: one level adds about 146 KB of dex code, about 59 KB compressed in the APK (measured for
English level 1). All 80 levels would add roughly 5 MB compressed. If that becomes a problem, the
rows can move from string literals to compressed resources without changing the format.

## Fonts

| Font | File | Licence | Source | Use |
|---|---|---|---|---|
| Noto Nastaliq Urdu (variable weight, 674 KB, SHA-256 `98a4787f…abbcd`) | `androidApp/src/main/res/font/noto_nastaliq_urdu.ttf` | SIL Open Font License 1.1, copyright 2022 The Noto Project Authors; full text in `docs/licenses/OFL-NotoNastaliqUrdu.txt` | `ofl/notonastaliqurdu/` in github.com/google/fonts (downloaded 2026-10-30 with the owner's approval) | Urdu learning content on Android |

## Stroke-order data

| Data | File | Licence | Source | Use |
|---|---|---|---|---|
| Stroke medians for the 2787 Chinese characters the app uses (simplified and traditional forms) | `content/hanzi/strokes.tsv`, compiled by `tools/gen_strokes.py` into `core/.../mandarin/generated/` | Arphic Public License (`content/hanzi/ARPHICPL.TXT`); the data is a modified version of `graphics.txt` and stays under that licence, so anyone may copy, modify and redistribute it on the same terms | Make Me a Hanzi, commit 618dbab (2018-10-16), derived from the fonts Arphic PL KaitiM GB and Arphic PL UKai (downloaded 2026-10-30 with the owner's approval) | Stroke-order animation and order checking |

What was changed is recorded in the header of `strokes.tsv`: only the app's characters, only the
medians (outlines removed), y axis flipped to screen orientation, coordinates rounded, tab-separated
format. `tools/extract_strokes.py` reproduces the file from the original `graphics.txt`. The rest of
the app is not under the Arphic licence; the data is a separate work aggregated with it. This reading
of the licence is not legal advice.

## On-device text recognition (OCR)

| Item | File | Licence | Source | Use |
|---|---|---|---|---|
| Arabic and Urdu recognition models (`ara.traineddata` 1.4 MB, `urd.traineddata` 1.4 MB) | `androidApp/src/main/assets/tessdata/` | Apache License 2.0 (`docs/licenses/Apache-2.0-tessdata_fast.txt`) | tesseract-ocr/tessdata_fast, commit 8741641 (2024-08-01), downloaded 2026-10-30 with the owner's approval; SHA-256 ara e3206d3d…b2bb3e729, urd 62e8250c…5d0ce3c5b5008387fd0d555f | Reading Arabic and Urdu photos on the device |
| Tesseract4Android 4.9.0 (Tesseract OCR with Leptonica, libjpeg, libpng) | Gradle dependency `cz.adaptech.tesseract4android:tesseract4android`, from JitPack | Apache License 2.0 (Tesseract, Tesseract4Android); Leptonica, libjpeg and libpng under their own permissive licences | github.com/adaptech-cz/Tesseract4Android | OCR engine on Android |

The models are unmodified. Models for the other six learning languages (about 16 MB together) are
not bundled.

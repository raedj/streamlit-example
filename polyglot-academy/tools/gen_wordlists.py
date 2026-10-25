#!/usr/bin/env python3
"""Validates content/wordlists/<lang>.words.tsv and <lang>.examples.tsv and generates Kotlin sources.

Output: core/src/commonMain/kotlin/com/polyglot/academy/core/wordlist/generated/
  <Lang>Level<NN>.kt   one object per language and level (at most 300 entries), loaded lazily
  GeneratedWordLists.kt  index of the available levels per language

This script checks the structure (columns, ranks, levels, required fields, example counts and
lengths, tone marks, Arabic vowel marks, duplicates). The language-aware checks (the example
contains the word; at least 85% of the example's words are within rank + 300) run in the Kotlin
test WordListTest, using the same matcher the lessons use. Any problem stops the script with the
file, line and reason. Run from the repository root of the Android project:
    python3 tools/gen_wordlists.py
"""
import glob, os, re, sys

LEVEL_SIZE, LEVELS, TARGET = 300, 10, 3000
LANGS = {'en': 'En', 'zh': 'Zh', 'es': 'Es', 'fr': 'Fr', 'ar': 'Ar', 'tr': 'Tr', 'ur': 'Ur', 'ms': 'Ms'}
WORD_COLS = ['rank', 'id', 'term', 'pos', 'topic', 'en', 'ar', 'pron', 'forms', 'extra', 'vocab_id']
EXAMPLE_COLS = ['rank', 'sentence', 'en', 'ar', 'names']
POS = {'noun', 'verb', 'adj', 'adv', 'pron', 'prep', 'conj', 'interj', 'num', 'phrase', 'part', 'measure', 'det'}
# Allowed keys in the 'extra' column and the keys each part of speech must have, per language.
EXTRA_KEYS = {
    'en': {'irregular'},
    'es': {'gender', 'article', 'plural', 'irregular'},
    'fr': {'gender', 'article', 'plural', 'irregular'},
    'ar': {'gender', 'plural', 'root', 'masdar', 'present'},
    'zh': {'trad', 'hsk', 'measure'},
    'tr': {'stem', 'irregular'},
    'ur': {'gender', 'plural', 'irregular'},
    'ms': {'irregular'},
}
REQUIRED_EXTRA = {
    ('es', 'noun'): {'gender', 'article'}, ('fr', 'noun'): {'gender', 'article'},
    ('ar', 'noun'): {'gender', 'plural', 'root'}, ('ar', 'adj'): {'root'}, ('ar', 'verb'): {'root', 'masdar', 'present'},
    ('zh', 'noun'): {'measure'}, ('ur', 'noun'): {'gender'},
}
PRON_REQUIRED = {'en', 'zh', 'ar', 'ur'}
TONE_MARKS = set('āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ')
ARABIC_MARKS = re.compile('[ً-ْ]')
ARABIC_LETTERS = re.compile('[ء-ي]')

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
src = os.path.join(root, 'content', 'wordlists')
out = os.path.join(root, 'core', 'src', 'commonMain', 'kotlin', 'com', 'polyglot', 'academy', 'core', 'wordlist', 'generated')
errors = []


def fail(path, line, msg):
    errors.append(f"{os.path.relpath(path, root)}:{line}: {msg}")


def read_tsv(path, cols):
    rows = []
    with open(path, encoding='utf-8') as f:
        lines = f.read().split('\n')
    if not lines or lines[0].rstrip('\r') != '\t'.join(cols):
        fail(path, 1, 'header must be: ' + '\\t'.join(cols))
        return rows
    for n, raw in enumerate(lines[1:], 2):
        line = raw.rstrip('\r')
        if not line.strip() or line.startswith('#'):
            continue
        parts = line.split('\t')
        # Editors often drop trailing tabs, so missing trailing (optional) columns are read as empty.
        if len(parts) > len(cols) or len(parts) < 4:
            fail(path, n, f'expected {len(cols)} tab-separated columns, got {len(parts)}')
            continue
        parts += [''] * (len(cols) - len(parts))
        rows.append((n, dict(zip(cols, (p.strip() for p in parts)))))
    return rows


def word_count(lang, text):
    if lang == 'zh':
        return len(re.findall(r'[一-鿿]', text))
    return len([t for t in re.split(r"[\s\-]+", text) if re.search(r'\w', t)])


def check_language(lang):
    wpath, epath = os.path.join(src, f'{lang}.words.tsv'), os.path.join(src, f'{lang}.examples.tsv')
    if not os.path.exists(wpath):
        return None
    if not os.path.exists(epath):
        fail(wpath, 0, f'{lang}.examples.tsv is missing')
        return None
    words, examples = read_tsv(wpath, WORD_COLS), read_tsv(epath, EXAMPLE_COLS)
    ids, vocab_ids, seen = set(), set(), set()
    expected = 1
    for n, w in words:
        if not w['rank'].isdigit() or int(w['rank']) != expected:
            fail(wpath, n, f"rank must be {expected} (ranks are consecutive from 1), got {w['rank']!r}")
        expected += 1
        for col in ('id', 'term', 'pos', 'topic', 'en', 'ar'):
            if not w[col]:
                fail(wpath, n, f'{col} is empty')
        if not re.fullmatch(rf'{lang}_w_[a-z0-9_]+', w['id']):
            fail(wpath, n, f"id must look like {lang}_w_<lowercase_ascii>, got {w['id']!r}")
        if w['id'] in ids:
            fail(wpath, n, f"duplicate id {w['id']}")
        ids.add(w['id'])
        if w['vocab_id']:
            if not re.fullmatch(rf'{lang}_[a-z0-9_]+', w['vocab_id']):
                fail(wpath, n, f"vocab_id must be an existing {lang}_… vocabulary id, got {w['vocab_id']!r}")
            if w['vocab_id'] in vocab_ids:
                fail(wpath, n, f"vocab_id {w['vocab_id']} is linked twice")
            vocab_ids.add(w['vocab_id'])
        if w['pos'] not in POS:
            fail(wpath, n, f"pos must be one of {sorted(POS)}, got {w['pos']!r}")
        if not re.fullmatch(r'[a-z_]+', w['topic']):
            fail(wpath, n, f"topic must be lowercase letters and _, got {w['topic']!r}")
        key = (w['term'].lower(), w['pos'], w['en'].lower())
        if key in seen:
            fail(wpath, n, f'duplicate entry: same term, part of speech and meaning {key}')
        seen.add(key)
        if lang in PRON_REQUIRED and not w['pron']:
            fail(wpath, n, 'pronunciation is required for this language')
        # Neutral-tone syllables are written with a leading middle dot (的 ·de), as in many dictionaries.
        if lang == 'zh' and w['pron'] and not (set(w['pron']) & TONE_MARKS) and '·' not in w['pron']:
            fail(wpath, n, f"pinyin needs tone marks (write a neutral tone as ·de), got {w['pron']!r}")
        if lang == 'en' and w['pron'] and not (w['pron'].startswith('/') and w['pron'].endswith('/')):
            fail(wpath, n, f"IPA must be written between slashes, got {w['pron']!r}")
        if lang == 'ar' and len(ARABIC_LETTERS.findall(w['term'])) > 1 and not ARABIC_MARKS.search(w['term']):
            fail(wpath, n, f"Arabic term needs vowel marks (tashkeel), got {w['term']!r}")
        extra = {}
        for item in filter(None, (x.strip() for x in w['extra'].split(';'))):
            if '=' not in item:
                fail(wpath, n, f'extra must be key=value pairs separated by ;, got {item!r}')
                continue
            k, v = (x.strip() for x in item.split('=', 1))
            if k not in EXTRA_KEYS[lang]:
                fail(wpath, n, f'extra key {k!r} is not allowed for {lang} (allowed: {sorted(EXTRA_KEYS[lang])})')
            if not v:
                fail(wpath, n, f'extra key {k!r} has no value')
            extra[k] = v
        missing = REQUIRED_EXTRA.get((lang, w['pos']), set()) - extra.keys()
        if missing:
            fail(wpath, n, f"{w['pos']} entries need extra keys {sorted(missing)}")
        if lang in ('es', 'fr') and 'gender' in extra and extra['gender'] not in ('m', 'f', 'mf'):
            fail(wpath, n, 'gender must be m, f or mf')
        if lang in ('ar', 'ur') and 'gender' in extra and extra['gender'] not in ('m', 'f', 'mf'):
            fail(wpath, n, 'gender must be m, f or mf')
    count = len(words)
    if count == 0 or count % LEVEL_SIZE or count > TARGET:
        fail(wpath, 0, f'the list must hold whole levels of {LEVEL_SIZE} entries (at most {TARGET}); it has {count}')
    by_rank = {}
    for n, e in examples:
        if not e['rank'].isdigit() or not (1 <= int(e['rank']) <= count):
            fail(epath, n, f"rank {e['rank']!r} does not exist in {lang}.words.tsv")
            continue
        for col in ('sentence', 'en', 'ar'):
            if not e[col]:
                fail(epath, n, f'{col} is empty')
        c = word_count(lang, e['sentence'])
        lo, hi = (4, 24) if lang == 'zh' else (4, 12)
        if not lo <= c <= hi:
            fail(epath, n, f'example length must be {lo}–{hi} {"characters" if lang == "zh" else "words"}, got {c}')
        by_rank.setdefault(int(e['rank']), []).append(e)
    for n, w in words:
        if not w['rank'].isdigit():
            continue
        r = int(w['rank'])
        need = 2 if (r - 1) // LEVEL_SIZE + 1 <= 3 else 1
        have = len(by_rank.get(r, []))
        if have < need:
            fail(epath, 0, f"rank {r} ({w['term']}) needs at least {need} examples, has {have}")
    return words, examples


def lit(s):
    return '"' + s.replace('\\', '\\\\').replace('"', '\\"').replace('$', '\\$') + '"'


def write(path, text):
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(text)


def main():
    results = {lang: check_language(lang) for lang in LANGS}
    if errors:
        print('\n'.join(errors), file=sys.stderr)
        sys.exit(f'{len(errors)} problem(s); nothing was generated.')
    os.makedirs(out, exist_ok=True)
    for old in glob.glob(os.path.join(out, '*.kt')):
        os.remove(old)
    hdr = '// GENERATED by tools/gen_wordlists.py from content/wordlists — do not edit by hand.\npackage com.polyglot.academy.core.wordlist.generated\n\n'
    index = {}
    for lang, res in results.items():
        if res is None:
            continue
        words, examples = res
        for level in range(1, len(words) // LEVEL_SIZE + 1):
            lo, hi = (level - 1) * LEVEL_SIZE + 1, level * LEVEL_SIZE
            name = f'{LANGS[lang]}Level{level:02d}'
            ws = ['\t'.join(w[c] for c in WORD_COLS) for _, w in words if lo <= int(w['rank']) <= hi]
            es = ['\t'.join(e[c] for c in EXAMPLE_COLS) for _, e in examples if lo <= int(e['rank']) <= hi]
            body = f'internal object {name} {{\n    val words: List<String> = listOf(\n'
            body += ''.join(f'        {lit(x)},\n' for x in ws)
            body += '    )\n    val examples: List<String> = listOf(\n'
            body += ''.join(f'        {lit(x)},\n' for x in es)
            body += '    )\n}\n'
            write(os.path.join(out, name + '.kt'), hdr + body)
            index.setdefault(lang, []).append(name)
        print(f'{lang}: {len(words)} entries, {len(examples)} examples, {len(words) // LEVEL_SIZE} level(s)')
    body = '/** Rows of each generated level: words then examples, as tab-separated lines. */\ninternal object GeneratedWordLists {\n'
    body += '    fun levels(languageTag: String): List<() -> Pair<List<String>, List<String>>> = when (languageTag) {\n'
    for lang, names in index.items():
        body += f'        "{lang}" -> listOf(' + ', '.join(f'{{ {n}.words to {n}.examples }}' for n in names) + ')\n'
    body += '        else -> emptyList()\n    }\n}\n'
    write(os.path.join(out, 'GeneratedWordLists.kt'), hdr + body)


if __name__ == '__main__':
    main()

#!/usr/bin/env python3
"""Strict check of a word list's examples, mirroring core/.../wordlist/WordMatcher.kt.

WordListTest allows up to 15% of an example's words to be outside the list (rank + 300). This
script is stricter and meant for authoring: it lists every word of every example that the list
does not know at all, every example that does not contain its own word, and (with -v) the words
accepted only through an inflection rule, so they can be checked by hand. Keep its rules in step
with WordMatcher.kt. Usage, from the Android project root:
    python3 tools/scan_wordlist.py <lang> [-v] [--level N]
"""
import os, re, sys

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
lang = sys.argv[1]
verbose = '-v' in sys.argv
only_level = int(sys.argv[sys.argv.index('--level') + 1]) if '--level' in sys.argv else None
WORD_COLS = ['rank', 'id', 'term', 'pos', 'topic', 'en', 'ar', 'pron', 'forms', 'extra', 'vocab_id']
TR_SUF = ["iyor", "ıyor", "uyor", "üyor", "yor", "acak", "ecek", "acağ", "eceğ", "mış", "miş", "muş", "müş", "malı", "meli", "abil", "ebil",
          "arak", "erek", "ınca", "ince", "unca", "ünce", "dan", "den", "tan", "ten", "nın", "nin", "nun", "nün", "lar", "ler", "mız", "miz",
          "muz", "müz", "nız", "niz", "nuz", "nüz", "sın", "sin", "sun", "sün", "dır", "dir", "dur", "dür", "tır", "tir", "tur", "tür",
          "maz", "mez", "mak", "mek", "ken", "lık", "lik", "luk", "lük", "sız", "siz", "suz", "süz", "dık", "dik", "duk", "dük", "tık", "tik",
          "tuk", "tük", "yla", "yle", "da", "de", "ta", "te", "ın", "in", "un", "ün", "la", "le", "ki", "ım", "im", "um", "üm", "dı", "di",
          "du", "dü", "tı", "ti", "tu", "tü", "ar", "er", "ır", "ir", "ur", "ür", "ma", "me", "sa", "se", "lı", "li", "lu", "lü", "cı", "ci",
          "cu", "cü", "çı", "çi", "çu", "çü", "ca", "ce", "ça", "çe", "ıp", "ip", "up", "üp", "mı", "mi", "mu", "mü",
          "a", "e", "ı", "i", "u", "ü", "m", "n", "k", "y", "s", "r", "z"]
UR_SUF = ["یاں", "یوں", "ئیں", "ئیے", "یے", "یں", "وں", "ؤں", "تا", "تی", "تے", "نا", "نی", "نے", "یا", "ئی", "ئے", "ے", "ی", "ا", "و", "ؤ"]
AR_PRE = ["وال", "بال", "فال", "كال", "لل", "ال", "و", "ف", "ب", "ل", "ك", "س"]
AR_SUF = ["هما", "كما", "ها", "هم", "هن", "كم", "كن", "نا", "ني", "ات", "ون", "ين", "ان", "وا", "تم", "ه", "ك", "ي", "ت", "ا", "و"]


def norm(t):
    t = t.replace('’', "'").replace('‘', "'")
    t = t.replace('I', 'ı').replace('İ', 'i').lower() if lang == 'tr' else t.lower()
    if lang in ('ar', 'ur'):
        t = re.sub('[ً-ٰٟـ]', '', t)
    if lang == 'ar':
        t = re.sub('[أإآٱ]', 'ا', t).replace('ى', 'ي')
    if lang == 'ur':
        t = t.replace('ي', 'ی').replace('ى', 'ی').replace('ك', 'ک').replace('ۓ', 'ئے')
    return t


def raw_tokens(t):
    if lang == 'en':
        return [x.strip("'") for x in re.findall(r"(?:[^\W\d_]|')+", t) if x.strip("'")]
    if lang == 'fr':
        return re.findall(r"[^\W\d_]+'?", t)
    return re.findall(r'[^\W\d_]+', t)


def tr_chain(rest, memo):
    if not rest:
        return True
    if rest not in memo:
        memo[rest] = any(rest.startswith(s) and tr_chain(rest[len(s):], memo) for s in TR_SUF)
    return memo[rest]


def cands(t):
    out = [t]
    if lang == 'en':
        if t.endswith("'s"): out.append(t[:-2])
        if t.endswith('ies') and len(t) > 4: out.append(t[:-3] + 'y')
        if t.endswith('es') and len(t) > 3: out.append(t[:-2])
        if t.endswith('s') and len(t) > 2: out.append(t[:-1])
        if t.endswith('ied') and len(t) > 4: out.append(t[:-3] + 'y')
        if t.endswith('ed') and len(t) > 3:
            out += [t[:-2], t[:-1]]
            if len(t) > 4 and t[-3] == t[-4]: out.append(t[:-3])
        if t.endswith('ing') and len(t) > 4:
            out += [t[:-3], t[:-3] + 'e']
            if len(t) > 5 and t[-4] == t[-5]: out.append(t[:-4])
        if t.endswith('er') and len(t) > 4: out += [t[:-2], t[:-1]]
        if t.endswith('est') and len(t) > 5: out += [t[:-3], t[:-2]]
    elif lang == 'es':
        if t.endswith('es') and len(t) > 3: out.append(t[:-2])
        if t.endswith('s') and len(t) > 2: out.append(t[:-1])
        base = t[:-1] if t.endswith('s') and len(t) > 2 else t
        if base.endswith('a') and len(base) > 2: out.append(base[:-1] + 'o')
    elif lang == 'fr':
        if (t.endswith('s') or t.endswith('x')) and len(t) > 2: out.append(t[:-1])
        base = t[:-1] if (t.endswith('s') or t.endswith('x')) and len(t) > 2 else t
        if base.endswith('e') and len(base) > 2: out.append(base[:-1])
        if base.endswith('ne') and len(base) > 3: out.append(base[:-2])
        if base.endswith('euse') and len(base) > 5: out.append(base[:-4] + 'eux')
        if base.endswith('ve') and len(base) > 3: out.append(base[:-2] + 'f')
    elif lang == 'ar':
        s = [t]
        for _ in range(2):
            for x in list(s):
                for p in AR_PRE:
                    if x.startswith(p) and len(x) - len(p) >= 2 and x[len(p):] not in s: s.append(x[len(p):])
        for _ in range(2):
            for x in list(s):
                for q in AR_SUF:
                    if x.endswith(q) and len(x) - len(q) >= 2 and x[:-len(q)] not in s: s.append(x[:-len(q)])
        for x in list(s):
            if x.endswith('ت') and len(x) > 2: s.append(x[:-1] + 'ة')
            if x.endswith('ة') and len(x) > 2: s.append(x[:-1])
        for x in list(s):
            if len(x) >= 3 and x[0] in 'اتن': s.append('ي' + x[1:])
        out += s[1:]
    elif lang == 'tr':
        for n in range(len(t) - 1, 0, -1):
            stem, rest = t[:n], t[n:]
            if not tr_chain(rest, {}): continue
            out.append(stem)
            soft = {'b': 'p', 'c': 'ç', 'd': 't', 'ğ': 'k', 'g': 'k'}.get(stem[-1])
            if soft: out.append(stem[:-1] + soft)
            if rest.startswith(('iyor', 'ıyor', 'uyor', 'üyor')): out += [stem + 'e', stem + 'a']
    elif lang == 'ur':
        for s in UR_SUF:
            if t.endswith(s) and len(t) > len(s):
                st = t[:-len(s)]
                out += [st, st + 'ا', st + 'ی', st + 'ہ']
    elif lang == 'ms':
        bases = [t]
        for suf in ["nya", "lah", "kah", "pun", "kan", "an", "ku", "mu", "i"]:
            if t.endswith(suf) and len(t) - len(suf) >= 3: bases.append(t[:-len(suf)])
        for b in list(bases):
            if b.startswith('meny') and len(b) > 5: bases.append('s' + b[4:])
            elif b.startswith('meng') and len(b) > 5: bases += [b[4:], 'k' + b[4:]]
            elif b.startswith('mem') and len(b) > 4: bases += [b[3:], 'p' + b[3:]]
            elif b.startswith('men') and len(b) > 4: bases += [b[3:], 't' + b[3:]]
            if b.startswith('me') and len(b) > 4: bases.append(b[2:])
            for p in ["ber", "ter", "be", "di", "ke", "se", "pe"]:
                if b.startswith(p) and len(b) - len(p) >= 3: bases.append(b[len(p):])
        out += bases[1:]
    return out


def read(path, cols):
    rows = []
    with open(path, encoding='utf-8') as f:
        for line in f.read().split('\n')[1:]:
            if line.strip() and not line.startswith('#'):
                p = line.rstrip('\r').split('\t')
                rows.append(dict(zip(cols, p + [''] * (len(cols) - len(p)))))
    return rows


words = read(os.path.join(root, 'content', 'wordlists', f'{lang}.words.tsv'), WORD_COLS)
examples = read(os.path.join(root, 'content', 'wordlists', f'{lang}.examples.tsv'), ['rank', 'sentence', 'en', 'ar', 'names'])
known, keys_of, zh = {}, {}, {}
for w in words:
    extra = dict(x.strip().split('=', 1) for x in w['extra'].split(';') if '=' in x)
    variants = [w['term']] + [f.strip() for f in w['forms'].split(',') if f.strip()]
    if 'stem' in extra: variants.append(extra['stem'])
    if lang == 'tr' and w['pos'] == 'verb' and w['term'].endswith(('mek', 'mak')) and len(w['term']) > 4: variants.append(w['term'][:-3])
    if lang == 'ar': variants += [extra[k] for k in ('plural', 'present') if k in extra]
    if lang == 'ur':
        if 'plural' in extra: variants.append(extra['plural'])
        if w['pos'] == 'verb' and w['term'].endswith('نا') and len(w['term']) > 2: variants.append(w['term'][:-2])
    keys = set()
    for v in variants:
        nv = norm(v)
        if not nv.strip(): continue
        if lang == 'zh':
            zh[nv] = min(zh.get(nv, 10 ** 9), int(w['rank']))
            keys.add((nv,))
            continue
        ts = raw_tokens(nv)
        for t in ts: known[t] = min(known.get(t, 10 ** 9), int(w['rank']))
        if ts: keys.add(tuple(ts))
    keys_of[w['rank']] = (w['term'], keys)
zh_max = max(map(len, zh), default=1)


def strip_names(s, names):
    for n in sorted([norm(x.strip()) for x in names.split(',') if x.strip()], key=len, reverse=True):
        s = s.replace(n, ' ') if lang == 'zh' else re.sub(r"(?<![^\W\d_])" + re.escape(n) + r"('[^\W\d_]+)?(?![^\W\d_])", ' ', s)
    return s


def tokens(sentence, names):
    s = strip_names(norm(sentence), names)
    if lang != 'zh': return raw_tokens(s)
    out, i = [], 0
    while i < len(s):
        if not ('一' <= s[i] <= '鿿'): i += 1; continue
        for ln in range(min(zh_max, len(s) - i), 0, -1):
            if s[i:i + ln] in zh: out.append(s[i:i + ln]); i += ln; break
        else: out.append(s[i]); i += 1
    return out


unknown, missing, via, late = {}, [], set(), {}
for e in examples:
    rank = int(e['rank'])
    if only_level and (rank - 1) // 300 + 1 != only_level: continue
    ts = tokens(e['sentence'], e['names'])
    for t in ts:
        if lang == 'zh':
            hits = [t] if t in zh else []
            r = zh.get(t)
        else:
            hits = [c for c in cands(t) if c in known]
            r = min((known[c] for c in hits), default=None)
        if not hits:
            unknown.setdefault(t, []).append(rank)
        else:
            if r > rank + 300: late.setdefault(t, []).append(rank)
            if lang != 'zh' and t not in known: via.add(f'{t}->{hits[0]}')
    term, keys = keys_of[e['rank']]
    if lang == 'zh':
        s = strip_names(norm(e['sentence']), e['names'])
        ok = any(k[0] in s for k in keys)
    else:
        ok = any((len(k) == 1 and any(k[0] in cands(t) for t in ts)) or
                 (len(k) > 1 and any(all(k[i] in cands(ts[j + i]) for i in range(len(k))) for j in range(len(ts) - len(k) + 1)))
                 for k in keys)
    if not ok: missing.append((rank, term, e['sentence']))
for t, r in sorted(unknown.items()): print('unknown', t, r)
for t, r in sorted(late.items()): print('beyond rank+300', t, r)
for m in missing: print('word not found', m)
print(f'{lang}: {len(unknown)} unknown, {len(late)} beyond rank+300, {len(missing)} examples without their word, {len(via)} via rules')
if verbose: print('  '.join(sorted(via)))
sys.exit(1 if unknown or missing else 0)

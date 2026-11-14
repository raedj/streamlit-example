#!/usr/bin/env python3
"""AI content check: several AI models judge the built-in content, and the owner decides what to hide.

AI models can flag mistakes but cannot certify content: a model checking text shares many of the blind spots
of the model that wrote it. So an item is only called "disputed" when models from at least two different
vendors mark it wrong, and nothing is hidden until the owner runs `apply` after reading the report.

Usage (from the project root):
  python tools/flag_content.py items <lang>
      counts the items that can be checked (lesson content and the word list)
  python tools/flag_content.py check <lang> --model PROVIDER:MODEL [--model ...] [--source curriculum|wordlist|all]
                                      [--level N] [--limit N] [--batch N] [--dry-run] [--yes]
      sends items to each model and stores the answers in content/review/ai_flags/; items already checked by
      that model, with the same text, are skipped, so an interrupted run can be resumed
  python tools/flag_content.py report <lang> [--min-vendors N]
      writes content/review/ai_flags/<lang>-report.tsv, disputed items first; report files that testers exported
      from the app and the owner saved in content/review/tester_reports/ are counted in the same table
  python tools/flag_content.py apply <lang> [--min-vendors N] [--yes]
      hides the disputed items in the app (status "ai_disputed" in content/review/status.tsv); rebuild after
  python tools/flag_content.py restore <lang> <item_id>
      shows a hidden item again

Providers and the environment variable holding each key (keys are read from the environment only, never
written to a file or printed):
  anthropic   ANTHROPIC_API_KEY    e.g. anthropic:claude-opus-5-5
  openai      OPENAI_API_KEY       e.g. openai:gpt-5
  gemini      GEMINI_API_KEY       e.g. gemini:gemini-2.5-pro
  openrouter  OPENROUTER_API_KEY   e.g. openrouter:anthropic/claude-opus-5-5 (one key, models of many vendors)
The vendor of an OpenRouter model is the part before "/". Only built-in content is sent: no learner data.

Lesson items come from content/review/items/<lang>.curriculum.tsv, which ContentItemsFileTest keeps equal to
the content in the app; word-list items come from content/wordlists/<lang>.{words,examples}.tsv.
"""
import argparse, csv, datetime, hashlib, json, os, re, sys, urllib.error, urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANGS = {'en': 'English', 'zh': 'Mandarin Chinese (simplified characters, pinyin with tone marks)', 'ms': 'Malay (Bahasa Melayu, Malaysia)',
         'es': 'Spanish (Spain)', 'fr': 'French (France)', 'tr': 'Turkish', 'ur': 'Urdu (Nastaliq script)'}
VERDICTS = ('ok', 'doubtful', 'wrong')
DISPUTED = 'ai_disputed'
KEY_ENV = {'anthropic': 'ANTHROPIC_API_KEY', 'openai': 'OPENAI_API_KEY', 'gemini': 'GEMINI_API_KEY', 'openrouter': 'OPENROUTER_API_KEY'}
STATUS_COLS = ['language', 'item_id', 'status', 'reviewer', 'date', 'note']
csv.field_size_limit(10_000_000)


def path(*parts):
    return os.path.join(ROOT, *parts)


def read_tsv(p):
    with open(p, encoding='utf-8', newline='') as f:
        return list(csv.DictReader(f, delimiter='\t', quoting=csv.QUOTE_NONE))


def write_tsv(p, cols, rows):
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, 'w', encoding='utf-8', newline='') as f:
        w = csv.DictWriter(f, cols, delimiter='\t', quoting=csv.QUOTE_NONE, escapechar='\\', lineterminator='\n')
        w.writeheader()
        for r in rows:
            w.writerow({c: clean(str(r.get(c, ''))) for c in cols})


def clean(s):
    return re.sub(r'[\t\r\n]+', ' ', s)


def digest(text):
    return hashlib.sha1(text.encode('utf-8')).hexdigest()[:12]


# ---------- items ----------

def curriculum_items(lang):
    p = path('content', 'review', 'items', f'{lang}.curriculum.tsv')
    if not os.path.exists(p):
        # Languages without a lesson course (es, fr, tr, ur) only have word-list items.
        print(f'note: {p} is missing; using word-list items only (run the core tests to write it)', file=sys.stderr)
        return []
    return [dict(id=r['id'], kind=r['kind'], level=r['level'], text=r['text'], source='curriculum') for r in read_tsv(p)]


def wordlist_items(lang):
    words = read_tsv(path('content', 'wordlists', f'{lang}.words.tsv'))
    examples = {}
    for e in read_tsv(path('content', 'wordlists', f'{lang}.examples.tsv')):
        examples.setdefault(int(e['rank']), []).append(e)
    items = []
    for w in words:
        rank = int(w['rank'])
        level = f"L{(rank - 1) // 300 + 1}"
        text = f"term: {w['term']} ¦ reading: {w['pron']} ¦ part_of_speech: {w['pos']} ¦ meaning: en: {w['en']} | ar: {w['ar']}"
        if w.get('extra'):
            text += f" ¦ details: {w['extra']}"
        items.append(dict(id=w['id'], kind='list_word', level=level, text=clean(text), source='wordlist'))
        for n, e in enumerate(examples.get(rank, []), 1):
            items.append(dict(id=f"{w['id']}#ex{n}", kind='list_example', level=level, source='wordlist',
                              text=clean(f"example of the word {w['term']}: {e['sentence']} ¦ meaning: en: {e['en']} | ar: {e['ar']}")))
    return items


def load_items(lang, source='all'):
    items = []
    if source in ('curriculum', 'all'):
        items += curriculum_items(lang)
    if source in ('wordlist', 'all'):
        items += wordlist_items(lang)
    for it in items:
        # The level is sent and judged too, so a level change must send the item again.
        it['hash'] = digest(it['level'] + '\t' + it['text'])
    return items


# ---------- models ----------

def parse_model(spec):
    provider, _, model = spec.partition(':')
    if provider not in KEY_ENV or not model:
        sys.exit(f'--model must be PROVIDER:MODEL with PROVIDER one of {sorted(KEY_ENV)}, got {spec!r}')
    return provider, model


def vendor(label):
    provider, model = label.split(':', 1)
    return model.split('/', 1)[0] if provider == 'openrouter' and '/' in model else provider


def system_prompt(lang):
    return (
        f"You check the built-in content of a language-learning app. Learners speak Arabic or English and learn {LANGS[lang]}. "
        "Each item gives the target-language text with its English (en) and Arabic (ar) meanings; check both meanings. "
        "For each item choose one verdict: \"ok\" when it is correct and natural; \"doubtful\" when it is probably acceptable but "
        "unnatural, ambiguous or you are not sure; \"wrong\" only for a clear error: a wrong meaning or translation in en or ar, a wrong "
        "reading, pinyin or tone, an ungrammatical sentence, an answer marked correct that is wrong, a wrong option that is also correct, "
        "or a level far from the one given. Do not mark style preferences or regional variants as wrong. "
        "Reply with JSON only: {\"results\": [{\"id\": \"...\", \"verdict\": \"ok|doubtful|wrong\", \"problem\": \"short, in English\", "
        "\"fix\": \"the corrected text, or empty\"}]}, one entry for every item id."
    )


def user_prompt(batch):
    return '\n\n'.join(f"id: {it['id']}\nkind: {it['kind']}\nlevel: {it['level']}\ncontent: {it['text']}" for it in batch)


def http_json(url, headers, body, timeout=180):
    req = urllib.request.Request(url, data=json.dumps(body).encode('utf-8'), headers={'content-type': 'application/json', **headers})
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            return json.loads(r.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        # The body explains the error; the request (with its key) is never printed.
        raise RuntimeError(f'HTTP {e.code}: {e.read().decode("utf-8", "replace")[:300]}') from None


def send(provider, model, system, user):
    key = os.environ.get(KEY_ENV[provider], '').strip()
    if not key:
        raise RuntimeError(f'{KEY_ENV[provider]} is not set')
    if provider == 'anthropic':
        r = http_json('https://api.anthropic.com/v1/messages', {'x-api-key': key, 'anthropic-version': '2023-06-01'},
                      {'model': model, 'max_tokens': 8000, 'system': system, 'messages': [{'role': 'user', 'content': user}]})
        return ''.join(b.get('text', '') for b in r.get('content', []))
    if provider == 'gemini':
        r = http_json(f'https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent', {'x-goog-api-key': key},
                      {'systemInstruction': {'parts': [{'text': system}]}, 'contents': [{'role': 'user', 'parts': [{'text': user}]}],
                       'generationConfig': {'responseMimeType': 'application/json'}})
        return ''.join(p.get('text', '') for p in r['candidates'][0]['content']['parts'])
    base = 'https://openrouter.ai/api/v1' if provider == 'openrouter' else 'https://api.openai.com/v1'
    r = http_json(f'{base}/chat/completions', {'authorization': f'Bearer {key}'},
                  {'model': model, 'messages': [{'role': 'system', 'content': system}, {'role': 'user', 'content': user}],
                   'response_format': {'type': 'json_object'}})
    return r['choices'][0]['message']['content']


def parse_answer(text, batch):
    """The verdicts for the batch's ids; anything malformed or unknown is left out and checked again next run."""
    m = re.search(r'\{.*\}', text, re.S)
    if not m:
        return []
    try:
        results = json.loads(m.group(0)).get('results', [])
    except (json.JSONDecodeError, AttributeError):
        return []
    ids = {it['id'] for it in batch}
    out = {}
    for r in results if isinstance(results, list) else []:
        if isinstance(r, dict) and r.get('id') in ids and r.get('verdict') in VERDICTS:
            out[r['id']] = dict(id=r['id'], verdict=r['verdict'], problem=clean(str(r.get('problem') or ''))[:400], fix=clean(str(r.get('fix') or ''))[:400])
    return list(out.values())


# ---------- stored answers ----------

def flags_file(lang, label):
    return path('content', 'review', 'ai_flags', f"{lang}.{re.sub(r'[^A-Za-z0-9.-]+', '_', label)}.jsonl")


def load_flags(lang):
    """{label: {item id: latest answer}} for every model that has answered."""
    d = path('content', 'review', 'ai_flags')
    out = {}
    if not os.path.isdir(d):
        return out
    for name in sorted(os.listdir(d)):
        if name.startswith(lang + '.') and name.endswith('.jsonl'):
            with open(os.path.join(d, name), encoding='utf-8') as f:
                for line in f:
                    if line.strip():
                        r = json.loads(line)
                        out.setdefault(r['model'], {})[r['id']] = r
    return out


def check(lang, models, source='all', level=None, limit=0, batch_size=20, dry_run=False, yes=False, sender=send):
    items = load_items(lang, source)
    if level:
        items = [it for it in items if it['level'] in (str(level), f'L{level}')]
    flags = load_flags(lang)
    plans = []
    for provider, model in models:
        label = f'{provider}:{model}'
        done = flags.get(label, {})
        todo = [it for it in items if done.get(it['id'], {}).get('hash') != it['hash']]
        plans.append((provider, model, label, todo[:limit] if limit else todo))
    for _, _, label, todo in plans:
        print(f'{label}: {len(todo)} items to check')
    if dry_run:
        first = next((t for *_, t in plans if t), [])
        print('\n--- system ---\n' + system_prompt(lang) + '\n--- first batch ---\n' + user_prompt(first[:batch_size]))
        return
    total = sum(len(t) for *_, t in plans)
    if total == 0:
        return
    if not yes and input(f'Send {total} item checks to the providers above? Each provider bills its own key. [y/N] ').strip().lower() != 'y':
        return
    today = datetime.date.today().isoformat()
    by_id = {it['id']: it for it in items}
    for provider, model, label, todo in plans:
        target = flags_file(lang, label)
        os.makedirs(os.path.dirname(target), exist_ok=True)
        answered = 0
        for i in range(0, len(todo), batch_size):
            batch = todo[i:i + batch_size]
            try:
                results = parse_answer(sender(provider, model, system_prompt(lang), user_prompt(batch)), batch)
            except Exception as e:  # one failed batch should not lose the others
                print(f'{label}: batch {i // batch_size + 1} failed: {e}')
                continue
            with open(target, 'a', encoding='utf-8') as f:
                for r in results:
                    f.write(json.dumps(dict(r, model=label, hash=by_id[r['id']]['hash'], date=today), ensure_ascii=False) + '\n')
            answered += len(results)
            print(f'{label}: {min(i + batch_size, len(todo))}/{len(todo)}', end='\r')
        print(f'{label}: {answered} of {len(todo)} items answered' + (' (run again for the rest)' if answered < len(todo) else ''))


# ---------- report and hiding ----------

def tester_reports(lang):
    """{item id: [notes]} from the files testers exported in the app (Settings → Your data), saved by the owner in
    content/review/tester_reports/. That folder is not committed: the notes are the testers' own words."""
    d = path('content', 'review', 'tester_reports')
    out = {}
    if not os.path.isdir(d):
        return out
    for name in sorted(os.listdir(d)):
        if name.endswith('.tsv'):
            for r in read_tsv(os.path.join(d, name)):
                if r.get('language') == lang and r.get('item_id'):
                    out.setdefault(r['item_id'], []).append(f"{r.get('issue', '')}: {r.get('note', '')}".strip(': '))
    return out


def summarize(lang, min_vendors=2):
    items = load_items(lang)
    flags = load_flags(lang)
    testers = tester_reports(lang)
    rows = []
    for it in items:
        answers = [(label, a[it['id']]) for label, a in flags.items() if it['id'] in a and a[it['id']]['hash'] == it['hash']]
        if not answers and it['id'] not in testers:
            continue
        wrong_vendors = sorted({vendor(label) for label, a in answers if a['verdict'] == 'wrong'})
        rows.append(dict(
            id=it['id'], kind=it['kind'], level=it['level'], text=it['text'],
            verdicts='; '.join(f"{label}={a['verdict']}" for label, a in answers),
            wrong_vendors=','.join(wrong_vendors),
            doubtful=sum(a['verdict'] == 'doubtful' for _, a in answers),
            problems=' || '.join(f"{label}: {a['problem']}" for label, a in answers if a['verdict'] != 'ok' and a['problem']),
            fixes=' || '.join(f"{label}: {a['fix']}" for label, a in answers if a['verdict'] != 'ok' and a['fix']),
            disputed='yes' if len(wrong_vendors) >= min_vendors else 'no',
            tester_reports=len(testers.get(it['id'], [])),
            tester_notes=' || '.join(testers.get(it['id'], [])),
        ))
    rows.sort(key=lambda r: (r['disputed'] != 'yes', -len(r['wrong_vendors'].split(',')) if r['wrong_vendors'] else 0,
                             -r['tester_reports'], -r['doubtful'], r['id']))
    return rows


def report(lang, min_vendors=2):
    rows = summarize(lang, min_vendors)
    hidden = {r['item_id'] for r in load_status() if r['language'] == lang and r['status'] == DISPUTED}
    for r in rows:
        r['hidden'] = 'yes' if r['id'] in hidden else 'no'
    target = path('content', 'review', 'ai_flags', f'{lang}-report.tsv')
    write_tsv(target, ['id', 'kind', 'level', 'disputed', 'hidden', 'wrong_vendors', 'doubtful', 'tester_reports', 'verdicts', 'problems', 'fixes',
                       'tester_notes', 'text'], rows)
    vendors = sorted({vendor(label) for label in load_flags(lang)})
    print(f'{sum(bool(r["verdicts"]) for r in rows)} items checked by {", ".join(vendors) or "no model"}, '
          f'{sum(r["tester_reports"] > 0 for r in rows)} reported by testers; '
          f'{sum(r["disputed"] == "yes" for r in rows)} disputed (wrong for {min_vendors}+ vendors), '
          f'{sum(bool(r["wrong_vendors"]) for r in rows)} marked wrong by at least one, '
          f'{sum(r["doubtful"] > 0 for r in rows)} doubtful for at least one')
    if len(vendors) < min_vendors:
        print(f'Only {len(vendors)} vendor(s) so far: nothing can be disputed until {min_vendors} vendors have checked the same items.')
    print(f'Report: {os.path.relpath(target, ROOT)}')
    return rows


def load_status():
    p = path('content', 'review', 'status.tsv')
    return read_tsv(p) if os.path.exists(p) else []


def save_status(rows):
    write_tsv(path('content', 'review', 'status.tsv'), STATUS_COLS, sorted(rows, key=lambda x: (x['language'], x['item_id'])))
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    import review_sheets
    review_sheets.root = ROOT
    review_sheets.STATUS = path('content', 'review', 'status.tsv')
    review_sheets.gen()


def apply(lang, min_vendors=2, yes=False):
    status = {(r['language'], r['item_id']): r for r in load_status()}
    disputed = [r for r in summarize(lang, min_vendors) if r['disputed'] == 'yes']
    new, kept = [], []
    for r in disputed:
        current = status.get((lang, r['id']))
        if current and current['status'] == 'reviewed':
            kept.append(r)  # a person checked it: their review wins
        elif not current or current['status'] != DISPUTED:
            new.append(r)
    for r in kept:
        print(f"kept (reviewed by a person): {r['id']}")
    if not new:
        print('Nothing new to hide.')
        return []
    for r in new:
        print(f"hide {r['id']} [{r['kind']}]: {r['problems'][:160]}")
    if not yes and input(f'Hide these {len(new)} items in the app? [y/N] ').strip().lower() != 'y':
        return []
    today = datetime.date.today().isoformat()
    for r in new:
        status[(lang, r['id'])] = dict(language=lang, item_id=r['id'], status=DISPUTED, reviewer='ai:' + r['wrong_vendors'].replace(',', '+'),
                                       date=today, note=r['problems'][:500])
    save_status(list(status.values()))
    print(f'{len(new)} items hidden. Rebuild the app; `restore` shows an item again.')
    return new


def restore(lang, item_id):
    rows = load_status()
    keep = [r for r in rows if not (r['language'] == lang and r['item_id'] == item_id and r['status'] == DISPUTED)]
    if len(keep) == len(rows):
        sys.exit(f'{item_id} is not hidden')
    save_status(keep)
    print(f'{item_id} will show again after a rebuild.')


def main(argv=None):
    # Windows consoles default to a legacy code page that cannot print IPA or Chinese; found in a dry run on 2026-11-07.
    for stream in (sys.stdout, sys.stderr):
        if hasattr(stream, 'reconfigure'):
            stream.reconfigure(encoding='utf-8', errors='replace')
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument('command', choices=['items', 'check', 'report', 'apply', 'restore'])
    p.add_argument('lang', choices=sorted(LANGS))
    p.add_argument('item_id', nargs='?')
    p.add_argument('--model', action='append', default=[])
    p.add_argument('--source', choices=['curriculum', 'wordlist', 'all'], default='all')
    p.add_argument('--level', type=int)
    p.add_argument('--limit', type=int, default=0)
    p.add_argument('--batch', type=int, default=20)
    p.add_argument('--min-vendors', type=int, default=2)
    p.add_argument('--dry-run', action='store_true')
    p.add_argument('--yes', action='store_true')
    a = p.parse_args(argv)
    if a.min_vendors < 2:
        sys.exit('--min-vendors must be at least 2: one model alone does not decide what is hidden')
    if a.command == 'items':
        items = load_items(a.lang)
        kinds = {}
        for it in items:
            kinds[it['kind']] = kinds.get(it['kind'], 0) + 1
        print(f'{len(items)} items: ' + ', '.join(f'{k} {v}' for k, v in sorted(kinds.items())))
    elif a.command == 'check':
        if not a.model:
            sys.exit('give at least one --model PROVIDER:MODEL, ideally two or more from different vendors')
        check(a.lang, [parse_model(m) for m in a.model], a.source, a.level, a.limit, a.batch, a.dry_run, a.yes)
    elif a.command == 'report':
        report(a.lang, a.min_vendors)
    elif a.command == 'apply':
        apply(a.lang, a.min_vendors, a.yes)
    elif a.command == 'restore':
        if not a.item_id:
            sys.exit('restore needs an item id')
        restore(a.lang, a.item_id)


if __name__ == '__main__':
    main()

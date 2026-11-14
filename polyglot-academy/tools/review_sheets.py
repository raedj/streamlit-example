#!/usr/bin/env python3
"""Human review of the word lists: sheets for reviewers, merging their results, and the status in the app.

Usage (from the project root):
  python3 tools/review_sheets.py export <lang> [<level>]   writes content/review/sheets/<lang>[-L<level>].tsv
  python3 tools/review_sheets.py merge <sheet.tsv>          adds the filled rows to content/review/status.tsv
  python3 tools/review_sheets.py gen                        regenerates the Kotlin status table

A sheet has one row per word and one per example. Reviewers fill four columns:
  status    reviewed | needs_fix   (leave empty to skip the row)
  correction  the corrected text, when needs_fix
  reviewer  name or initials of a qualified speaker
  date      YYYY-MM-DD
content/review/status.tsv is the record of what was reviewed, by whom and when; the app shows an item
as reviewed only when it is listed there as "reviewed". Corrections are applied to the word lists by
hand (then this file is updated), so nothing changes in the content without a person deciding it.
"""
import csv, datetime, os, re, sys

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WL = os.path.join(root, 'content', 'wordlists')
REVIEW = os.path.join(root, 'content', 'review')
STATUS = os.path.join(REVIEW, 'status.tsv')
STATUS_COLS = ['language', 'item_id', 'status', 'reviewer', 'date', 'note']
SHEET_COLS = ['item_id', 'kind', 'level', 'text', 'pron', 'en', 'ar', 'status', 'correction', 'reviewer', 'date', 'note']
LANGS = ['en', 'zh', 'es', 'fr', 'ar', 'tr', 'ur', 'ms']
STATUSES = {'reviewed', 'needs_fix'}


def read_tsv(path):
    with open(path, encoding='utf-8', newline='') as f:
        return list(csv.DictReader(f, delimiter='\t', quoting=csv.QUOTE_NONE))


def write_tsv(path, cols, rows):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='') as f:
        w = csv.DictWriter(f, cols, delimiter='\t', quoting=csv.QUOTE_NONE, escapechar='\\', lineterminator='\n')
        w.writeheader()
        for r in rows:
            w.writerow({c: r.get(c, '') for c in cols})


def export(lang, level=None):
    if lang not in LANGS:
        sys.exit(f'unknown language {lang}')
    words = read_tsv(os.path.join(WL, f'{lang}.words.tsv'))
    examples = read_tsv(os.path.join(WL, f'{lang}.examples.tsv'))
    by_rank = {}
    for e in examples:
        by_rank.setdefault(int(e['rank']), []).append(e)
    rows = []
    for w in words:
        rank = int(w['rank'])
        lvl = (rank - 1) // 300 + 1
        if level and lvl != level:
            continue
        rows.append(dict(item_id=w['id'], kind='word', level=lvl, text=w['term'], pron=w['pron'], en=w['en'], ar=w['ar']))
        for n, e in enumerate(by_rank.get(rank, []), 1):
            rows.append(dict(item_id=f"{w['id']}#ex{n}", kind='example', level=lvl, text=e['sentence'], en=e['en'], ar=e['ar']))
    name = f'{lang}-L{level}.tsv' if level else f'{lang}.tsv'
    target = os.path.join(REVIEW, 'sheets', name)
    write_tsv(target, SHEET_COLS, rows)
    print(f'{len(rows)} rows written to {os.path.relpath(target, root)}')


def load_status():
    return read_tsv(STATUS) if os.path.exists(STATUS) else []


def merge(sheet):
    lang = os.path.basename(sheet).split('-')[0].split('.')[0]
    if lang not in LANGS:
        sys.exit(f'cannot tell the language from the file name {sheet}')
    current = {(r['language'], r['item_id']): r for r in load_status()}
    added = 0
    for n, r in enumerate(read_tsv(sheet), 2):
        status = (r.get('status') or '').strip()
        if not status:
            continue
        if status not in STATUSES:
            sys.exit(f'{sheet}:{n}: status must be one of {sorted(STATUSES)}')
        if not (r.get('reviewer') or '').strip():
            sys.exit(f'{sheet}:{n}: reviewer is required')
        try:
            datetime.date.fromisoformat((r.get('date') or '').strip())
        except ValueError:
            sys.exit(f'{sheet}:{n}: date must be YYYY-MM-DD')
        note = r.get('note') or ''
        if status == 'needs_fix' and r.get('correction'):
            note = (note + ' | correction: ' + r['correction']).strip(' |')
        current[(lang, r['item_id'])] = dict(language=lang, item_id=r['item_id'], status=status,
                                             reviewer=r['reviewer'].strip(), date=r['date'].strip(), note=note)
        added += 1
    write_tsv(STATUS, STATUS_COLS, sorted(current.values(), key=lambda x: (x['language'], x['item_id'])))
    print(f'{added} rows merged into {os.path.relpath(STATUS, root)}')
    gen()


def kotlin_str(s):
    return '"' + s.replace('\\', '\\\\').replace('"', '\\"').replace('$', '\\$') + '"'


def gen():
    rows = load_status()
    if not os.path.exists(STATUS):
        write_tsv(STATUS, STATUS_COLS, [])
    out = os.path.join(root, 'core', 'src', 'commonMain', 'kotlin', 'com', 'polyglot', 'academy', 'core', 'content', 'generated', 'GeneratedReviewStatus.kt')
    os.makedirs(os.path.dirname(out), exist_ok=True)
    lines = ['// GENERATED by tools/review_sheets.py from content/review/status.tsv — do not edit by hand.',
             'package com.polyglot.academy.core.content.generated', '',
             '/** language, item id, status, reviewer, date: one entry per reviewed item. */',
             'internal object GeneratedReviewStatus {',
             '    val rows: List<List<String>> = listOf(']
    for r in rows:
        lines.append('        listOf(' + ', '.join(kotlin_str(r[c]) for c in ['language', 'item_id', 'status', 'reviewer', 'date']) + '),')
    lines += ['    )', '}', '']
    with open(out, 'w', encoding='utf-8', newline='\n') as f:
        f.write('\n'.join(lines))
    print(f'{len(rows)} reviewed items in {os.path.relpath(out, root)}')


if __name__ == '__main__':
    if len(sys.argv) >= 3 and sys.argv[1] == 'export':
        export(sys.argv[2], int(sys.argv[3]) if len(sys.argv) > 3 else None)
    elif len(sys.argv) == 3 and sys.argv[1] == 'merge':
        merge(sys.argv[2])
    elif len(sys.argv) == 2 and sys.argv[1] == 'gen':
        gen()
    else:
        sys.exit(__doc__)

#!/usr/bin/env python3
"""A sheet for a native Arabic speaker to review the interface text (not the learning content).

Usage (from the project root):
  python3 tools/ui_review_sheet.py      writes content/review/sheets/ui-ar.tsv

One row per string from ui/strings/*.strings: the key, the English text for context, the Arabic
text, and empty columns for the reviewer (status ok | fix, correction, reviewer, date, note). Plural
entries keep their forms separated by "|" (Arabic has six: 0, 1, 2, 3-10, 11-99, 100+). Corrections
go back into ui/strings by hand, then tools/gen_strings.py regenerates the app's tables.
"""
import csv, glob, os

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
out = os.path.join(root, 'content', 'review', 'sheets', 'ui-ar.tsv')
cols = ['file', 'key', 'english', 'arabic', 'status', 'correction', 'reviewer', 'date', 'note']

rows = []
for path in sorted(glob.glob(os.path.join(root, 'ui', 'strings', '*.strings'))):
    with open(path, encoding='utf-8') as fh:
        for line in fh:
            line = line.rstrip('\r\n')
            if not line.strip() or line.lstrip().startswith('#'):
                continue
            parts = [p.strip() for p in line.split('¦')]
            if len(parts) != 3:
                continue
            rows.append([os.path.basename(path), parts[0], parts[1], parts[2], '', '', '', '', ''])

os.makedirs(os.path.dirname(out), exist_ok=True)
with open(out, 'w', encoding='utf-8', newline='') as fh:
    w = csv.writer(fh, delimiter='\t', lineterminator='\n')
    w.writerow(cols)
    w.writerows(rows)
print(f'wrote {len(rows)} strings to {os.path.relpath(out, root)}')

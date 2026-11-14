#!/usr/bin/env python3
"""Extracts stroke medians for the app's Chinese characters from Make Me a Hanzi's graphics.txt.

Usage (from the project root):  python3 tools/extract_strokes.py <path/to/graphics.txt>

Writes content/hanzi/strokes.tsv. The output is a modified version of graphics.txt and is
therefore licensed under the Arphic Public License (content/hanzi/ARPHICPL.TXT); the header of
the output file records what was changed. Characters are taken from the Mandarin word list
(terms, traditional forms and examples) and the hand-written Mandarin curriculum.

Each data line: character, TAB, strokes separated by ';', each stroke a median polyline of
"x,y" points separated by spaces, in Make Me a Hanzi's 1024-unit box converted to screen
orientation (y grows downwards, 0..1024).
"""
import glob, json, os, re, sys

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
HAN = re.compile(r'[一-鿿]')


def app_characters():
    chars = set()
    # Word list: term (column 2) and extra fields such as trad= (column 9); examples: sentence.
    for name, cols in (('zh.words.tsv', (2, 9)), ('zh.examples.tsv', (1,))):
        with open(os.path.join(root, 'content', 'wordlists', name), encoding='utf-8') as f:
            next(f)
            for line in f:
                cells = line.rstrip('\n').split('\t')
                for col in cols:
                    if len(cells) > col:
                        chars |= set(HAN.findall(cells[col]))
    for p in glob.glob(os.path.join(root, 'core', 'src', 'commonMain', 'kotlin', 'com', 'polyglot', 'academy', 'core', 'content', '*Mandarin*.kt')):
        chars |= set(HAN.findall(open(p, encoding='utf-8').read()))
    return chars


def main(graphics):
    wanted = app_characters()
    out, missing = {}, set(wanted)
    with open(graphics, encoding='utf-8') as f:
        for line in f:
            d = json.loads(line)
            c = d['character']
            if c not in wanted:
                continue
            strokes = []
            for median in d['medians']:
                # Make Me a Hanzi draws with transform scale(1,-1) translate(0,-900): screen y = 900 - y.
                strokes.append(' '.join(f'{round(x)},{round(900 - y)}' for x, y in median))
            out[c] = ';'.join(strokes)
            missing.discard(c)
    target = os.path.join(root, 'content', 'hanzi', 'strokes.tsv')
    os.makedirs(os.path.dirname(target), exist_ok=True)
    header = [
        '# Stroke-order medians for the Chinese characters used by Polyglot Academy.',
        '# Modified version of graphics.txt from Make Me a Hanzi (https://github.com/skishore/makemeahanzi,',
        '# commit 618dbab8a8ddefb958763c8b4afbaa741a4460de, 2018-10-16), which is derived from the fonts',
        '# Arphic PL KaitiM GB and Arphic PL UKai by Arphic Technology Co., Ltd.',
        '# Licensed under the Arphic Public License: see ARPHICPL.TXT in this directory.',
        '# Changes (Polyglot Academy, 2026-10-30): kept only the characters used by the app',
        '# (simplified and traditional forms); kept only the',
        '# stroke medians (the stroke outlines were removed); flipped the y axis to screen orientation',
        '# (y = 900 - y); rounded coordinates to whole units; changed the format from JSON lines to',
        '# tab-separated text.',
        '# This file may be copied, modified and redistributed by anyone under the Arphic Public License.',
    ]
    with open(target, 'w', encoding='utf-8', newline='\n') as f:
        f.write('\n'.join(header) + '\n')
        for c in sorted(out):
            f.write(f'{c}\t{out[c]}\n')
    print(f'{len(out)} of {len(wanted)} characters written to {os.path.relpath(target, root)} '
          f'({os.path.getsize(target)} bytes); missing from graphics.txt: {"".join(sorted(missing)) or "none"}')


if __name__ == '__main__':
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    main(sys.argv[1])

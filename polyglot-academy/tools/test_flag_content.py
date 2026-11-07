#!/usr/bin/env python3
"""Tests for flag_content.py with a fake model: no network, no keys. Run: python tools/test_flag_content.py"""
import json, os, sys, tempfile, unittest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import flag_content as fc

WORDS = 'rank\tid\tterm\tpos\ttopic\ten\tar\tpron\tforms\textra\tvocab_id\n1\tms_w_saya\tsaya\tpron\tpeople\tI\tأنا\t\t\t\t\n2\tms_w_makan\tmakan\tverb\tfood\teat\tيأكل\t\t\t\t\n'
EXAMPLES = 'rank\tsentence\ten\tar\tnames\n1\tSaya pelajar.\tI am a student.\tأنا طالب.\t\n'
ITEMS = 'id\tkind\tlevel\ttext\nms_hello\tword\tA1\tterm: helo ¦ meaning: en: hello | ar: مرحبًا\nms_x_q1\texercise\tA1\tquestion: ___ ¦ correct: x\n'


def fake(verdicts):
    """A model that answers every item with verdicts[id] (default ok)."""
    def send(provider, model, system, user):
        ids = [line[4:] for line in user.splitlines() if line.startswith('id: ')]
        return 'Sure:\n```json\n' + json.dumps({'results': [{'id': i, 'verdict': verdicts.get(i, 'ok'), 'problem': 'p' if verdicts.get(i) else '', 'fix': ''} for i in ids]}) + '\n```'
    return send


class FlagContentTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        fc.ROOT = self.tmp.name
        for rel, text in [('content/wordlists/ms.words.tsv', WORDS), ('content/wordlists/ms.examples.tsv', EXAMPLES),
                          ('content/review/items/ms.curriculum.tsv', ITEMS), ('content/review/status.tsv', '\t'.join(fc.STATUS_COLS) + '\n')]:
            os.makedirs(os.path.dirname(os.path.join(self.tmp.name, rel)), exist_ok=True)
            with open(os.path.join(self.tmp.name, rel), 'w', encoding='utf-8') as f:
                f.write(text)

    def tearDown(self):
        self.tmp.cleanup()

    def test_items_cover_the_lessons_the_words_and_their_examples(self):
        ids = [it['id'] for it in fc.load_items('ms')]
        self.assertEqual(['ms_hello', 'ms_x_q1', 'ms_w_saya', 'ms_w_saya#ex1', 'ms_w_makan'], ids)

    def test_one_vendor_alone_never_disputes_an_item(self):
        fc.check('ms', [('anthropic', 'm1'), ('openrouter', 'anthropic/m2')], yes=True, sender=fake({'ms_hello': 'wrong'}))
        rows = {r['id']: r for r in fc.summarize('ms')}
        self.assertEqual('anthropic', rows['ms_hello']['wrong_vendors'])
        self.assertEqual('no', rows['ms_hello']['disputed'])
        self.assertEqual([], fc.apply('ms', yes=True))

    def test_two_vendors_dispute_an_item_and_apply_hides_it_until_restored(self):
        fc.check('ms', [('anthropic', 'm1'), ('gemini', 'm2')], yes=True, sender=fake({'ms_hello': 'wrong', 'ms_w_makan': 'doubtful'}))
        rows = fc.report('ms')
        self.assertEqual('ms_hello', rows[0]['id'])
        self.assertEqual('yes', rows[0]['disputed'])
        self.assertEqual('no', next(r for r in rows if r['id'] == 'ms_w_makan')['disputed'])
        self.assertEqual(['ms_hello'], [r['id'] for r in fc.apply('ms', yes=True)])
        status = fc.load_status()
        self.assertEqual([('ms', 'ms_hello', 'ai_disputed', 'ai:anthropic+gemini')], [(r['language'], r['item_id'], r['status'], r['reviewer']) for r in status])
        with open(os.path.join(self.tmp.name, 'core/src/commonMain/kotlin/com/polyglot/academy/core/content/generated/GeneratedReviewStatus.kt'), encoding='utf-8') as f:
            self.assertIn('"ms_hello", "ai_disputed"', f.read())
        self.assertEqual([], fc.apply('ms', yes=True))
        fc.restore('ms', 'ms_hello')
        self.assertEqual([], fc.load_status())

    def test_a_persons_review_wins_over_the_models(self):
        with open(os.path.join(self.tmp.name, 'content/review/status.tsv'), 'a', encoding='utf-8') as f:
            f.write('ms\tms_hello\treviewed\tA.B.\t2026-11-01\t\n')
        fc.check('ms', [('anthropic', 'm1'), ('openai', 'm2')], yes=True, sender=fake({'ms_hello': 'wrong'}))
        self.assertEqual([], fc.apply('ms', yes=True))

    def test_answers_are_kept_and_only_new_or_changed_items_are_sent_again(self):
        calls = []
        def counting(*a):
            calls.append(a[3].count('id: '))
            return fake({})(*a)
        fc.check('ms', [('openai', 'm')], yes=True, sender=counting)
        fc.check('ms', [('openai', 'm')], yes=True, sender=counting)
        self.assertEqual([5], calls)
        with open(os.path.join(self.tmp.name, 'content/review/items/ms.curriculum.tsv'), 'w', encoding='utf-8') as f:
            f.write(ITEMS.replace('helo', 'halo'))
        fc.check('ms', [('openai', 'm')], yes=True, sender=counting)
        self.assertEqual([5, 1], calls)

    def test_tester_reports_are_counted_without_changing_what_is_disputed(self):
        d = os.path.join(self.tmp.name, 'content/review/tester_reports')
        os.makedirs(d)
        with open(os.path.join(d, 'tester1.tsv'), 'w', encoding='utf-8') as f:
            f.write('created_at_millis\tlanguage\titem_id\titem_text\tissue\tnote\n1\tms\tms_w_makan\tmakan\tTRANSLATION\tmeans to eat\n'
                    '2\ten\ten_hello\thello\tOTHER\tnot this language\n')
        rows = fc.summarize('ms')
        self.assertEqual([('ms_w_makan', 1, 'TRANSLATION: means to eat', 'no')], [(r['id'], r['tester_reports'], r['tester_notes'], r['disputed']) for r in rows])

    def test_malformed_or_unknown_answers_are_dropped(self):
        batch = [{'id': 'a'}, {'id': 'b'}]
        self.assertEqual([], fc.parse_answer('no json here', batch))
        got = fc.parse_answer('{"results": [{"id": "a", "verdict": "wrong", "problem": "x"}, {"id": "zz", "verdict": "ok"}, {"id": "b", "verdict": "maybe"}]}', batch)
        self.assertEqual([{'id': 'a', 'verdict': 'wrong', 'problem': 'x', 'fix': ''}], got)

    def test_a_failed_batch_does_not_stop_the_run(self):
        def broken(*a):
            raise RuntimeError('HTTP 500')
        fc.check('ms', [('openai', 'm')], yes=True, sender=broken)
        self.assertEqual({}, fc.load_flags('ms'))


if __name__ == '__main__':
    unittest.main()

"""Phase 2 regression checks: shipped data plus real Java/API integration.

Game interfaces supply controlled context; the renderer, resolvers, loader,
proposal summaries and dialogue pipeline are the production classes.
"""
import csv
import io
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import build

ROOT = Path(__file__).resolve().parents[2]
ORIGINAL = 'd5cd42288ab7fd0b194266d51b206ed1f43edc8c'


def historical(path):
    return subprocess.run(['git', 'show', ORIGINAL + ':' + path], cwd=ROOT,
                          capture_output=True, check=True).stdout.decode('utf-8-sig')


def leaves(node, path=''):
    if isinstance(node, dict):
        for key, value in node.items():
            yield from leaves(value, path + '/' + key.replace('~', '~0').replace('/', '~1'))
    elif isinstance(node, list):
        for i, value in enumerate(node):
            yield from leaves(value, path + '/' + str(i))
    else:
        yield path, node


class PresentationDataTests(unittest.TestCase):
    def test_dialogue_and_roster_mechanics_unchanged(self):
        inventory = json.loads((ROOT/'docs/immersion-audit/generated/string_inventory.json').read_text(encoding='utf-8'))['entries']
        for path in ('data/lords/dialog/dialog.json', 'data/lords/lords.json'):
            allowed = {e['locator'] for e in inventory if e['file'] == path and
                       e['surface'] in {'dialogue', 'dialogue_highlight', 'biography', 'fleet_name'}}
            before = dict(leaves(json.loads(historical(path))))
            after = dict(leaves(json.loads((ROOT/path).read_text(encoding='utf-8'))))
            # the only addition: the "ask for promotion" option, its answers and its slot in the question menu
            menu = '/default/lines/default/lines/optionSet_askQuestion/options/'
            added = after.keys() - before.keys()
            self.assertTrue(path.endswith('dialog.json') or not added, path)
            self.assertEqual({k for k in added if 'promotion' not in k and not k.startswith(menu)}, set(), path)
            self.assertEqual(before.keys() - after.keys(), set(), path)
            changed = {key for key in before if before[key] != after[key]}
            self.assertTrue(changed)
            self.assertEqual({k for k in changed - allowed if not k.startswith(menu)}, set(), path)

    def test_console_command_ids_and_numeric_settings_unchanged(self):
        for path, columns in [('data/console/commands.csv', {'help'}),
                              ('data/config/LunaSettings.csv', {'fieldName', 'fieldDescription', 'tab'})]:
            old = list(csv.reader(io.StringIO(historical(path))))
            new = list(csv.reader(io.StringIO((ROOT/path).read_text(encoding='utf-8'))))
            self.assertEqual(old[0], new[0])
            # new commands may be appended (PromotePlayer); existing rows keep their ids and syntax
            if path.endswith('commands.csv'):
                self.assertGreaterEqual(len(new), len(old))
            else:
                self.assertEqual(len(old), len(new))
            headers = old[0]
            for a, b in zip(old[1:], new[1:]):
                self.assertEqual(len(a), len(b))
                ignore = set(columns)
                if path.endswith('LunaSettings.csv') and len(a) > headers.index('fieldType'):
                    if a[headers.index('fieldType')] in ('Header', 'Text'): ignore.add('defaultValue')
                for i in range(len(a)):
                    if i >= len(headers) or headers[i] not in ignore: self.assertEqual(a[i], b[i], (path, a, i))

    def test_all_literal_catalog_references_exist(self):
        data = json.loads((ROOT/'data/lords/presentation/neutral.json').read_text(encoding='utf-8'))
        self.assertEqual(data['schemaVersion'], 1)
        messages = data['messages']
        for path in (ROOT/'src').rglob('*.java'):
            source = path.read_text(encoding='utf-8')
            for key in re.findall(r'Presentation\.(?:text|format)\("([^"]+)"', source):
                if key == 'policy.name.': continue  # enum suffix, checked below
                self.assertIn(key, messages, str(path))
        for policy in ('crown_authority', 'noble_authority', 'trade_law', 'feast_law'):
            self.assertIn('policy.name.' + policy, messages)
        self.assertTrue(all(isinstance(v, str) and v.strip() for v in messages.values()))

    def test_visible_dialogue_has_no_debug_fallback_or_medieval_generic_titles(self):
        data = json.loads((ROOT/'data/lords/dialog/dialog.json').read_text(encoding='utf-8'))
        inventory = json.loads((ROOT/'docs/immersion-audit/generated/string_inventory.json').read_text(encoding='utf-8'))['entries']
        visible = {e['locator'] for e in inventory if e['file'] == 'data/lords/dialog/dialog.json'
                   and e['surface'] in {'dialogue', 'dialogue_highlight'}}
        for path, value in leaves(data):
            if path not in visible or not isinstance(value, str): continue
            self.assertNotIn('(error)', value, path)
            # IDs such as %LORD_NAME and authored history remain supported.
            self.assertIsNone(re.search(r'\b(liege|fiefs?|demesne|lords?|lordship)\b', value, re.I), path)

    def test_existing_localization_placeholders_preserved(self):
        path = 'data/strings/strings.json'
        old = dict(leaves(json.loads(historical(path))))
        new = dict(leaves(json.loads((ROOT/path).read_text(encoding='utf-8'))))
        self.assertEqual(old.keys(), new.keys())
        for key in old:
            expected = old[key].count('$string')
            if key.endswith('/offensive_bombard_saturation'): expected = 1  # original typo #string
            self.assertEqual(expected, new[key].count('$string'), key)

    def test_dialogue_highlights_still_match_rewritten_sentences(self):
        data = json.loads((ROOT/'data/lords/dialog/dialog.json').read_text(encoding='utf-8'))
        def check(node):
            if isinstance(node, dict):
                if isinstance(node.get('line'), str) and isinstance(node.get('highlight'), dict):
                    highlights = node['highlight']['highlight']
                    if isinstance(highlights, str): highlights = [highlights]
                    for text in highlights: self.assertIn(text, node['line'])
                for value in node.values(): check(value)
            elif isinstance(node, list):
                for value in node: check(value)
        check(data)


@unittest.skipUnless(build.CONFIG.is_file(), 'Local Starsector/JDK configuration required')
class JavaPresentationTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temp = tempfile.TemporaryDirectory(prefix='starlords presentation ')
        cls.work = Path(cls.temp.name)
        cls.env = build.environment(build.CONFIG)
        (cls.work/'mod').mkdir()
        classes, _ = build.compile_sources(ROOT/'src', cls.work/'mod', cls.env['javac'],
                                          16, cls.env['jars'], cls.env['processors'])
        cls.classpath = os.pathsep.join(str(p) for p in [classes, *cls.env['jars'], cls.work])
        cls.java = cls.env['jdk']/'bin'/('java.exe' if os.name == 'nt' else 'java')
        result = subprocess.run([str(cls.env['javac']), '-encoding', 'UTF-8', '--release', '16',
                                 '-cp', cls.classpath, '-d', str(cls.work),
                                 str(ROOT/'tools/tests/java/PresentationChecks.java')],
                                capture_output=True, text=True, cwd=ROOT)
        if result.returncode: raise AssertionError(result.stdout + result.stderr)

    @classmethod
    def tearDownClass(cls):
        cls.temp.cleanup()

    def check(self, mode):
        result = subprocess.run([str(self.java), '-Djava.awt.headless=true', '-cp', self.classpath,
                                 'PresentationChecks', str(ROOT), mode], cwd=ROOT,
                                capture_output=True, text=True)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_exact_tokens_literal_values_and_context_validation(self): self.check('renderer')
    def test_binding_precedence_partial_legacy_and_unknown_factions(self): self.check('titles')
    def test_live_faction_commission_host_recipient_and_missing_people(self): self.check('context')
    def test_proposal_authority_market_names_and_every_proposal_type(self): self.check('proposals')
    def test_dialogue_line_option_hint_highlight_and_effect_order(self): self.check('dialogue')
    def test_schema_errors_and_absent_optional_faction(self): self.check('validation')


if __name__ == '__main__': unittest.main()

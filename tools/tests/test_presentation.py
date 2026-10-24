"""Phase 2/3 regression checks: shipped data plus real Java/API integration.

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
import preview_profiles
import sync_ranks

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
            # the only additions: the "ask for promotion" option and its answers, and the rave menu with its
            # dance-floor scenes. The question and greeting menus may be rearranged (courtship moved to the rave menu).
            menus = ('/default/lines/default/lines/optionSet_askQuestion/options/',
                     '/default/lines/default/lines/optionSet_greeting/options/')
            new_content = ('promotion', 'rave', 'dance_floor')
            added = after.keys() - before.keys()
            if path.endswith('lords.json'):
                # new commanders may be added (faction-mod support); the original roster stays as it was
                original = {k.split('/')[1] for k in before}
                added = {k for k in added if k.split('/')[1] in original}
            self.assertTrue(path.endswith('dialog.json') or not added, path)
            self.assertEqual({k for k in added if not any(w in k for w in new_content) and not k.startswith(menus)}, set(), path)
            self.assertEqual({k for k in before.keys() - after.keys() if not k.startswith(menus)}, set(), path)
            changed = {key for key in before.keys() & after.keys() if before[key] != after[key]}
            self.assertTrue(changed)
            self.assertEqual({k for k in changed - allowed if not k.startswith(menus)}, set(), path)
        # nothing was lost from the menus: every option the question menu had is still offered somewhere
        dialog = json.loads((ROOT/'data/lords/dialog/dialog.json').read_text(encoding='utf-8'))['default']['lines']['default']['lines']
        old_menu = json.loads(historical('data/lords/dialog/dialog.json'))['default']['lines']['default']['lines']['optionSet_askQuestion']['options']
        offered = set(dialog['optionSet_askQuestion']['options']) | set(dialog['optionSet_join_rave']['options'])
        self.assertLessEqual(set(old_menu), offered)

    def test_console_command_ids_and_numeric_settings_unchanged(self):
        for path, columns in [('data/console/commands.csv', {'help'}),
                              ('data/config/LunaSettings.csv', {'fieldName', 'fieldDescription', 'tab'})]:
            old = list(csv.reader(io.StringIO(historical(path))))
            new = list(csv.reader(io.StringIO((ROOT/path).read_text(encoding='utf-8'))))
            self.assertEqual(old[0], new[0])
            # new rows may be appended (PromotePlayer, the player faction profile); existing rows keep their ids and syntax
            self.assertGreaterEqual(len(new), len(old))
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
            literal = (r'Presentation\.(?:text|format)\("([^"]+)"',
                       r'Presentation\.(?:textFor|formatFor)\([^"\n]*?, "([^"]+)"',
                       r'\bwords\("([^"]+)"')
            for pattern in literal:
                for key in re.findall(pattern, source):
                    self.assertIn(key, messages, str(path))
        for policy in ('crown_authority', 'noble_authority', 'trade_law', 'feast_law'):
            self.assertIn('policy.name.' + policy, messages)
            self.assertIn('policy.description.' + policy, messages)
            for level in range(5): self.assertIn('policy.level.%s.%d' % (policy, level), messages)
        for level in ('v_low', 'low', 'medium', 'high', 'v_high'): self.assertIn('law_level.' + level, messages)
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
        moved = {k for k in old if re.fullmatch(r'/starlords_ui/laws_(tooltip|(crown_authority|noble_authority|trade_law|feast_law)(_[0-4]|_tooltip)?)', k)}
        moved |= {'/starlords_ui/' + k for k in ('campaign_start', 'campaign_end', 'fleet_campaign_follow_desc', 'fleet_campaign_lead_desc')}
        self.assertEqual(len(moved), 33)
        self.assertEqual(old.keys() - moved, new.keys())
        old = {k: v for k, v in old.items() if k not in moved}
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


class ProfileDataTests(unittest.TestCase):
    """profiles.json may only reword institutional messages, with the tokens their context supplies."""
    @classmethod
    def setUpClass(cls):
        load = lambda p: json.loads((ROOT/'data/lords/presentation'/p).read_text(encoding='utf-8'))
        cls.messages = load('neutral.json')['messages']
        cls.overridable = load('overridable_keys.json')['keys']
        cls.profiles = load('profiles.json')

    @staticmethod
    def tokens(text):
        return set(re.findall(r'\{[A-Za-z][A-Za-z0-9_.]*\}', text))

    def test_overridable_keys_exist_and_profiles_stay_inside_them(self):
        self.assertEqual(len(self.overridable), len(set(self.overridable)))
        for key in self.overridable: self.assertIn(key, self.messages)
        for name, profile in self.profiles['profiles'].items():
            self.assertNotEqual(name, 'default')
            for key, text in profile.get('messages', {}).items():
                self.assertIn(key, self.overridable, (name, key))
                self.assertTrue(text.strip(), (name, key))
                self.assertLessEqual(self.tokens(text), self.tokens(self.messages[key]), (name, key))
                # leading/trailing spaces are part of composed UI lines
                self.assertEqual(text[:1] == ' ', self.messages[key][:1] == ' ', (name, key))
                self.assertEqual(text[-1:] == ' ', self.messages[key][-1:] == ' ', (name, key))
                self.assertEqual(text.count('|'), self.messages[key].count('|'), (name, key))

    def test_bindings_and_inheritance_resolve(self):
        profiles = self.profiles['profiles']
        for faction, name in self.profiles['factions'].items():
            seen = []
            while name != 'default':
                self.assertIn(name, profiles, faction)
                self.assertNotIn(name, seen, faction)
                seen.append(name)
                name = profiles[name].get('extends', 'default')
        for name, profile in profiles.items():
            if 'titles' in profile:
                self.assertEqual(len(profile['titles']), 3, name)
                self.assertTrue(all(t.strip() for t in profile['titles']), name)

    def test_rave_and_tournament_stay_universal(self):
        # Julie's decision for Phase 3: only holdings, council, office, expedition and policies vary
        for name, profile in self.profiles['profiles'].items():
            for key in profile.get('messages', {}):
                self.assertFalse(key.startswith(('event.feast', 'tournament', 'policy.name.feast_law')), (name, key))

    def test_player_profile_setting_offers_real_profiles(self):
        rows = list(csv.reader(io.StringIO((ROOT/'data/config/LunaSettings.csv').read_text(encoding='utf-8'))))
        row = next(r for r in rows if r and r[0] == 'Other_playerFactionProfile')
        headers = rows[0]
        options = row[headers.index('secondaryValue')].split(',')
        self.assertEqual(options[0], 'default')
        self.assertEqual(row[headers.index('defaultValue')], 'default')
        for option in options[1:]: self.assertIn(option, self.profiles['profiles'], option)
        settings = json.loads((ROOT/'data/config/starlord_settings.json').read_text(encoding='utf-8'))
        self.assertIn(settings['playerFactionProfile'], options)

    def test_profile_wording_never_says_raid(self):
        # Julie's rule: pirates muster an armada under a Warmaster; no profile text uses "raid"
        for name, profile in self.profiles['profiles'].items():
            for key, text in profile.get('messages', {}).items():
                self.assertIsNone(re.search(r'raid', text, re.I), (name, key, text))

    def test_generated_rank_files_are_current(self):
        neutral, resolved = sync_ranks.resolver()
        for faction, names in resolved.items():
            text = sync_ranks.faction_file(faction).read_text(encoding='utf-8')
            data = json.loads(text[text.index('\n') + 1:])
            self.assertEqual([data['ranks']['ranks']['starlords_tier_%d' % i]['name'] for i in range(3)], names, faction)


class RaveSceneDataTests(unittest.TestCase):
    LEVELS = ('anything_goes', 'permissive', 'licensed', 'strict', 'banned')

    def test_raves_json_shape(self):
        data = json.loads((ROOT/'data/lords/presentation/raves.json').read_text(encoding='utf-8'))
        self.assertEqual(data['schemaVersion'], 1)
        factions = data['factions']
        self.assertTrue(isinstance(factions['default']['arrival'], str) and factions['default']['image'])
        self.assertTrue(factions['default']['crowd'])
        for faction, venue in factions.items():
            arrival = venue.get('arrival')
            texts = [arrival] if isinstance(arrival, str) else list((arrival or {}).values())
            if isinstance(arrival, dict):
                self.assertLessEqual(set(arrival), set(self.LEVELS), faction)
            for text in texts + venue.get('crowd', []):
                self.assertTrue(isinstance(text, str) and text.strip(), faction)
                self.assertNotIn('%', text, faction)  # scene text takes no tokens
        # Julie's decisions: the Church follows its licensing, the Path has its own scene, both in the teahouse
        self.assertEqual(set(factions['luddic_church']['arrival']), set(self.LEVELS))
        self.assertIsInstance(factions['luddic_path']['arrival'], str)
        self.assertEqual(factions['luddic_church']['image'], 'teahouse')

    @unittest.skipUnless(build.CONFIG.is_file(), 'Local Starsector configuration required')
    def test_every_venue_picture_exists_in_the_game(self):
        core = next(Path(p).parent for p in json.loads(build.CONFIG.read_text(encoding='utf-8'))['classpath']
                    if p.replace('\\', '/').endswith('starsector-core/*.jar'))
        settings = (core/'data/config/settings.json').read_text(encoding='utf-8', errors='replace')
        data = json.loads((ROOT/'data/lords/presentation/raves.json').read_text(encoding='utf-8'))
        for faction, venue in data['factions'].items():
            if 'image' in venue: self.assertIn('"%s":' % venue['image'], settings, faction)


class RaveMusicDataTests(unittest.TestCase):
    def test_music_set_named_in_code_is_defined_and_toggle_exists(self):
        source = (ROOT/'src/starlords/util/RaveMusic.java').read_text(encoding='utf-8')
        music_set = re.search(r'MUSIC_SET = "([^"]+)"', source).group(1)
        # Starsector's JSON allows # comments and trailing commas; strip them for Python's parser
        text = re.sub(r'(?m)^\s*#.*$', '', (ROOT/'data/config/sounds.json').read_text(encoding='utf-8'))
        sounds = json.loads(re.sub(r',(\s*[}\]])', r'\1', text))
        entries = sounds['music'][music_set]
        self.assertTrue(entries and all(entry.get('files') and entry.get('source') for entry in entries))
        rows = list(csv.reader(io.StringIO((ROOT/'data/config/LunaSettings.csv').read_text(encoding='utf-8'))))
        self.assertIn('Other_raveMusic', [r[0] for r in rows if r])
        settings = json.loads((ROOT/'data/config/starlord_settings.json').read_text(encoding='utf-8'))
        self.assertIs(settings['raveMusic'], True)


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
    def test_profile_inheritance_contexts_player_setting_and_titles(self): self.check('profiles')
    def test_profile_schema_errors_name_the_path(self): self.check('profile_validation')
    def test_rave_scenes_follow_licensing_and_fall_back(self): self.check('raves')
    def test_data_for_uninstalled_faction_mods_is_skipped_or_inert(self): self.check('absent_factions')

    def test_java_profile_resolution_matches_preview_tool(self):
        result = subprocess.run([str(self.java), '-Djava.awt.headless=true', '-cp', self.classpath,
                                 'PresentationChecks', str(ROOT), 'messages'], cwd=ROOT, capture_output=True,
                                encoding='utf-8')
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        line = next(l for l in result.stdout.splitlines() if l.startswith('MESSAGES '))
        java = json.loads(line[len('MESSAGES '):])
        keys, factions, resolve = preview_profiles.resolver()
        self.assertEqual(set(java), set(factions))
        for faction in factions:
            self.assertEqual(java[faction], {key: resolve(faction, key) for key in keys}, faction)

    def test_java_titles_match_generated_rank_names(self):
        result = subprocess.run([str(self.java), '-Djava.awt.headless=true', '-cp', self.classpath,
                                 'PresentationChecks', str(ROOT), 'ranks'], cwd=ROOT, capture_output=True, text=True)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        java = {}
        for line in result.stdout.splitlines():
            if line.startswith('RANKS '):
                faction, *names = line[6:].split('|')
                java[faction] = names
        _, python = sync_ranks.resolver()
        # the harness adds its own test factions; every shipped faction must agree
        self.assertLessEqual(python.keys(), java.keys())
        self.assertEqual({faction: java[faction] for faction in python}, python)


if __name__ == '__main__': unittest.main()

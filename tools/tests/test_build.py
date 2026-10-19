"""Meaningful build/compatibility regressions; no Starsector runtime required.

The Java fixture exercises the real compiler, archive writer and class parser.
It is not a stub implementation of Starsector and cannot build the mod itself.
"""
from __future__ import annotations

import copy
import contextlib
import io
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import build
from java_compat import (compare_classes, compare_contract, inspect_jar,
                         read_class, sha256, source_contract)

ROOT = Path(__file__).resolve().parents[2]


def test_javac():
    jdk = os.environ.get('STARLORDS_TEST_JDK')
    if not jdk and build.CONFIG.is_file():
        jdk = json.loads(build.CONFIG.read_text())['jdkHome']
    if jdk:
        return str(Path(jdk) / 'bin' / ('javac.exe' if os.name == 'nt' else 'javac'))
    return shutil.which('javac')


class BaselineTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.baseline = json.loads(build.BASELINE.read_text(encoding='utf-8'))

    @unittest.skipUnless(shutil.which('git'), 'Git needed to read the historical reference')
    def test_historical_baseline_matches_git_reference(self):
        # The working runtime JAR is now replaced on every successful build.
        # Test historical provenance without constraining the current source/JAR.
        original = subprocess.run(
            ['git', 'show', 'd5cd42288ab7fd0b194266d51b206ed1f43edc8c:jars/StarLords.jar'],
            cwd=ROOT, capture_output=True, check=True).stdout
        with tempfile.TemporaryDirectory() as directory:
            jar = Path(directory) / 'original.jar'
            jar.write_bytes(original)
            self.assertEqual(sha256(jar), self.baseline['jarSha256'])
            self.assertEqual(inspect_jar(jar), self.baseline['classes'])

    def test_all_packaged_classes_have_source_including_package_private_classes(self):
        self.assertEqual(self.baseline['classesWithoutSource'], [])
        helper = self.baseline['classes']['starlords/util/dialogControler/dialogRull/CurrentAction_base']
        self.assertEqual(helper['sourceFile'], 'DialogRule_currentAction.java')

    def test_optional_legacy_comparison_detects_saved_key_changes(self):
        before = self.baseline['sourceContract']
        after = copy.deepcopy(before)
        after['xstreamAliases']['person.Lord'] = 'RenamedLord'
        first = next(iter(after['memoryConstants']))
        after['memoryConstants'][first] = '$changed'
        path = next(iter(after['persistentDataKeys']))
        removed = after['persistentDataKeys'][path].pop()
        errors = compare_contract(before, after)
        self.assertTrue(any('person.Lord' in error for error in errors))
        self.assertTrue(any(first in error for error in errors))
        self.assertTrue(any(removed in error for error in errors))

    def test_missing_archive_class_and_newer_bytecode_are_rejected(self):
        before = self.baseline['classes']
        after = copy.deepcopy(before)
        name = 'starlords/person/Lord'
        after.pop(name)
        next(iter(after.values()))['major'] = self.baseline['maxClassMajor'] + 1
        errors = compare_classes(before, after, self.baseline['maxClassMajor'])
        self.assertIn(f'Class removed: {name}', errors)
        self.assertTrue(any('Unsupported bytecode' in e for e in errors))

    def test_malformed_class_and_duplicate_jar_entries_are_rejected(self):
        with self.assertRaises(ValueError):
            read_class(b'\xca\xfe\xba\xbe')
        with self.assertRaises(ValueError):
            read_class(b'not a class')
        with tempfile.TemporaryDirectory() as directory:
            target = Path(directory) / 'duplicate.jar'
            with zipfile.ZipFile(target, 'w') as archive:
                archive.writestr('duplicate', b'one')
                with self.assertWarns(UserWarning):
                    archive.writestr('duplicate', b'two')
            with self.assertRaisesRegex(ValueError, 'Duplicate archive'):
                inspect_jar(target)


@unittest.skipUnless(test_javac(), 'No JDK compiler available for real compiler tests')
class CompilerTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.temporary = tempfile.TemporaryDirectory(prefix='starlords compiler test ')
        cls.directory = Path(cls.temporary.name)
        cls.source = '''package starlords.fixture;
import java.util.List;
public class Commander {
    private List<String> holdings;
    private int standing;
    public String title() { return "Commander"; }
    public enum Office { NONE, COORDINATOR }
}
class Helper { }
'''
        cls.first = cls.compile(cls.source, 'first build')
        cls.second = cls.compile(cls.source, 'second build')
        cls.changed = cls.compile(cls.source.replace('List<String>', 'List<Integer>')
                                  .replace('private int standing', 'private transient int standing')
                                  .replace('String title()', 'String renamedTitle()')
                                  .replace('NONE, COORDINATOR', 'COORDINATOR, NONE'), 'changed build')

    @classmethod
    def tearDownClass(cls):
        cls.temporary.cleanup()

    @classmethod
    def compile(cls, source, name):
        # TemporaryDirectory provides and owns this verified test tree.
        work = cls.directory / name
        source_root = work / 'source'
        package = source_root / 'starlords/fixture'
        package.mkdir(parents=True)
        (package / 'Commander.java').write_text(source, encoding='utf-8')
        classes, _ = build.compile_sources(source_root, work, test_javac(), 16)
        jar = work / 'fixture.jar'
        build.package_classes(classes, jar)
        return jar

    def test_two_clean_builds_in_different_paths_are_byte_identical(self):
        self.assertEqual(sha256(self.first), sha256(self.second))
        with zipfile.ZipFile(self.first) as archive:
            self.assertEqual(archive.namelist(), sorted(archive.namelist()))
            self.assertTrue(all(e.date_time == (1980, 1, 1, 0, 0, 0) for e in archive.infolist()))

    def test_parser_recognizes_all_generated_classes_and_target_level(self):
        classes = inspect_jar(self.first)
        self.assertEqual(set(classes), {'starlords/fixture/Commander', 'starlords/fixture/Commander$Office', 'starlords/fixture/Helper'})
        self.assertTrue(all(c['major'] == 60 for c in classes.values()))
        self.assertEqual(compare_classes(classes, inspect_jar(self.second), 60), [])

    def test_real_serialization_and_public_api_regressions_are_detected(self):
        errors = compare_classes(inspect_jar(self.first), inspect_jar(self.changed), 60)
        for expected in ('holdings', 'standing', 'title', 'Enum constants removed/reordered'):
            self.assertTrue(any(expected in error for error in errors), (expected, errors))

    def test_failed_compilation_does_not_produce_an_artifact(self):
        with self.assertRaises(build.BuildError):
            self.compile('this is not Java', 'failed build')
        self.assertFalse((self.directory / 'failed build/fixture.jar').exists())

    def test_dependency_classes_cannot_be_bundled(self):
        work = self.directory / 'dependency build'
        source_root = work / 'source'
        source_root.mkdir(parents=True)
        (source_root / 'Dependency.java').write_text(
            'package org.example; public class Dependency {}', encoding='utf-8')
        classes, _ = build.compile_sources(source_root, work, test_javac(), 16)
        with self.assertRaisesRegex(build.BuildError, 'Unexpected class'):
            build.package_classes(classes, self.directory / 'rejected.jar')
        # Verification must also reject a separately supplied JAR that bypassed
        # our packager, even when all original mod classes are still present.
        injected = self.directory / 'injected.jar'
        shutil.copyfile(ROOT / 'jars/StarLords.jar', injected)
        with zipfile.ZipFile(injected, 'a') as archive:
            archive.write(classes / 'org/example/Dependency.class', 'org/example/Dependency.class')
        with self.assertRaisesRegex(build.BuildError, 'Unexpected class'):
            build.verify(injected)

    def make_mod_root(self, name):
        root = self.directory / name
        (root / 'data').mkdir(parents=True)
        (root / 'graphics').mkdir()
        (root / 'mod_info.json').write_text(json.dumps({
            'id': 'test_mod', 'modPlugin': 'starlords.fixture.Commander',
            'jars': ['jars/StarLords.jar']}), encoding='utf-8')
        return root

    def test_changed_save_schema_passes_default_verification(self):
        root = self.make_mod_root('changed schema')
        baseline = root / 'baseline.json'
        build.write_json(baseline, {'classes': inspect_jar(self.first),
                                    'sourceContract': source_contract(root)})
        with patch.object(build, 'ROOT', root), patch.object(build, 'BASELINE', baseline):
            self.assertEqual(build.verify(self.changed)['legacyCompatibility'], 'not required')
            with self.assertRaisesRegex(build.BuildError, 'holdings'):
                build.verify(self.changed, check_legacy=True)

    def test_runtime_checks_still_reject_newer_bytecode_and_missing_plugin(self):
        root = self.make_mod_root('runtime checks')
        newer = root / 'newer.jar'
        with zipfile.ZipFile(self.first) as source, zipfile.ZipFile(newer, 'w') as target:
            for name in source.namelist():
                data = bytearray(source.read(name))
                data[6:8] = (61).to_bytes(2, 'big')
                target.writestr(name, data)
        with patch.object(build, 'ROOT', root):
            with self.assertRaisesRegex(build.BuildError, 'Unsupported bytecode'):
                build.verify(newer)
            config = json.loads((root / 'mod_info.json').read_text())
            config['modPlugin'] = 'starlords.MissingPlugin'
            build.write_json(root / 'mod_info.json', config)
            with self.assertRaisesRegex(build.BuildError, 'Missing mod plugin'):
                build.verify(self.first)

    def test_installable_package_uses_new_jar_and_only_runtime_resources(self):
        root = self.make_mod_root('distribution')
        assets = {'data/config/example.json': '{}', 'graphics/icon.png': 'fixture',
                  'sounds/test.ogg': 'fixture', 'Starlords.version': '{}',
                  'LICENSE': 'fixture', 'TESTING.md': 'fixture', 'PRESENTATION.md': 'fixture'}
        excluded = {'build/dependencies/dependency.jar': 'exclude', 'src/Secret.java': 'exclude',
                    '.git/config': 'exclude', 'build.local.json': 'exclude',
                    'jars/StarLords.jar': 'old runtime'}
        for name, content in {**assets, **excluded}.items():
            path = root / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding='utf-8')
        first, second = root / 'first.zip', root / 'second.zip'
        build.package_mod(root, self.changed, first)
        build.package_mod(root, self.changed, second)
        self.assertEqual(sha256(first), sha256(second))
        with zipfile.ZipFile(first) as archive:
            prefix = 'Starlords_immersionPatches/'
            self.assertEqual(set(archive.namelist()),
                             {prefix + p for p in [*assets, 'mod_info.json', 'jars/StarLords.jar']})
            self.assertEqual(archive.read(prefix + 'jars/StarLords.jar'), self.changed.read_bytes())

    def test_success_publishes_runtime_and_failures_preserve_it(self):
        root = self.make_mod_root('publish workflow')
        output = root / 'build'
        runtime = root / 'jars/StarLords.jar'
        distribution = output / 'Starlords_immersionPatches.zip'
        source = root / 'src/starlords/fixture/Commander.java'
        source.parent.mkdir(parents=True)
        source.write_text(self.source, encoding='utf-8')
        runtime.parent.mkdir()
        runtime.write_bytes(b'old runtime')
        for script in ('build.py', 'java_compat.py'):
            target = root / 'tools' / script
            target.parent.mkdir(exist_ok=True)
            shutil.copyfile(ROOT / 'tools' / script, target)
        config = root / 'local.json'
        config.write_text('{}', encoding='utf-8')
        env = {'javac': test_javac(), 'release': 16, 'jars': [], 'processors': []}
        with patch.multiple(build, ROOT=root, OUTPUT=output, RUNTIME=runtime, DISTRIBUTION=distribution), \
                patch.object(build, 'environment', return_value=env), \
                patch.object(build, 'require_lock', return_value={}), \
                contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(build.main(['build', '--config', str(config)]), 0)
            self.assertEqual(runtime.read_bytes(), self.first.read_bytes())
            previous = runtime.read_bytes(), distribution.read_bytes()
            source.write_text('invalid Java', encoding='utf-8')
            self.assertEqual(build.main(['build', '--config', str(config)]), 1)
            self.assertEqual((runtime.read_bytes(), distribution.read_bytes()), previous)
            source.write_text(self.source.replace('"Commander"', '"Admiral"'), encoding='utf-8')
            compile_once = build.build_once
            calls = []

            def fail_second(env, target):
                calls.append(target)
                if len(calls) == 2:
                    raise build.BuildError('Simulated second build failure')
                return compile_once(env, target)

            with patch.object(build, 'build_once', side_effect=fail_second):
                self.assertEqual(build.main(['reproduce', '--config', str(config)]), 1)
            self.assertEqual(len(calls), 2)
            self.assertEqual((runtime.read_bytes(), distribution.read_bytes()), previous)


if __name__ == '__main__':
    unittest.main()

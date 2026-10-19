"""Offline, deterministic javac/JAR build; Python 3.10+ and a configured JDK.

Successful builds replace jars/StarLords.jar and create an installable ZIP.
Dependencies are local and are never bundled or used from an old Starlords JAR.
"""
from __future__ import annotations

import argparse
import glob
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import zipfile

from java_compat import (compare_classes, compare_contract, inspect_jar,
                         make_baseline, sha256, source_contract, source_fingerprint)

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / 'build'
BASELINE = ROOT / 'build-support' / 'baseline-0.3.78.json'
LOCK = ROOT / 'build.lock.json'
CONFIG = ROOT / 'build.local.json'
RUNTIME = ROOT / 'jars' / 'StarLords.jar'
DISTRIBUTION = OUTPUT / 'Starlords_immersionPatches.zip'
JAVA_RELEASE = 16
PROCESSOR = 'lombok.launch.AnnotationProcessorHider$AnnotationProcessor'
REQUIREMENTS = {
    'Starsector API': 'com/fs/starfarer/api/Global.class',
    'JSON': 'org/json/JSONObject.class',
    'LWJGL': 'org/lwjgl/input/Keyboard.class',
    'LWJGL vectors': 'org/lwjgl/util/vector/Vector2f.class',
    'Log4j': 'org/apache/log4j/Logger.class',
    'XStream': 'com/thoughtworks/xstream/XStream.class',
    'Nexerelin': 'exerelin/campaign/alliances/Alliance.class',
    'LunaLib': 'lunalib/lunaSettings/LunaSettings.class',
    'Console Commands': 'org/lazywizard/console/BaseCommand.class',
    'Second in Command': 'second_in_command/SCUtils.class',
    'JetBrains annotations': 'org/jetbrains/annotations/NotNull.class',
    'Lombok annotations': 'lombok/Getter.class',
}


class BuildError(Exception):
    pass


def read_json(path):
    try:
        return json.loads(path.read_text(encoding='utf-8-sig'))
    except (OSError, json.JSONDecodeError) as exc:
        raise BuildError(f'Cannot read {path}: {exc}') from exc


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')


def resolve_path(raw, relative_to):
    if not isinstance(raw, str) or any(c in raw for c in ('\n', '\r', '\0')):
        raise BuildError('Configured paths must be strings without line breaks or NULs')
    path = Path(os.path.expandvars(raw)).expanduser()
    return path if path.is_absolute() else relative_to / path


def expand_jars(patterns, config_dir, errors):
    paths = []
    if not isinstance(patterns, list) or not patterns:
        errors.append('Provide a nonempty list of JAR paths/globs')
        return paths
    for pattern in patterns:
        path = resolve_path(pattern, config_dir)
        matches = sorted(Path(p).resolve() for p in glob.glob(str(path), recursive=True))
        matches = [p for p in matches if p.is_file() and p.suffix.lower() == '.jar']
        if not matches:
            errors.append(f'No JAR files match: {path}')
        paths.extend(matches)
    return list(dict.fromkeys(paths))


def run(command, *, cwd=ROOT):
    return subprocess.run([str(arg) for arg in command], cwd=cwd, text=True,
                          encoding='utf-8', errors='replace', stdout=subprocess.PIPE,
                          stderr=subprocess.STDOUT, check=False)


def environment(config_path):
    config = read_json(config_path)
    errors = []
    for key in ('JAVA_TOOL_OPTIONS', '_JAVA_OPTIONS', 'JDK_JAVAC_OPTIONS'):
        if os.environ.get(key):
            errors.append(f'Unset {key} for a controlled compiler invocation')
    jdk = resolve_path(config.get('jdkHome', ''), config_path.parent).resolve()
    executable = jdk / 'bin' / ('javac.exe' if os.name == 'nt' else 'javac')
    if not executable.is_file():
        errors.append(f'JDK compiler not found: {executable}')
    release = config.get('javaRelease')
    expected_release = JAVA_RELEASE
    if type(release) is not int or release != expected_release:
        errors.append(f'javaRelease must be {expected_release}, the supported runtime target')
    jars = expand_jars(config.get('classpath'), config_path.parent, errors)
    processors = expand_jars(config.get('processorPath'), config_path.parent, errors)
    all_jars = list(dict.fromkeys(jars + processors))
    entries = {}
    for jar in all_jars:
        try:
            with zipfile.ZipFile(jar) as archive:
                names = set(archive.namelist())
                if any(n.startswith('starlords/') and n.endswith('.class') for n in names):
                    errors.append(f'Classpath must not include precompiled Starlords classes: {jar}')
                entries[jar] = names
        except (OSError, zipfile.BadZipFile) as exc:
            errors.append(f'Invalid dependency JAR {jar}: {exc}')
    for label, marker in REQUIREMENTS.items():
        providers = [str(path) for path, names in entries.items() if marker in names]
        if not providers:
            errors.append(f'Missing compile dependency: {label} ({marker})')
        elif len(providers) > 1:
            errors.append(f'Ambiguous {label} providers: {", ".join(providers)}')
    processor_entry = PROCESSOR.replace('.', '/') + '.class'
    if not any(processor_entry in entries.get(p, set()) for p in processors):
        errors.append('processorPath must contain the Lombok annotation processor')
    version = None
    if executable.is_file():
        result = run([executable, '-version'])
        version = result.stdout.strip()
        if result.returncode or not version.startswith('javac 17.'):
            errors.append(f'Use a JDK 17 toolchain; found {version!r}')
    if errors:
        raise BuildError('\n'.join(errors))
    return {'jdk': jdk, 'javac': executable, 'version': version, 'release': release,
            'jars': all_jars, 'processors': processors}


def input_lock(env):
    toolchain = {}
    for relative in ('release', 'lib/modules', 'bin/javac.exe' if os.name == 'nt' else 'bin/javac'):
        path = env['jdk'] / relative
        if not path.is_file():
            raise BuildError(f'Incomplete JDK: missing {path}')
        toolchain[relative] = sha256(path)
    return {'schemaVersion': 1, 'compiler': env['version'], 'javaRelease': env['release'],
            'compilerOptions': ['-encoding', 'UTF-8', '-g:source,lines,vars', '--release', str(env['release'])],
            'compilerJvmOptions': ['-Duser.language=en', '-Duser.country=US', '-Dfile.encoding=UTF-8'],
            'processor': PROCESSOR, 'toolchain': toolchain,
            'classpath': [{'name': p.name, 'sha256': sha256(p)} for p in env['jars']],
            'processorPath': [{'name': p.name, 'sha256': sha256(p)} for p in env['processors']]}


def require_lock(env):
    if not LOCK.is_file():
        raise BuildError('Dependency lock is missing. Run: python tools/build.py lock')
    current = input_lock(env)
    if current != read_json(LOCK):
        raise BuildError('Compiler/dependencies differ from build.lock.json. Review the change, then run lock explicitly.')
    return current


def argfile_value(value):
    # javac argument files, not shell command strings. Forward slashes preserve
    # Windows paths with spaces without backslash escape ambiguities.
    value = str(value).replace('\\', '/')
    if any(c in value for c in ('\n', '\r', '\0')):
        raise BuildError('Invalid compiler argument')
    return '"' + value.replace('"', '\\"') + '"'


def compile_sources(source_root, work, javac, release, jars=(), processors=()):
    sources = sorted(source_root.rglob('*.java'))
    if not sources:
        raise BuildError(f'No Java source files in {source_root}')
    classes = work / 'classes'
    classes.mkdir()
    options = ['--release', str(release), '-encoding', 'UTF-8', '-g:source,lines,vars',
               '-d', str(classes), '-sourcepath', str(source_root)]
    if jars:
        options += ['-classpath', os.pathsep.join(str(p) for p in jars)]
    if processors:
        options += ['-processorpath', os.pathsep.join(str(p) for p in processors), '-processor', PROCESSOR]
    else:
        options += ['-proc:none']
    options.extend(str(p) for p in sources)
    args = work / 'javac.args'
    args.write_text('\n'.join(argfile_value(v) for v in options) + '\n', encoding='utf-8')
    result = run([javac, '-J-Duser.language=en', '-J-Duser.country=US', '-J-Dfile.encoding=UTF-8', '@' + str(args)])
    (work / 'javac.log').write_text(result.stdout, encoding='utf-8')
    if result.returncode:
        raise BuildError(f'javac failed ({result.returncode}):\n{result.stdout}')
    return classes, result.stdout


def owned_class(name):
    return name.startswith('starlords/') or name == 'com/fs/starfarer/api/impl/campaign/rulecmd/LordsOpenCommCmd.class'


def package_classes(classes, target):
    paths = sorted(classes.rglob('*.class'))
    if not paths:
        raise BuildError('Compiler produced no classes')
    # Stored entries avoid compression-library-version differences. Fixed order,
    # timestamp, permissions and metadata avoid host/path/clock-dependent JARs.
    with zipfile.ZipFile(target, 'w', compression=zipfile.ZIP_STORED) as archive:
        for path in paths:
            name = path.relative_to(classes).as_posix()
            if not owned_class(name):
                raise BuildError(f'Unexpected class in artifact (possible bundled dependency): {name}')
            item = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
            item.create_system = 3
            item.external_attr = 0o100644 << 16
            archive.writestr(item, path.read_bytes())


def verify(jar, *, check_legacy=False):
    classes = inspect_jar(jar)
    # Runtime validity is mandatory. Old save shapes and internal APIs are not:
    # this overhaul may deliberately require a new campaign.
    errors = []
    mod_info = read_json(ROOT / 'mod_info.json')
    if not isinstance(mod_info.get('id'), str) or not mod_info['id'].strip():
        errors.append('mod_info.json must declare a nonempty mod ID')
    if mod_info.get('jars') != ['jars/StarLords.jar']:
        errors.append('This single-JAR build requires mod_info.json to load jars/StarLords.jar')
    for name, item in classes.items():
        if not owned_class(name + '.class'):
            errors.append(f'Unexpected class in artifact (possible bundled dependency): {name}')
        if item['major'] > JAVA_RELEASE + 44 or item['minor'] == 65535:
            errors.append(f'Unsupported bytecode level: {name} ({item["major"]}.{item["minor"]})')
    expected_plugin = str(mod_info.get('modPlugin', '')).replace('.', '/')
    if expected_plugin not in classes:
        errors.append(f'Missing mod plugin: {expected_plugin}')
    if check_legacy:
        baseline = read_json(BASELINE)
        errors += compare_classes(baseline['classes'], classes, JAVA_RELEASE + 44)
        errors += compare_contract(baseline['sourceContract'], source_contract(ROOT))
    if errors:
        raise BuildError('Artifact check failed:\n' + '\n'.join(errors))
    return {'jarSha256': sha256(jar), 'classCount': len(classes),
            'maxClassMajor': max(v['major'] for v in classes.values()),
            'artifactChecks': 'passed',
            'legacyCompatibility': 'passed' if check_legacy else 'not required',
            'campaignTest': 'not run'}


def replace_file(source, target):
    """Stage beside the destination, then replace it without a partial write."""
    target.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(prefix=target.name + '.', suffix='.tmp',
                                     dir=target.parent, delete=False) as stream:
        temporary = Path(stream.name)
    try:
        shutil.copyfile(source, temporary)
        os.replace(temporary, target)
    finally:
        temporary.unlink(missing_ok=True)


def package_mod(root, jar, target):
    """Package runtime resources and the candidate JAR, excluding build tools/deps."""
    files = {'jars/StarLords.jar': jar, 'mod_info.json': root / 'mod_info.json'}
    for directory in ('data', 'graphics', 'sounds'):
        folder = root / directory
        if directory != 'sounds' and not folder.is_dir():
            raise BuildError(f'Missing mod resource directory: {folder}')
        if folder.is_dir():
            for path in folder.rglob('*'):
                if path.is_file():
                    files[path.relative_to(root).as_posix()] = path
    for name in ('LICENSE', 'Starlords.version', 'changelog.text', 'TESTING.md', 'PRESENTATION.md'):
        path = root / name
        if path.is_file():
            files[name] = path
    with zipfile.ZipFile(target, 'w', compression=zipfile.ZIP_STORED) as archive:
        for name, path in sorted(files.items()):
            entry = zipfile.ZipInfo('Starlords_immersionPatches/' + name, (1980, 1, 1, 0, 0, 0))
            entry.create_system = 3
            entry.external_attr = 0o100644 << 16
            archive.writestr(entry, path.read_bytes())


def build_once(env, target):
    OUTPUT.mkdir(exist_ok=True)
    # The only recursive cleanup is TemporaryDirectory's own generated child
    # under the verified repository build directory; no user path is deleted.
    if OUTPUT.resolve() != ROOT / 'build':
        raise BuildError('The build directory must not be a symlink/junction outside the workspace')
    with tempfile.TemporaryDirectory(prefix='compile-', dir=OUTPUT) as temporary:
        work = Path(temporary).resolve()
        if not work.is_relative_to(OUTPUT.resolve()):
            raise BuildError('Compiler output escaped the build directory')
        try:
            classes, output = compile_sources(ROOT / 'src', work, env['javac'], env['release'], env['jars'], env['processors'])
            (OUTPUT / 'javac.log').write_text(output, encoding='utf-8')
            candidate = work / 'StarLords.jar'
            package_classes(classes, candidate)
            report = verify(candidate)
            shutil.copyfile(candidate, target)
        except BuildError:
            if (work / 'javac.log').exists():
                shutil.copyfile(work / 'javac.log', OUTPUT / 'javac.log')
            raise
    return report


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('command', choices=['doctor', 'lock', 'build', 'reproduce', 'verify', 'capture-baseline'])
    parser.add_argument('--config', type=Path, default=CONFIG)
    parser.add_argument('--jar', type=Path, help='JAR for verify; defaults to jars/StarLords.jar')
    parser.add_argument('--check-legacy', action='store_true',
                        help='Optional original save/API comparison, for verify only')
    args = parser.parse_args(argv)
    try:
        if args.check_legacy and args.command != 'verify':
            raise BuildError('--check-legacy is only available with verify; it never gates builds')
        if args.command == 'capture-baseline':
            if BASELINE.exists():
                raise BuildError('Baseline already exists; it is immutable during ordinary builds')
            baseline = make_baseline(ROOT, ROOT / 'jars/StarLords.jar')
            write_json(BASELINE, baseline)
            print(f'Captured {len(baseline["classes"])} classes and {len(baseline["sourceFiles"])} source files.')
            return 0
        if args.command == 'verify':
            print(json.dumps(verify((args.jar or RUNTIME).resolve(), check_legacy=args.check_legacy), indent=2))
            return 0
        if not args.config.is_file():
            raise BuildError(f'Build configuration missing: {args.config}. Copy build.example.json to build.local.json and set local paths.')
        env = environment(args.config.resolve())
        if args.command == 'doctor':
            print(f'{env["version"]}; target Java {env["release"]}; {len(env["jars"])} dependency JARs; all required APIs found.')
            return 0
        if args.command == 'lock':
            write_json(LOCK, input_lock(env))
            print('Wrote build.lock.json. Review/version this file when changing dependencies.')
            return 0
        lock = require_lock(env)
        target = OUTPUT / 'StarLords.jar'
        report = build_once(env, target)
        report['reproducibility'] = 'not checked'
        if args.command == 'reproduce':
            second = OUTPUT / 'StarLords.reproduce.jar'
            second_report = build_once(env, second)
            if report['jarSha256'] != second_report['jarSha256']:
                raise BuildError('Two clean builds produced different JAR hashes')
            report['reproducibility'] = 'two clean builds have identical SHA-256'
        report['inputs'] = {'lock': lock, 'sourceFiles': source_fingerprint(ROOT),
                            'buildTools': {p: sha256(ROOT / p) for p in ('tools/build.py', 'tools/java_compat.py')}}
        # Publish only after compilation, runtime checks and (when requested)
        # both reproducibility builds succeed. Package the fresh candidate, not
        # the previous runtime JAR. A failed build leaves that runtime intact.
        with tempfile.TemporaryDirectory(prefix='package-', dir=OUTPUT) as temporary:
            package = Path(temporary) / DISTRIBUTION.name
            package_mod(ROOT, target, package)
            report['distributionSha256'] = sha256(package)
            replace_file(package, DISTRIBUTION)
        replace_file(target, RUNTIME)
        report['runtimeJar'] = RUNTIME.relative_to(ROOT).as_posix()
        report['distribution'] = DISTRIBUTION.relative_to(ROOT).as_posix()
        write_json(OUTPUT / 'build-report.json', report)
        print(json.dumps({k: v for k, v in report.items() if k != 'inputs'}, indent=2))
        print(f'Runtime JAR updated: {RUNTIME}')
        print(f'Copy this mod folder into Starsector/mods, or extract: {DISTRIBUTION}')
        return 0
    except (BuildError, ValueError, OSError, zipfile.BadZipFile) as exc:
        print(f'BUILD ERROR: {exc}', file=sys.stderr)
        return 1


if __name__ == '__main__':
    sys.exit(main())

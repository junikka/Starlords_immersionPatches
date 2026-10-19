"""Class-file inspection and conservative structural compatibility checks.

Uses the Python standard library; does not load game classes or execute a JAR.
This is a regression guard, not proof that a Starsector save will load.
"""
from __future__ import annotations

import hashlib
import io
import json
from pathlib import Path
import re
import struct
import zipfile


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            digest.update(block)
    return digest.hexdigest()


class Reader:
    def __init__(self, data: bytes):
        self.stream = io.BytesIO(data)

    def take(self, size: int) -> bytes:
        value = self.stream.read(size)
        if len(value) != size:
            raise ValueError('Truncated Java class file')
        return value

    def u1(self):
        return self.take(1)[0]

    def u2(self):
        return struct.unpack('>H', self.take(2))[0]

    def u4(self):
        return struct.unpack('>I', self.take(4))[0]


def read_class(data: bytes) -> dict:
    reader = Reader(data)
    if reader.u4() != 0xCAFEBABE:
        raise ValueError('Not a Java class file')
    minor, major = reader.u2(), reader.u2()
    count = reader.u2()
    pool = [None] * count
    i = 1
    while i < count:
        tag = reader.u1()
        if tag == 1:
            # Names/descriptors are ASCII; replace modified-UTF8 string constants
            # only for decoding. They are not interpreted as identifiers here.
            pool[i] = reader.take(reader.u2()).decode('utf-8', errors='replace')
        elif tag in (7, 8, 16, 19, 20):
            pool[i] = (tag, reader.u2())
        elif tag in (3, 4):
            reader.take(4)
        elif tag in (5, 6):
            reader.take(8)
            i += 1
        elif tag in (9, 10, 11, 12, 17, 18):
            pool[i] = (tag, reader.u2(), reader.u2())
        elif tag == 15:
            pool[i] = (tag, reader.u1(), reader.u2())
        else:
            raise ValueError(f'Unsupported constant-pool tag {tag}')
        i += 1

    def class_name(index):
        return pool[pool[index][1]] if index else None

    def attributes():
        result = {}
        for _ in range(reader.u2()):
            name = pool[reader.u2()]
            result[name] = reader.take(reader.u4())
        return result

    access, name, parent = reader.u2(), class_name(reader.u2()), class_name(reader.u2())
    interfaces = [class_name(reader.u2()) for _ in range(reader.u2())]

    def members():
        result = []
        for _ in range(reader.u2()):
            member_access, member_name, descriptor = reader.u2(), pool[reader.u2()], pool[reader.u2()]
            attrs = attributes()
            item = {'name': member_name, 'descriptor': descriptor, 'access': member_access}
            if 'Signature' in attrs:
                item['signature'] = pool[struct.unpack('>H', attrs['Signature'])[0]]
            result.append(item)
        return result

    fields, methods, attrs = members(), members(), attributes()
    source_file = pool[struct.unpack('>H', attrs['SourceFile'])[0]] if 'SourceFile' in attrs else None
    return {'name': name, 'super': parent, 'interfaces': interfaces, 'access': access,
            'major': major, 'minor': minor, 'sourceFile': source_file,
            'fields': fields, 'methods': methods}


def inspect_jar(path: Path) -> dict:
    classes = {}
    with zipfile.ZipFile(path) as jar:
        names = jar.namelist()
        if len(names) != len(set(names)):
            raise ValueError(f'Duplicate archive entries in {path}')
        for entry in sorted(names):
            if entry.endswith('.class'):
                item = read_class(jar.read(entry))
                if entry != item['name'] + '.class':
                    raise ValueError(f'Class name does not match archive entry: {entry}')
                classes[item['name']] = item
    if not classes:
        raise ValueError(f'No Java classes in {path}')
    return classes


def strip_comments(source: str) -> str:
    tokens = re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/')
    return tokens.sub(lambda m: re.sub(r'[^\n]', ' ', m[0]) if m[0].startswith(('/', '/*')) else m[0], source)


def source_contract(root: Path) -> dict:
    sources = sorted((root / 'src').rglob('*.java'))
    aliases = {}
    memory_constants = {}
    persistent_keys = {}
    for path in sources:
        source = strip_comments(path.read_text(encoding='utf-8-sig'))
        relative = path.relative_to(root).as_posix()
        for alias, class_name in re.findall(r'\bx\.alias\("([^"]+)"\s*,\s*([\w.]+)\.class\)', source):
            aliases[alias] = class_name
        for name, value in re.findall(r'\b(?:static\s+(?:final\s+)?)String\s+(\w+)\s*=\s*"([^"\n]*)"', source):
            if re.search(r'KEY|MEMORY|MemoryKey', name, re.I) or value.startswith('$'):
                memory_constants[f'{relative}:{name}'] = value
        keys = sorted(set(re.findall(r'\bpersistentData\.put\("([^"\n]+)"', source)))
        if keys:
            persistent_keys[relative] = keys
    return {'xstreamAliases': aliases, 'memoryConstants': memory_constants,
            'persistentDataKeys': persistent_keys}


def source_fingerprint(root: Path) -> dict:
    return {p.relative_to(root).as_posix(): sha256(p) for p in sorted((root / 'src').rglob('*.java'))}


def compare_contract(expected: dict, actual: dict) -> list[str]:
    errors = []
    for category in ('xstreamAliases', 'memoryConstants'):
        for key, value in expected[category].items():
            if actual[category].get(key) != value:
                errors.append(f'{category} changed or removed: {key}')
    for path, keys in expected['persistentDataKeys'].items():
        for key in set(keys) - set(actual['persistentDataKeys'].get(path, [])):
            errors.append(f'Persistent key removed from {path}: {key}')
    return sorted(errors)


def compare_classes(expected: dict, actual: dict, max_major: int) -> list[str]:
    errors = []
    for name, item in actual.items():
        if item['major'] > max_major or item['minor'] == 65535:
            errors.append(f'Unsupported bytecode level: {name} ({item["major"]}.{item["minor"]})')
    for name, before in expected.items():
        if name not in actual:
            errors.append(f'Class removed: {name}')
            continue
        after = actual[name]
        if before['super'] != after['super'] or set(before['interfaces']) - set(after['interfaces']):
            errors.append(f'Superclass/interfaces changed: {name}')
        if before['access'] & 1 and not after['access'] & 1:
            errors.append(f'Class is no longer public: {name}')
        after_fields = {f['name']: f for f in after['fields']}
        for field in before['fields']:
            # XStream can persist private instance fields; ignore static,
            # transient and compiler-generated fields for the save-shape check.
            saved = not field['access'] & (0x0008 | 0x0080 | 0x1000)
            public = bool(field['access'] & (0x0001 | 0x0004))
            if not (saved or public):
                continue
            other = after_fields.get(field['name'])
            if other is None or field['descriptor'] != other['descriptor'] or field.get('signature') != other.get('signature'):
                errors.append(f'Field removed/type changed: {name}.{field["name"]}')
            elif saved and other['access'] & (0x0008 | 0x0080):
                errors.append(f'Persisted field became static/transient: {name}.{field["name"]}')
            elif public and (field['access'] & 0x000D) != (other['access'] & 0x000D):
                errors.append(f'Field visibility/static contract changed: {name}.{field["name"]}')
        before_enum = [f['name'] for f in before['fields'] if f['access'] & 0x4000]
        after_enum = [f['name'] for f in after['fields'] if f['access'] & 0x4000]
        if after_enum[:len(before_enum)] != before_enum:
            errors.append(f'Enum constants removed/reordered: {name}')
        after_methods = {(m['name'], m['descriptor']): m for m in after['methods']}
        for method in before['methods']:
            if not method['access'] & (0x0001 | 0x0004) or method['access'] & 0x1000:
                continue
            other = after_methods.get((method['name'], method['descriptor']))
            if other is None:
                errors.append(f'Public/protected method removed: {name}.{method["name"]}{method["descriptor"]}')
            elif (method['access'] & 0x000D) != (other['access'] & 0x000D):
                errors.append(f'Method visibility/static contract changed: {name}.{method["name"]}')
    return sorted(errors)


def make_baseline(root: Path, jar: Path) -> dict:
    classes = inspect_jar(jar)
    missing_sources = []
    for name, item in classes.items():
        package = name.rsplit('/', 1)[0] if '/' in name else ''
        if not item['sourceFile'] or not (root / 'src' / package / item['sourceFile']).is_file():
            missing_sources.append(name)
    return {'schemaVersion': 1, 'modInfo': json.loads((root / 'mod_info.json').read_text()),
            'jarSha256': sha256(jar), 'maxClassMajor': max(c['major'] for c in classes.values()),
            'classes': classes, 'sourceFiles': source_fingerprint(root),
            'classesWithoutSource': missing_sources, 'sourceContract': source_contract(root)}

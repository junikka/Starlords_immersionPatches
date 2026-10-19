"""Read-only extraction of Starlords wording into review artifacts (Python stdlib).

Run from any directory. Writes only docs/immersion-audit/generated/.
Classifications are editorial review candidates, not automatic replacements.
"""
from pathlib import Path
import bisect
import collections
import csv
import hashlib
import io
import json
import re

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/immersion-audit/generated'
CONCEPTS = {
    'titles_and_collectives': r'\b(?:star[ -]?lords?|lords?|lad(?:y|ies)|high lord|first lord|nobles?|nobility|noblemen|noblewomen|aristocra\w*|dukes?|duchess|doux|megas|barons?|counts?|knights?)\b',
    'territory_and_fealty': r'\b(?:fiefs?|fiefdoms?|feifs?|feifdoms?|lieges?|realms?|vassals?|vassalage|subjects?|rulers?|rulership|crown|throne|kingdom|kings?|queens?|sovereign|rightful|birthright|bloodline|oaths?|swear|sworn|allegiance|fealty|homage|estates?|demesne|dynast\w*|reign|exile|join our houses|claim to unite|claim is seen|justify that claim)\b',
    'social_events': r'\b(?:feasts?|feasting|banquets?|revelry|revels?|merry|morrow|halls?|retinue|banners?)\b',
    'courtship_and_marriage': r'\b(?:court(?:ing|ship|ed)?|courts|suitors?|woo\w*|wed(?:ding|dings)?|marry|marr(?:ied|iage|iages)|betroth\w*|spouses?|husbands?|wives|wife|consort|hand in marriage|dowr\w*|admiration|romantic|romance|beloved|household|ceremony)\b',
    'military_office': r'\b(?:marshals?|marshall|generalissimo|admirals?|commanders?)\b',
    'tournament': r'\b(?:tournaments?|champions?|wagers?|dedicat\w*|joust\w*)\b',
    'faction_voice_review': r'\b(?:bishops?|archbishops?|cardinals?|brothers?|sisters?|clergy|cleric\w*|crusad\w*|holy|divine|ludd|god|heresy|heretic\w*|infidel\w*|honou?r\w*|valor|chival\w*|peasant\w*|serfs?|commoners?|lowborn|highborn|patrician\w*|patriarch\w*|well met|en garde|pray tell|oubliette|butter\w*)\b',
    'meta_or_tone': r'\b(?:final boss|malice|alex|women shortages|They Has No Mouth|terrifying individual|mount and blade|warband|DLC|edlric|insignificantint)\b|\(error\)',
}
PATTERNS = {k: re.compile(v, re.I) for k, v in CONCEPTS.items()}
LEX = re.compile(r'(?P<comment>//[^\n]*|/\*[\s\S]*?\*/)|(?P<char>\'(?:\\.|[^\'\\])*\')|(?P<string>"(?:\\.|[^"\\])*")')
rows = []


def concepts(text):
    text = re.sub(r'%[A-Z][A-Z_0-9]*|%c\d+|\$string', '', text)
    return [key for key, pat in PATTERNS.items() if pat.search(text)]


def add(path, line, locator, text, surface, activity='active', context=''):
    found = concepts(text)
    if surface == 'dialogue' and re.search(r'marriage|admiration|marriedToPlayer|spend_time_together|wedding|spouse', locator, re.I):
        found = list(dict.fromkeys(found + ['courtship_and_marriage']))
    if surface == 'dialogue' and re.search(r'tournament|dedicate', locator, re.I):
        found = list(dict.fromkeys(found + ['tournament']))
    if surface == 'dialogue' and re.search(r'feast', locator, re.I):
        found = list(dict.fromkeys(found + ['social_events']))
    if surface == 'rank_title':
        found = list(dict.fromkeys(found + ['rank_scheme_review']))
    if surface == 'biography':
        found = list(dict.fromkeys(found + ['biography_review']))
    hard = {'titles_and_collectives', 'territory_and_fealty', 'social_events', 'meta_or_tone'}
    priority = 'rewrite_candidate' if hard.intersection(found) else 'context_review' if found else 'unflagged'
    if activity != 'active':
        priority = 'reference_only' if found else 'unflagged'
    key = f'{path}|{locator}'
    rows.append(dict(id='S-' + hashlib.sha256(key.encode()).hexdigest()[:10],
                     file=path, line=line, locator=locator, surface=surface,
                     activity=activity, priority=priority, concepts=found,
                     original=text, context=context, replacement=None, decision=None, notes=''))


def json_leaves(path):
    """Parse strict JSON, retaining the exact line of each string value."""
    src = (ROOT / path).read_text(encoding='utf-8-sig')
    tokens = [(m.start(), m.group()) for m in re.finditer(r'"(?:\\.|[^"\\])*"|[{}\[\]:,]|true|false|null|-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?', src)]
    newlines = [m.start() for m in re.finditer('\n', src)]
    i = 0
    leaves = []

    def parse(parts):
        nonlocal i
        pos, tok = tokens[i]
        i += 1
        if tok == '{':
            while tokens[i][1] != '}':
                key = json.loads(tokens[i][1]); i += 1
                assert tokens[i][1] == ':'; i += 1
                parse(parts + [key])
                if tokens[i][1] == ',': i += 1
            i += 1
        elif tok == '[':
            index = 0
            while tokens[i][1] != ']':
                parse(parts + [index]); index += 1
                if tokens[i][1] == ',': i += 1
            i += 1
        elif tok.startswith('"'):
            leaves.append((parts, json.loads(tok), bisect.bisect_left(newlines, pos) + 1))

    json.loads(src)  # fail loudly for malformed input
    parse([])
    assert i == len(tokens)
    return leaves


def pointer(parts):
    return '/' + '/'.join(str(p).replace('~', '~0').replace('/', '~1') for p in parts)


def data_strings():
    for p in sorted((ROOT / 'data').rglob('*.json')):
        path = p.relative_to(ROOT).as_posix()
        if path.startswith('data/config/') or p.name in ('SMods.json', 'default_dialog_options.json'):
            continue  # configuration identifiers, examined separately
        for parts, text, line in json_leaves(path):
            surface = None
            activity = 'active'
            if path == 'data/strings/strings.json':
                surface = 'rank_title' if parts[0] == 'starlords_title' else 'localized_ui'
            elif p.name == 'faction.json' and str(parts[-1]).startswith('rank_'):
                surface = 'rank_title'
            elif p.name.startswith('lords') and parts[-1] in ('lore', 'fleetName'):
                surface = 'biography' if parts[-1] == 'lore' else 'fleet_name'
                if p.name != 'lords.json': activity = 'unloaded_roster_copy'
            elif p.name == 'dialog.json':
                # additionalText contains line IDs, not visible prose.
                if (len(parts) == 5 and parts[1] == parts[3] == 'lines') or (len(parts) == 6 and parts[1] == parts[3] == 'lines' and parts[-1] in ('line', 'hint')):
                    surface = 'dialogue'
                elif 'highlight' in parts:
                    surface = 'dialogue_highlight'
                if parts[0] == 'template': activity = 'opt_in_dialogue_template'
            if surface:
                add(path, line, pointer(parts), text, surface, activity)
    for path in ('data/config/LunaSettings.csv', 'data/console/commands.csv', 'data/campaign/rules.csv'):
        src = (ROOT / path).read_text(encoding='utf-8-sig')
        reader = csv.DictReader(io.StringIO(src))
        last = 1
        for row in reader:
            line = last + 1
            last = reader.line_num
            if not row or not next(iter(row.values()), ''): continue
            identity = row.get('fieldID') or row.get('name') or row.get('id') or next(iter(row.values()))
            for col in ('fieldName', 'fieldDescription', 'tab', 'text', 'options', 'help', 'description'):
                if row.get(col):
                    add(path, line, f'{identity}.{col}', row[col], 'console_help' if '/console/' in path else 'settings' if 'Luna' in path else 'rule_text')
            if row.get('fieldType') in ('Header', 'Text'):
                add(path, line, f'{identity}.defaultValue', row['defaultValue'], 'settings')
    path = 'data/world/factions/starlords_nobility.faction'
    src = (ROOT / path).read_text()
    for m in re.finditer(r'"(displayName(?:WithArticle)?)"\s*:\s*("(?:\\.|[^"\\])*")', src):
        add(path, src.count('\n', 0, m.start()) + 1, m[1], json.loads(m[2]), 'hidden_portrait_faction', 'hidden_placeholder')
    for parts, value, line in json_leaves('mod_info.json'):
        if parts[-1] in ('name', 'description'):
            add('mod_info.json', line, pointer(parts), value, 'mod_metadata')


def java_strings():
    for p in sorted((ROOT / 'src').rglob('*.java')):
        path = p.relative_to(ROOT).as_posix()
        src = p.read_text(encoding='utf-8-sig')
        lines = src.splitlines()
        newlines = [m.start() for m in re.finditer('\n', src)]
        tokens = list(LEX.finditer(src))
        # Erase comments and char literals, retaining length/line offsets.
        clean = LEX.sub(lambda m: m[0] if m.lastgroup == 'string' else re.sub(r'[^\n]', ' ', m[0]), src)
        seen_lines = collections.Counter()
        for match in tokens:
            if match.lastgroup != 'string': continue
            raw = match[0]
            try: text = json.loads(raw)
            except json.JSONDecodeError: text = raw[1:-1]
            if not text.strip(): continue
            line = bisect.bisect_left(newlines, match.start()) + 1
            seen_lines[line] += 1
            stmt_start = max(clean.rfind(';', 0, match.start()), clean.rfind('{', 0, match.start()), clean.rfind('}', 0, match.start())) + 1
            stmt_end = clean.find(';', match.end())
            if stmt_end < 0: stmt_end = match.end()
            statement = clean[stmt_start:stmt_end+1].strip()
            before = clean[stmt_start:match.start()]
            context = '\n'.join(lines[max(0,line-2):min(len(lines),line+2)])
            activity = 'active'
            surface = 'java_text'
            if re.search(r'\b(?:log|logger)\s*\.\s*(?:info|warn|error|debug|fatal|trace)\s*\(', before) or 'System.out.' in before or 'throw new ' in before or (path.endswith('/LordController.java') and line >= 503) or (path.endswith('/BattleListener.java') and 'PrisonerCalculation' in statement) or (path.endswith('/DefectionUtils.java') and line >= 196) or (path.endswith('/Utils.java') and line >= 714):
                activity = 'diagnostic'; surface = 'diagnostic'
            elif '/console/' in path:
                surface = 'console_help'
            elif (re.fullmatch(r'[%$\w./\\:-]+', text) and (not text[:1].isupper() or '_' in text)) or re.search(r'(?:case\s*|\.equals\(|\.contains\(|\.getString\(|\.getBoolean\(|\.getInt\(|\.getDouble\(|\.getJSONObject\(|\.getJSONArray\(|\.has\(|\.get\(|\.put\(|\.set\(|\.remove\(|getString\([^;]*,\s*)$', before):
                activity = 'internal_identifier'; surface = 'internal'
            if path.endswith('/DefectionUtils.java') and 130 <= line <= 132:
                activity = 'internal_identifier'; surface = 'faction_name_logic_comparison'
            if path.endswith('/GenderUtils.java'):
                activity = 'unused_helper' if 23 <= line <= 29 else 'active'
                surface = 'java_text'
            add(path, line, f'java:{line}:{seen_lines[line]}', text, surface, activity, context)
            rows[-1]['source_expression'] = statement
    # Event headings expose an internal event ID; this is not a literal search hit.
    add('src/starlords/ui/EventIntelPlugin.java', 117, 'derived:getName:FEAST',
        'Ongoing Feast', 'java_text', context='Derived by capitalizing LordEvent.FEAST ("feast"). Replace display mapping; retain event ID.')


def md_escape(value):
    return value.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;').replace('|', '\\|').replace('\n', '<br>')


def write_review(name, selected, intro):
    buf = [f'# {name.replace("_", " ").title()}', '', intro, '', 'Keep placeholders and identifiers unchanged. Fill `Replacement` and record `keep` where the original fits. Java entries are literal fragments: use the JSON context to rewrite the complete expression.', '']
    files = collections.defaultdict(list)
    for row in selected: files[row['file']].append(row)
    for path, group in files.items():
        buf += [f'## {path}', '']
        for row in group:
            buf += [f'### {row["id"]} — line {row["line"]}', '', f'- Location: `{row["locator"]}`', f'- Review: {row["priority"]}; {", ".join(row["concepts"])}', f'- Original: {md_escape(row["original"])}', '- Replacement: ', '- Notes: ', '']
    (OUT / f'{name}.md').write_text('\n'.join(buf), encoding='utf-8')


def write_unique_review(active):
    grouped = collections.defaultdict(list)
    for row in active:
        grouped[row['original']].append(row)
    buf = ['# Distinct wording for replacement review', '',
           f'{len(grouped)} distinct values across {len(active)} active-source candidate occurrences. Some are context-dependent and can be kept. Identical wording is grouped for convenience, not assumed to require the same replacement in every context.', '',
           'For each item, enter a replacement or “keep”; specify faction/personality exceptions in Notes. Source IDs map to the detailed inventories and JSON. Java entries can be fragments of longer expressions.', '']
    for original, group in grouped.items():
        uid = 'U-' + hashlib.sha256(original.encode()).hexdigest()[:10]
        categories = sorted({c for row in group for c in row['concepts']})
        buf += [f'## {uid}', '', f'- Review: {", ".join(categories)}',
                f'- Original: {md_escape(original)}', '- Replacement: ', '- Notes: ', '- Occurrences:']
        for row in group:
            buf.append(f'  - `{row["id"]}` — `{row["file"]}:{row["line"]}` — `{row["locator"]}`')
        buf.append('')
    (OUT / '00_distinct_wording.md').write_text('\n'.join(buf), encoding='utf-8')


def main():
    data_strings()
    java_strings()
    OUT.mkdir(parents=True, exist_ok=True)
    assert len({r['id'] for r in rows}) == len(rows), 'Duplicate review IDs'
    active = [r for r in rows if r['activity'] == 'active' and r['concepts']]
    groups = {
        '01_dialogue': [r for r in active if r['surface'] in ('dialogue', 'dialogue_highlight')],
        '02_titles_ui_and_events': [r for r in active if r['surface'] in ('rank_title', 'localized_ui', 'java_text')],
        '03_biographies_and_fleets': [r for r in active if r['surface'] in ('biography', 'fleet_name')],
        '04_settings_and_console': [r for r in active if r['surface'] in ('settings', 'console_help', 'rule_text', 'mod_metadata')],
        '05_inactive_reference_copies': [r for r in rows if r['concepts'] and r['activity'] in ('unloaded_roster_copy', 'opt_in_dialogue_template', 'hidden_placeholder', 'unused_helper')],
    }
    for name, group in groups.items():
        write_review(name, group, f'{len(group)} review entries. This is an editorial candidate list, including context-dependent terms that may be retained.')
    write_unique_review(active)
    stats = dict(total_extracted_strings=len(rows), active_candidates=len(active),
                 distinct_active_values=len({r['original'] for r in active}),
                 java_files_scanned=len(list((ROOT / 'src').rglob('*.java'))),
                 candidate_files=len({r['file'] for r in active}),
                 by_review_file={k:len(v) for k,v in groups.items()},
                 by_activity=dict(collections.Counter(r['activity'] for r in rows)),
                 by_concept=dict(collections.Counter(c for r in active for c in r['concepts'])))
    manifest = dict(scope='Local source/data audit; no game execution or JAR/source parity verification. All Java literals retained; prose classification is heuristic.',
                    stats=stats, entries=rows)
    (OUT / 'string_inventory.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(stats, indent=2))


if __name__ == '__main__': main()

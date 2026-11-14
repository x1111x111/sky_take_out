"""Static packaging checks only. This does not compile Java or execute MySQL."""
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(__file__).resolve().parents[1]
errors = []
xmls = [root/'sky-server/pom.xml', *(root/'sky-server/src/main/resources/mapper').glob('*.xml')]
for p in xmls:
    try:
        ET.parse(p)
    except ET.ParseError as e:
        errors.append(f'{p.relative_to(root)}: {e}')
for p in [root/'README.md', *(root/'docs').rglob('*.md')]:
    for target in re.findall(r'\[[^\]]*\]\(([^\s)]+)\)', p.read_text()):
        if target.startswith(('http:', 'https:', '#')):
            continue
        if not (p.parent/target.split('#')[0]).exists():
            errors.append(f'Broken link: {p.relative_to(root)} -> {target}')
sql = (root/'sql/sky_take_out.sql').read_text()
tables = set(re.findall(r'^CREATE TABLE `?(\w+)`?', sql, flags=re.I | re.M))
if len(tables) != 13:
    errors.append(f'Expected 13 schema tables, found {len(tables)}')
for p in (root/'sky-server/src/main/java/com/example/skyserver/entity').glob('*.java'):
    table = re.sub(r'(?<!^)(?=[A-Z])', '_', p.stem).lower()
    m = re.search(r'CREATE TABLE `'+table+r'` \((.*?)\) ENGINE', sql, flags=re.S)
    if not m:
        errors.append('Missing entity table: '+table)
        continue
    columns = {c.lower() for c in re.findall(r'^  `(\w+)`', m.group(1), flags=re.M)}
    for name in re.findall(r'private\s+[\w<>]+\s+(\w+)\s*;', p.read_text()):
        name = re.sub(r'(?<!^)(?=[A-Z])', '_', name).lower()
        if name not in columns:
            errors.append(f'Missing column: {table}.{name}')
routes = json.loads((root/'docs/api/routes.json').read_text())
if len({(r['method'],r['path']) for r in routes}) != len(routes):
    errors.append('Duplicate documented routes')
source_count = sum(len(re.findall(r'@(Get|Post|Put|Delete|Patch)Mapping\b',p.read_text()))
                   for p in (root/'sky-server/src/main/java/com/example/skyserver/controller').glob('*.java'))
if source_count != len(routes):
    errors.append('Route count does not match source')
config = (root/'sky-server/src/main/resources/application.yml').read_text()
if '${DB_PASSWORD}' not in config or '${JWT_SECRET}' not in config:
    errors.append('Required secret placeholders missing')
if (root/'sky-server/src/main/java/com/example/skyserver/controller/TestController.java').exists():
    errors.append('Debug employee endpoint still included')
print(json.dumps({'status':'failed' if errors else 'passed','xml_files':len(xmls),
                  'schema_tables':len(tables),'documented_routes':len(routes),
                  'errors':errors},ensure_ascii=False,indent=2))
sys.exit(bool(errors))

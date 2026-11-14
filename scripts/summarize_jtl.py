"""Recompute CSV JTL metrics. Percentiles use nearest rank. No response bodies exist."""
import csv
import hashlib
import json
import math
from datetime import datetime, timezone, timedelta
from pathlib import Path

root = Path(__file__).resolve().parents[1]
results = []
for p in sorted((root/'docs/testing/raw').glob('*.jtl')):
    with p.open(encoding='utf-8-sig', newline='') as f:
        rows = list(csv.DictReader(f))
    if not rows:
        continue
    elapsed = sorted(int(r['elapsed']) for r in rows)
    first = min(int(r['timeStamp']) for r in rows)
    end = max(int(r['timeStamp']) + int(r['elapsed']) for r in rows)
    failures = sum(r['success'].lower() != 'true' for r in rows)
    seconds = (end-first)/1000
    def pct(value):
        return elapsed[max(0, math.ceil(value*len(elapsed))-1)]
    def date(value):
        return datetime.fromtimestamp(value/1000, timezone(timedelta(hours=8))).isoformat()
    results.append({
        'file': p.name, 'sha256': hashlib.sha256(p.read_bytes()).hexdigest(),
        'samples': len(rows), 'start_china_time': date(first), 'end_china_time': date(end),
        'duration_seconds': seconds, 'average_ms': round(sum(elapsed)/len(elapsed),4),
        'p95_ms': pct(.95), 'p99_ms': pct(.99), 'min_ms': min(elapsed), 'max_ms': max(elapsed),
        'failed_samples': failures, 'jmeter_error_percent': round(100*failures/len(rows),4),
        'throughput_per_second': round(len(rows)/seconds,4) if seconds else None,
        'max_observed_threads': max(int(r['allThreads']) for r in rows),
        'urls': sorted({r['URL'] for r in rows}),
        'response_codes': sorted({r['responseCode'] for r in rows}),
        'received_kb_per_second': round(sum(int(r['bytes']) for r in rows)/1024/seconds,4) if seconds else None,
        'sent_kb_per_second': round(sum(int(r['sentBytes']) for r in rows)/1024/seconds,4) if seconds else None,
    })
print(json.dumps(results, ensure_ascii=False, indent=2))

#!/usr/bin/env python3
"""Exercise startup/exit in a temporary directory, without touching historical data."""
from pathlib import Path
import os
import subprocess
import tempfile

root = Path(__file__).resolve().parents[1]
java_home = os.environ.get('JAVA_HOME')
java = str(Path(java_home) / 'bin' / ('java.exe' if os.name == 'nt' else 'java')) if java_home else 'java'
with tempfile.TemporaryDirectory(prefix='cine-smoke-') as cwd:
    result = subprocess.run(
        [java, '-cp', str(root / 'Cine/target/classes'), 'com.penta.code.cine.AppCineTics'],
        input='4\n', text=True, encoding='utf-8', capture_output=True, cwd=cwd, timeout=20,
    )
    if result.returncode != 0 or 'Saliendo del sistema' not in result.stdout:
        raise SystemExit(f'Console smoke failed ({result.returncode}):\n{result.stdout}\n{result.stderr}')
    if list(Path(cwd).iterdir()):
        raise SystemExit('Console startup unexpectedly wrote files')
print('PASS: menu startup and clean exit; no data files written')

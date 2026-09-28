#!/usr/bin/env python3
"""Generate the human-readable requirements matrix from its canonical CSV."""
from pathlib import Path
import csv
root = Path(__file__).resolve().parents[1]
folder = root / 'docs/requirements'
with (folder / 'matrix.csv').open(newline='') as f:
    rows = list(csv.DictReader(f))
assert len({row['id'] for row in rows}) == len(rows), 'Duplicate requirement IDs'
lines = ['# Matriz de requisitos', '',
         'Fuente: PDF académico de siete páginas aportado por el propietario. Estado inicial basado en inspección; ver README de esta carpeta para interpretar los estados.', '',
         'Las rutas de evidencia son relativas a los paquetes históricos de `Cine/src/main/java`. Los criterios describen el resultado futuro exigido; no afirman que ya esté implementado.', '',
         f'Total: {len(rows)} grupos de requisitos.', '']
for row in rows:
    lines.extend([f"## {row['id']} — {row['requisito']}", '',
                  f"Página: {row['pagina']} · Estado: **{row['estado']}** · Prioridad: {row['prioridad']} · Hito: {row['hito']}", '',
                  f"Evidencia: {row['evidencia']}.", '',
                  f"Aceptación: {row['aceptacion']}.", ''])
(folder / 'MATRIX.md').write_text('\n'.join(lines))
print(f'Generated matrix: {len(rows)} requirements')

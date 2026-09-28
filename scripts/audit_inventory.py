#!/usr/bin/env python3
"""Inventory source files only; never read runtime customer or payment data."""
from pathlib import Path
import re
root = Path(__file__).resolve().parents[1]
src = root / 'Cine/src/main/java'
targets = {
    'Clientes': 'Identidad y perfiles', 'Persona': 'Perfiles; revisar herencia',
    'Personal': 'Personal y permisos', 'Datos': 'Adaptadores e importador de legado',
    'Dulceria': 'Inventario y ventas', 'Financiera': 'Pago simulado; reemplazar almacenamiento sensible',
    'ProgramaLealtad': 'Lealtad', 'CineTICS': 'Cartelera, reservas y ventas',
    'Funciones': 'Cartelera y casos de uso', 'Administrador': 'Casos de uso administrativos',
    'SIGA': 'Pedidos y simulación', 'rob': 'Asistente',
}
files = sorted(src.rglob('*.java'))
lines = ['# Inventario de fuentes', '', 'Generado con `python3 scripts/audit_inventory.py`. No incluye contenido de archivos de datos.', '',
         f'Fuentes Java: {len(files)}. Líneas: {sum(len(p.read_text().splitlines()) for p in files)}.', '',
         '| Archivo | Líneas | Destino previsto |', '|---|---:|---|']
empty = []
for p in files:
    text = p.read_text()
    rel = p.relative_to(src).as_posix()
    destination = targets.get(p.parent.name, 'Punto de entrada de consola')
    if 'com/penta/cinetics/' in rel:
        destination = {'aplicacion': 'Casos de uso y contratos', 'dominio': 'Reglas de dominio',
                       'infraestructura': 'Adaptadores de persistencia', 'consola': 'Consola de demostración'}.get(p.parent.name, destination)
    if p.name == 'MetodosEnGeneral.java':
        destination = 'Separar menús, compra, identidad y navegación'
    lines.append(f'| `{rel}` | {len(text.splitlines())} | {destination} |')
    for m in re.finditer(r'public\s+(?:static\s+)?void\s+(\w+)\([^)]*\)\s*\{\s*\}', text):
        empty.append(f'- `{rel}`: `{m.group(1)}` (línea {text[:m.start()].count(chr(10)) + 1}).')
lines.extend(['', '## Métodos públicos vacíos detectados', '', *empty, '',
              'La detección es orientativa; las clases vacías, lógica incompleta y código sin uso requieren revisión adicional. Un método no se elimina solo por estar vacío.'])
(root / 'docs/audit/INVENTORY.md').write_text('\n'.join(lines) + '\n')
print(f'Inventoried {len(files)} Java files and {len(empty)} empty public methods')

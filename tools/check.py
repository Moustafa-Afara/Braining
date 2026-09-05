# -*- coding: utf-8 -*-
import io, os, re, sys, collections

roots = [d for d in os.listdir('.') if os.path.isdir(d) and not d.startswith('.')]
kt = []
for r in roots:
    for dp, dn, fn in os.walk(r):
        dn[:] = [d for d in dn if d != 'build']
        for f in fn:
            if f.endswith('.kt'): kt.append(os.path.join(dp, f))

bad = 0
# 1. braces / parens balance, ignoring strings and comments (rough but catches gross errors)
def strip(src):
    out, i, n = [], 0, len(src)
    while i < n:
        c = src[i]
        if c == '/' and i+1 < n and src[i+1] == '/':
            while i < n and src[i] != '\n': i += 1
        elif c == '/' and i+1 < n and src[i+1] == '*':
            i += 2
            while i+1 < n and not (src[i] == '*' and src[i+1] == '/'): i += 1
            i += 2
        elif src[i:i+3] == '"""':
            i += 3
            while i+2 < n and src[i:i+3] != '"""': i += 1
            i += 3
        elif c == '"':
            i += 1
            while i < n and src[i] != '"':
                if src[i] == '\\': i += 1
                i += 1
            i += 1
        elif c == '`':
            i += 1
            while i < n and src[i] != '`': i += 1
            i += 1
        elif c == "'":
            i += 1
            while i < n and src[i] != "'":
                if src[i] == '\\': i += 1
                i += 1
            i += 1
        else:
            out.append(c); i += 1
    return ''.join(out)

for p in kt:
    src = io.open(p, encoding='utf-8').read()
    s = strip(src)
    for open_c, close_c in (('{','}'), ('(',')')):
        if s.count(open_c) != s.count(close_c):
            print("BRACE %s : %s=%d %s=%d" % (p, open_c, s.count(open_c), close_c, s.count(close_c))); bad += 1
    imports = re.findall(r'^import\s+(\S+)', src, re.M)
    dup = [k for k, v in collections.Counter(imports).items() if v > 1]
    if dup: print("DUPIMPORT %s : %s" % (p, dup)); bad += 1

print("kotlin files scanned: %d" % len(kt))

# 2. every R.string.x used in a module resolves in that module (or is fully qualified)
strings = {}
for r in roots:
    for dp, dn, fn in os.walk(r):
        dn[:] = [d for d in dn if d != 'build']
        for f in fn:
            if f == 'strings.xml':
                mod = dp.split(os.sep)[0]
                loc = 'en' if 'values-en' in dp else 'ar'
                body = io.open(os.path.join(dp, f), encoding='utf-8').read()
                for name in re.findall(r'<string name="([^"]+)"', body):
                    strings.setdefault(mod, {}).setdefault(loc, set()).add(name)
                for name in re.findall(r'<plurals name="([^"]+)"', body):
                    strings.setdefault(mod, {}).setdefault(loc, set()).add(name)

# ar/en parity
for mod, locs in sorted(strings.items()):
    a, e = locs.get('ar', set()), locs.get('en', set())
    if a - e: print("MISSING-EN %s : %s" % (mod, sorted(a - e))); bad += 1
    if e - a: print("MISSING-AR %s : %s" % (mod, sorted(e - a))); bad += 1

deps = {  # what each module can see (its own + core-ui + core-domain)
}
for p in kt:
    mod = p.split(os.sep)[0]
    src = io.open(p, encoding='utf-8').read()
    own = strings.get(mod, {}).get('ar', set())
    ui  = strings.get('core-ui', {}).get('ar', set())
    for m in re.finditer(r'(?<!\.)\bR\.(?:string|plurals)\.(\w+)', src):
        name = m.group(1)
        if name in own: continue
        if mod == 'core-ui' and name in ui: continue
        print("UNRESOLVED %s : R.string.%s (module %s)" % (p, name, mod)); bad += 1
    for m in re.finditer(r'com\.braining\.core\.ui\.R\.(?:string|plurals)\.(\w+)', src):
        if m.group(1) not in ui:
            print("UNRESOLVED-QUALIFIED %s : core.ui.R.string.%s" % (p, m.group(1))); bad += 1

# 3. unescaped apostrophes in Arabic/English string bodies (AAPT2 hard error)
for r in roots:
    for dp, dn, fn in os.walk(r):
        dn[:] = [d for d in dn if d != 'build']
        for f in fn:
            if f.endswith('.xml') and 'values' in dp:
                for i, line in enumerate(io.open(os.path.join(dp, f), encoding='utf-8'), 1):
                    body = re.search(r'>([^<]*)<', line)
                    if body and re.search(r"(?<!\\)'", body.group(1)):
                        print("APOSTROPHE %s:%d : %s" % (os.path.join(dp, f), i, line.strip()[:90])); bad += 1

print("PROBLEMS: %d" % bad)

# ── missing-import check, added 2026-09-05 ────────────────────────────────────────
# `DropdownMenu(` shipped with no import because an insertion guard matched
# `...DropdownMenuItem` as a substring. Braces, duplicate imports and resources all
# passed; only the compiler caught it, and the compiler runs on the owner's machine.
import subprocess, os as _os
_r = subprocess.run(['python3', _os.path.expanduser('~/imports.py')],
                    capture_output=True, text=True, cwd=_os.getcwd())
print(_r.stdout.strip().splitlines()[-1] if _r.stdout.strip() else 'import check: no output')
for _l in _r.stdout.splitlines():
    if _l.startswith('MISSING IMPORT?'):
        print(_l)

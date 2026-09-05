# -*- coding: utf-8 -*-
"""
Catches a symbol used but never imported.

Written 2026-09-05 after `DropdownMenu(` shipped to the owner's build with no import. The
insertion script had guarded with `if "import ...DropdownMenu" in source: continue` — and that
string IS present, as a **substring of `...DropdownMenuItem`**. The guard was satisfied by a
different import, the add was skipped, and nothing downstream noticed: the brace check passed,
the duplicate-import check passed, the resource checks passed. Only the compiler saw it, and the
compiler runs on his machine.

Heuristic, deliberately: it flags a capitalised name used as a call which has no import ending in
that name, is not declared anywhere in its own package, and is not Kotlin stdlib. False positives
are cheap here (one look); the false negative cost an hour of the owner's time.
"""
import io, os, re, sys

STDLIB = set("""
String Int Long Double Float Boolean Char Byte Short Any Unit Nothing
Array List Map Set Pair Triple Regex StringBuilder Exception RuntimeException
IllegalStateException IllegalArgumentException Error Throwable Comparator
IntRange LongRange CharRange Result Comparable Iterable Sequence Number
ArrayDeque LinkedHashSet LinkedHashMap HashSet HashMap ByteArray IntArray
CharArray BooleanArray MutableList MutableMap MutableSet StringBuffer
""".split())

# ALL-CAPS names are enum entries and const markers, never calls needing an import.
def is_constant(name):
    return name.upper() == name

roots = [d for d in os.listdir('.') if os.path.isdir(d) and not d.startswith('.')]
files = []
for r in roots:
    for dp, dn, fn in os.walk(r):
        dn[:] = [d for d in dn if d != 'build']
        for f in fn:
            if f.endswith('.kt'):
                files.append(os.path.join(dp, f))

# what each package declares, so a same-package reference needs no import
declared = {}
src_of = {}
for p in files:
    s = io.open(p, encoding='utf-8').read()
    src_of[p] = s
    m = re.search(r'^package\s+([\w.]+)', s, re.M)
    pkg = m.group(1) if m else ''
    for d in re.findall(r'^\s*(?:@\w+\s+)*(?:public |internal |private |abstract |open |sealed |data |enum |value |annotation )*(?:class|object|interface|fun)\s+([A-Z]\w*)', s, re.M):
        declared.setdefault(pkg, set()).add(d)
    # top-level typealiases and vals too
    for d in re.findall(r'^\s*typealias\s+([A-Z]\w*)', s, re.M):
        declared.setdefault(pkg, set()).add(d)

bad = 0
for p in files:
    s = src_of[p]
    pkg = (re.search(r'^package\s+([\w.]+)', s, re.M) or [None, ''])[1] if re.search(r'^package\s+([\w.]+)', s, re.M) else ''
    imported = set()
    for imp in re.findall(r'^import\s+([\w.]+)(?:\s+as\s+(\w+))?', s, re.M):
        path, alias = imp
        imported.add(alias or path.split('.')[-1])
    own = declared.get(pkg, set())
    body = '\n'.join(l for l in s.split('\n') if not l.startswith('import ') and not l.startswith('package '))
    # strip comments and strings so prose cannot trigger a finding
    body = re.sub(r'//[^\n]*', '', body)
    body = re.sub(r'/\*.*?\*/', '', body, flags=re.S)
    body = re.sub(r'"""».*?"""', '""', body, flags=re.S)
    body = re.sub(r'"(?:[^"\\\n]|\\.)*"', '""', body)
    seen = set()
    for m in re.finditer(r'(?<![\w.@])([A-Z]\w*)\s*[(<]', body):
        name = m.group(1)
        if name in seen or name in imported or name in own or name in STDLIB or is_constant(name):
            continue
        seen.add(name)
        print("MISSING IMPORT?  %s : %s" % (p, name))
        bad += 1

print("possible missing imports: %d" % bad)

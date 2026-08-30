# Prompt-Framework Library — Braining (فهم)

The FORGE stage turns a matured idea into a rigorous English prompt using a
**templated framework** chosen for the task type. This file is the catalogue plus
the selection heuristics. "Most suitable framework" is a **heuristic**, not a rigid
rule — always show the chosen framework to the user and let them edit or swap it.

## 1. Framework catalogue

### CO-STAR — general structured prompts
Context · Objective · Style · Tone · Audience · Response-format.
Best for: consultation, summaries, writing, business/finance explainers, teaching.

### RTF — quick, crisp tasks
Role · Task · Format.
Best for: short, well-scoped requests where heavy scaffolding is overkill.

### ReAct — reasoning + acting (tool use)
Interleaves Reasoning and Actions/tool calls with observations.
Best for: agentic tasks, web research, and ALL Path B (PC agent) work.

### Chain-of-Thought / Decomposition — hard reasoning
Ask for explicit step-by-step reasoning or break the problem into sub-problems.
Best for: analysis, planning, scientific reasoning, architecture decisions.

### Anthropic-style structure — high-fidelity control
XML-like tags to delimit sections + few-shot examples + explicit output contract.
Best for: complex builds, precise formats, and anything the DEFAULT BRAIN (Claude)
executes. Combine with CO-STAR or ReAct as the backbone.

### TAG — outcome-focused
Task · Action · Goal.
Best for: automation and digital-transformation requests framed around an outcome.

### Persona + Few-shot — style/skill transfer
A strong role persona plus 2–5 worked examples.
Best for: English-language teaching, entrepreneurship coaching, discussion partners.

## 2. Task-type → framework mapping (default routing table, user-editable)

| Task type | Primary framework | Backbone / combine with |
|---|---|---|
| Software build (large) | Anthropic-style structure | + ReAct (Path B), + decomposition |
| Systems analysis | Chain-of-Thought / decomposition | + CO-STAR |
| English teaching | Persona + Few-shot | + CO-STAR |
| Idea / project discussion | CO-STAR | + persona |
| Entrepreneurship & finance | CO-STAR | + decomposition |
| Automation systems | TAG | + ReAct |
| Digital transformation | TAG | + decomposition |
| Research (broad web) | ReAct | + CO-STAR |
| Scientific research | Chain-of-Thought | + Anthropic-style structure |
| Planning | Decomposition | + CO-STAR |
| Quick task | RTF | — |

## 3. Selection heuristics (how FORGE decides)

1. Classify the task type from the refined idea (reuse the router's classification).
2. Pick the PRIMARY framework from the table above.
3. If the task uses tools or acts on files/web → ensure ReAct is in the mix.
4. If reasoning is the hard part → add Chain-of-Thought / decomposition.
5. If the DEFAULT BRAIN (Claude) will execute → wrap in Anthropic-style structure
   (XML tags + explicit output contract + few-shot where helpful).
6. Always fill a consistent skeleton so prompts are comparable and improvable:
   ROLE · CONTEXT · OBJECTIVE · CONSTRAINTS · INPUT · REASONING-GUIDANCE ·
   OUTPUT-CONTRACT · EXAMPLES(optional).
7. Show the chosen framework + a one-line rationale; let the user edit or swap it,
   then regenerate.

## 4. Universal prompt skeleton (the FORGE fills this)

```
# ROLE
<who the model should be for this task>

# CONTEXT
<the matured idea + relevant session context>

# OBJECTIVE
<the single, sharp goal>

# CONSTRAINTS
<must / must-not, scope, tools allowed, guardrails for Path B>

# INPUT
<the user's material / data / files-as-text>

# REASONING GUIDANCE
<CoT / decomposition / ReAct instructions as applicable>

# OUTPUT CONTRACT
<exact format, language(EN), length, structure the result must follow>

# EXAMPLES   (optional, few-shot)
<worked examples that pin down the style/format>
```

## 5. Notes

- Frameworks are **building blocks**, not silos — combining CO-STAR (structure) with
  ReAct (tool use) and Anthropic-style tags (control) is normal and encouraged.
- Keep the library data-driven so new frameworks can be added without code changes.
- The generated prompt is always in **English**; the result is translated back to
  **Arabic** for the user at the TRANSLATE stage.

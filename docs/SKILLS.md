# Agent Skills Catalogue — Braining (فهم)

These are **agent skills** (procedural knowledge installed into the OpenCode
environment) that raise build quality across ALL of the owner's domains. They are
**not** Android dependencies — Android libraries (Ktor, Hilt, Room, Compose, …) are
declared in Gradle as usual.

Install everything with:

```bash
bash scripts/install-skills.sh
```

Skills come from the open Skills directory (https://www.skills.sh) via
`npx skills add <owner/repo/skill>`. Slugs occasionally change — if one 404s, search
the directory for the closest current equivalent and install that, then note the
substitution in your build log.

---

## Domain → skills

### A. AI software engineering & code quality
- `anthropics/skills/frontend-design` — distinctive, non-templated UI design.
- `vercel-labs/agent-skills/vercel-react-native-skills` — mobile/RN patterns.
- `vercel-labs/skills/find-skills` — discover further skills on demand.
- `mattpocock/skills/codebase-design` — sound codebase architecture.
- `mattpocock/skills/domain-modeling` — model the domain cleanly.
- `mattpocock/skills/code-review` — structured self-review.
- `obra/superpowers/systematic-debugging` — disciplined debugging.
- `obra/superpowers/test-driven-development` — TDD workflow.
- `obra/superpowers/requesting-code-review` — request/receive review loops.

### B. Systems analysis & deep planning (matches "analyze-first" philosophy)
- `obra/superpowers/brainstorming` — structured idea exploration.
- `obra/superpowers/writing-plans` — turn goals into rigorous plans.
- `obra/superpowers/executing-plans` — execute plans faithfully.
- `obra/superpowers/verification-before-completion` — verify before "done".
- `mattpocock/skills/to-prd` — shape a request into a product-requirements doc.
- `lllllllama/rigorpilot-skills/analyze-project` — rigorous project analysis.

### C. English-language teaching
- `mattpocock/skills/teach` — explain/teach a concept effectively.
- `coreyhaines31/marketingskills/copywriting` — clear, correct written English.
- `coreyhaines31/marketingskills/copy-editing` — editing and language polish.
- `mattpocock/skills/edit-article` — structured editing of long-form text.

### D. Idea & project discussion
- `obra/superpowers/brainstorming` — (shared) idea discussion partner.
- `mattpocock/skills/zoom-out` — see the bigger picture of a project.
- `mattpocock/skills/triage` — prioritize among many ideas/tasks.

### E. Entrepreneurship & finance
- `coreyhaines31/marketingskills/marketing-psychology` — go-to-market thinking.
- `coreyhaines31/marketingskills/content-strategy` — content/business strategy.
- `coreyhaines31/marketingskills/sales-enablement` — sales/revenue framing.
- `coreyhaines31/marketingskills/revops` — revenue-operations perspective.
- `coreyhaines31/marketingskills/churn-prevention` — retention/finance angle.

### F. Automation systems
- `firecrawl/cli/firecrawl-agent` — automated web data/agents.
- `browser-use/browser-use/browser-use` — browser automation.
- `microsoft/playwright-cli/playwright-cli` — reliable browser automation/testing.
- `scrapegraphai/just-scrape/just-scrape` — structured scraping automation.

### G. Digital-transformation projects
- `microsoft/azure-skills/azure-cloud-migrate` — migration/transformation planning.
- `microsoft/azure-skills/azure-enterprise-infra-planner` — infra planning at scale.
- `microsoft/azure-skills/azure-observability` — operational visibility.
- `supabase/agent-skills/supabase` — modern data backend for transformed systems.

### H. Research & scientific work
- `firecrawl/cli/firecrawl-search` — broad web research.
- `firecrawl/cli/firecrawl-crawl` — deep crawl for research.
- `lllllllama/ai-paper-reproduction-skill/paper-context-resolver` — read/interpret papers.
- `lllllllama/ai-paper-reproduction-skill/repo-intake-and-plan` — reproduce research code.
- `lllllllama/ai-paper-reproduction-skill/minimal-run-and-audit` — verify research results.

### I. Planning
- `obra/superpowers/writing-plans` — (shared) rigorous planning.
- `mattpocock/skills/to-issues` — break a plan into actionable issues.
- `mattpocock/skills/handoff` — clean handoff/checkpoint documents.

### J. Complementary skills (support all of the above)
- `obra/superpowers/using-superpowers` — meta-skill: use the skill set well.
- `obra/superpowers/dispatching-parallel-agents` — parallelize sub-tasks.
- `obra/superpowers/using-git-worktrees` — manage parallel work safely.
- `anthropics/skills/mcp-builder` — build MCP tools/integrations.
- `anthropics/skills/webapp-testing` — test web/app deliverables.
- `pbakaus/impeccable/polish` — final-mile quality polish.
- `pbakaus/impeccable/critique` — critical review of output.

---

## Notes for the agent

- Install skills **before** you design, so they inform your architecture and code.
- Prefer the closest current equivalent for any moved/renamed slug; never block on a
  single 404 — substitute and continue, and record what you changed.
- Skills improve *your* process; they do not ship inside the APK.

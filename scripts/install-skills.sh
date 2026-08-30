#!/usr/bin/env bash
# ==============================================================================
# install-skills.sh - install ALL agent skills for the Braining project
# into the OpenCode environment. Run this BEFORE building.
#
# These are AGENT SKILLS (procedural knowledge for the building agent), NOT
# Android dependencies. See docs/SKILLS.md for the domain-by-domain rationale.
#
# Usage:   bash scripts/install-skills.sh
# Requires: Node.js (for npx) and network access.
# ==============================================================================

set -u  # do NOT set -e: one bad/renamed slug must not abort the whole install.

# --- helpers ------------------------------------------------------------------
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
ok(){   printf "${GREEN}[ok] %s${NC}\n" "$1"; }
warn(){ printf "${YELLOW}[warn] %s${NC}\n" "$1"; }
err(){  printf "${RED}[error] %s${NC}\n" "$1"; }

FAILED=()

install_skill() {
  local slug="$1"
  local repo skill
  # Parse "owner/repo/skill-name" into repo="owner/repo" and skill="skill-name"
  repo=$(echo "$slug" | cut -d'/' -f1-2)
  skill=$(echo "$slug" | cut -d'/' -f3)
  echo "-> installing: $slug"
  if npx --yes skills add "$repo" --skill "$skill" -y; then
    ok "$slug"
  else
    warn "could not install $slug - find the closest current equivalent in the directory and install it, then note the substitution."
    FAILED+=("$slug")
  fi
}

# --- preflight ----------------------------------------------------------------
if ! command -v npx >/dev/null 2>&1; then
  err "npx not found. Install Node.js first from nodejs.org, then re-run."
  exit 1
fi

echo "Installing Braining agent skills across all domains..."
echo "(If a slug has moved, this script warns and continues - substitute later.)"
echo

# --- A. AI software engineering & code quality --------------------------------
install_skill "anthropics/skills/frontend-design"
install_skill "vercel-labs/agent-skills/vercel-react-native-skills"
install_skill "vercel-labs/skills/find-skills"
install_skill "mattpocock/skills/codebase-design"
install_skill "mattpocock/skills/domain-modeling"
install_skill "mattpocock/skills/code-review"
install_skill "obra/superpowers/systematic-debugging"
install_skill "obra/superpowers/test-driven-development"
install_skill "obra/superpowers/requesting-code-review"

# --- B. Systems analysis & deep planning --------------------------------------
install_skill "obra/superpowers/brainstorming"
install_skill "obra/superpowers/writing-plans"
install_skill "obra/superpowers/executing-plans"
install_skill "obra/superpowers/verification-before-completion"
install_skill "mattpocock/skills/to-spec"

# --- C. English-language teaching ---------------------------------------------
install_skill "mattpocock/skills/teach"
install_skill "coreyhaines31/marketingskills/copywriting"
install_skill "coreyhaines31/marketingskills/copy-editing"
install_skill "mattpocock/skills/edit-article"

# --- D. Idea & project discussion ---------------------------------------------
install_skill "mattpocock/skills/triage"

# --- E. Entrepreneurship & finance --------------------------------------------
install_skill "coreyhaines31/marketingskills/marketing-psychology"
install_skill "coreyhaines31/marketingskills/content-strategy"
install_skill "coreyhaines31/marketingskills/sales-enablement"
install_skill "coreyhaines31/marketingskills/revops"
install_skill "coreyhaines31/marketingskills/churn-prevention"

# --- F. Automation systems ----------------------------------------------------
install_skill "firecrawl/cli/firecrawl-agent"
install_skill "browser-use/browser-use/browser-use"
install_skill "microsoft/playwright-cli/playwright-cli"
install_skill "scrapegraphai/just-scrape/just-scrape"

# --- G. Digital-transformation projects ---------------------------------------
install_skill "microsoft/azure-skills/azure-cloud-migrate"
install_skill "microsoft/azure-skills/azure-enterprise-infra-planner"
install_skill "supabase/agent-skills/supabase"

# --- H. Research & scientific work --------------------------------------------
install_skill "firecrawl/cli/firecrawl-search"
install_skill "firecrawl/cli/firecrawl-crawl"

# --- I. Planning --------------------------------------------------------------
install_skill "mattpocock/skills/to-tickets"
install_skill "mattpocock/skills/handoff"

# --- J. Complementary ---------------------------------------------------------
install_skill "obra/superpowers/using-superpowers"
install_skill "obra/superpowers/dispatching-parallel-agents"
install_skill "obra/superpowers/using-git-worktrees"
install_skill "anthropics/skills/mcp-builder"
install_skill "anthropics/skills/webapp-testing"

# --- summary ------------------------------------------------------------------
echo
echo "----------------------------------------------"
if [ ${#FAILED[@]} -eq 0 ]; then
  ok "All skills installed successfully."
else
  warn "${#FAILED[@]} skill(s) need a manual substitute (slug likely moved):"
  for s in "${FAILED[@]}"; do echo "   - $s"; done
  echo "Search https://www.skills.sh for the closest current equivalent and install it."
fi
echo "Done. You may now proceed with the build steps in BRAINING.md."
